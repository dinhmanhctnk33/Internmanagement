package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/my-contract")
public class InternContractServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = requireIntern(request, response);
        if (user == null) return;
        try (Connection connection = DBConnection.getConnection()) {
            Map<String, Object> contract = findContract(connection, user.getId(), null);
            Long openId = parseId(request.getParameter("open"));
            if (openId != null) {
                Map<String, Object> owned = findContract(connection, user.getId(), openId);
                if (owned == null || owned.get("contract_pdf_url") == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.getSession().setAttribute("viewedContractId", openId);
                response.sendRedirect(request.getContextPath() + normaliseFileUrl(String.valueOf(owned.get("contract_pdf_url"))));
                return;
            }
            request.setAttribute("contract", contract);
            request.setAttribute("contractViewed", contract != null && contract.get("id").equals(
                    request.getSession().getAttribute("viewedContractId")));
            request.setAttribute("currentUser", user);
            request.getRequestDispatcher("/WEB-INF/views/intern-contract.jsp").forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải hợp đồng thực tập.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireIntern(request, response);
        if (user == null) return;
        Long contractId = parseId(request.getParameter("contractId"));
        try {
            if (contractId == null) throw new IllegalArgumentException("Hợp đồng không hợp lệ.");
            if (!contractId.equals(request.getSession().getAttribute("viewedContractId"))) {
                throw new IllegalArgumentException("Vui lòng mở và kiểm tra bản PDF trước khi xác nhận.");
            }
            if (request.getParameter("agreement") == null) {
                throw new IllegalArgumentException("Bạn cần đồng ý với nội dung hợp đồng trước khi xác nhận.");
            }
            try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                    "UPDATE contracts c JOIN intern_profiles ip ON ip.id=c.intern_profile_id "
                            + "SET c.signature_status='SIGNED',c.signed_at=NOW(),c.signed_ip_address=? "
                            + "WHERE c.id=? AND ip.user_id=? AND c.signature_status='SENT'")) {
                statement.setString(1, request.getRemoteAddr());
                statement.setLong(2, contractId);
                statement.setInt(3, user.getId());
                if (statement.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Hợp đồng đã được xác nhận hoặc không còn hiệu lực xác nhận.");
                }
            }
            request.getSession().removeAttribute("viewedContractId");
            response.sendRedirect(request.getContextPath() + "/my-contract?success=confirmed");
        } catch (IllegalArgumentException exception) {
            response.sendRedirect(request.getContextPath() + "/my-contract?error="
                    + URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
        } catch (SQLException exception) {
            response.sendRedirect(request.getContextPath() + "/my-contract?error="
                    + URLEncoder.encode("Không thể xác nhận hợp đồng. Vui lòng thử lại.", StandardCharsets.UTF_8));
        }
    }

    private Map<String, Object> findContract(Connection connection, int userId, Long contractId) throws SQLException {
        String sql = "SELECT c.id,c.contract_number,c.effective_date,c.expiration_date,c.contract_pdf_url," 
                + "c.signature_status,c.signed_at,p.program_name,d.dept_name "
                + "FROM contracts c JOIN intern_profiles ip ON ip.id=c.intern_profile_id "
                + "LEFT JOIN internship_programs p ON p.id=ip.program_id LEFT JOIN departments d ON d.id=p.department_id "
                + "WHERE ip.user_id=? AND c.signature_status<>'DRAFT' "
                + (contractId == null ? "ORDER BY c.id DESC LIMIT 1" : "AND c.id=?");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            if (contractId != null) statement.setLong(2, contractId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                    row.put(result.getMetaData().getColumnLabel(i), result.getObject(i));
                }
                return row;
            }
        }
    }

    private String normaliseFileUrl(String url) {
        String name = url.substring(url.lastIndexOf('/') + 1);
        return "/contract-files/" + name;
    }

    private Long parseId(String value) {
        try { return value == null ? null : Long.valueOf(value); }
        catch (NumberFormatException exception) { return null; }
    }

    private User requireIntern(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"INTERN".equalsIgnoreCase(String.valueOf(request.getSession().getAttribute("userRole")))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }
}
