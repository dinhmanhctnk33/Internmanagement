package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternProfileDAO;
import com.example.internmanagement.model.InternProfile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet("/interns/import")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class InternImportServlet extends HttpServlet {
    private static final String PENDING_IMPORT = "pendingInternImport";
    private final InternProfileDAO dao = new InternProfileDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if ("confirm".equals(request.getParameter("action"))) { 
            confirm(request, response); 
            return; 
        }
        preview(request, response);
    }

    private void preview(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Part file = request.getPart("excelFile");
        String fileName = file == null ? "" : file.getSubmittedFileName();
        if (file == null || file.getSize() == 0 || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            result(request, response, "Vui lòng chọn tệp Excel .xlsx, tối đa 5 MB."); 
            return; 
        }
        
        try (InputStream input = file.getInputStream(); Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) { 
                result(request, response, "Tệp Excel chưa có dữ liệu để import."); 
                return; 
            }
            if (sheet.getLastRowNum() > 2000) { 
                result(request, response, "Mỗi lần import tối đa 2.000 dòng."); 
                return; 
            }
            
            DataFormatter formatter = new DataFormatter();
            PendingImport pending = new PendingImport();
            
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(row, formatter)) continue;
                
                String internCode = cell(row, 0, formatter);
                String fullName = cell(row, 1, formatter);
                String email = cell(row, 2, formatter);
                if (internCode.isBlank() || fullName.isBlank() || email.isBlank()) {
                    pending.errors.add("Dòng " + (rowIndex + 1) + ": cần có Mã TTS, họ tên và email.");
                    continue; 
                }

                String mentorName = cell(row, 6, formatter);
                String note = cell(row, 8, formatter);
                if (!note.isBlank()) {
                    pending.errors.add("Dòng " + (rowIndex + 1) + ": schema hiện tại chưa hỗ trợ lưu Ghi chú.");
                    continue;
                }

                String status = cell(row, 7, formatter);
                status = status.isBlank() ? "ACTIVE" : status.toUpperCase(Locale.ROOT);
                if (!List.of("ACTIVE", "COMPLETED", "PAUSED", "DROPPED").contains(status)) {
                    pending.errors.add("Dòng " + (rowIndex + 1) + ": trạng thái không hợp lệ.");
                    continue;
                }

                InternProfile existing = dao.findByCode(internCode);
                Integer userId = dao.findActiveInternUserIdByEmail(email);
                if (existing != null && !email.equalsIgnoreCase(existing.getEmail())) {
                    pending.errors.add("Dòng " + (rowIndex + 1) + ": mã TTS đã được liên kết với email khác.");
                    continue;
                }
                if (userId == null && existing == null) {
                    pending.errors.add("Dòng " + (rowIndex + 1) + ": email chưa thuộc tài khoản thực tập sinh đang hoạt động.");
                    continue;
                }

                InternProfile profile = existing == null ? new InternProfile() : existing;
                if (existing == null) {
                    profile.setUserId(userId);
                }
                profile.setFullName(fullName);
                profile.setEmail(email);
                profile.setInternCode(internCode);
                profile.setPhoneNumber(cell(row, 3, formatter));
                profile.setUniversityName(cell(row, 4, formatter));
                profile.setMajorName(cell(row, 5, formatter));
                profile.setInternshipStatus(status);
                if (!mentorName.isBlank()) {
                    Integer mentorId = dao.findMentorIdByName(mentorName);
                    if (mentorId == null) {
                        pending.errors.add("Dòng " + (rowIndex + 1) + ": không tìm thấy Mentor đang hoạt động hoặc tên không duy nhất.");
                        continue;
                    }
                    profile.setMentorId(mentorId);
                }

                pending.profiles.add(profile);
            }
            
            if (pending.profiles.isEmpty()) { 
                result(request, response, "Không có dòng hợp lệ để import."); 
                return; 
            }
            
            request.getSession().setAttribute(PENDING_IMPORT, pending);
            request.setAttribute("pendingImport", pending);
            request.getRequestDispatcher("/WEB-INF/views/screens/intern-import-confirm.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Không thể đọc tệp Excel. Kiểm tra đúng định dạng .xlsx.", e);
        } catch (SQLException e) {
            throw new ServletException("Không thể kiểm tra dữ liệu import trong cơ sở dữ liệu.", e);
        }
    }

    private void confirm(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        PendingImport pending = session == null ? null : (PendingImport) session.getAttribute(PENDING_IMPORT);
        if (pending == null || pending.profiles.isEmpty()) { 
            result(request, response, "Phiên xác nhận import đã hết hạn. Vui lòng tải lại tệp."); 
            return; 
        }
        
        int created = 0, updated = 0;
        try {
            for (InternProfile profile : pending.profiles) {
                if (profile.getId() == 0) {
                    created++;
                    dao.insert(profile);
                } else {
                    updated++;
                    dao.update(profile);
                }
            }
            
            String message = "Import hoàn tất: thêm " + created + ", cập nhật " + updated + " hồ sơ.";
            if (!pending.errors.isEmpty()) {
                message += " Bỏ qua " + pending.errors.size() + " dòng không hợp lệ.";
            }
            
            session.removeAttribute(PENDING_IMPORT);
            result(request, response, message);
        } catch (SQLException e) { 
            throw new ServletException("Không thể lưu dữ liệu Excel vào hệ thống.", e); 
        }
    }

    private void result(HttpServletRequest request, HttpServletResponse response, String message) throws IOException { 
        request.getSession().setAttribute("importResult", message); 
        response.sendRedirect(request.getContextPath() + "/interns"); 
    }
    
    private String cell(Row row, int index, DataFormatter formatter) { 
        return row.getCell(index) == null ? "" : formatter.formatCellValue(row.getCell(index)).trim(); 
    }
    
    private boolean isBlank(Row row, DataFormatter formatter) { 
        for (int i = 0; i < 9; i++) {
            if (!cell(row, i, formatter).isBlank()) return false; 
        }
        return true; 
    }

    public static class PendingImport { 
        public final List<InternProfile> profiles = new ArrayList<>(); 
        public final List<String> errors = new ArrayList<>(); 
    }
}