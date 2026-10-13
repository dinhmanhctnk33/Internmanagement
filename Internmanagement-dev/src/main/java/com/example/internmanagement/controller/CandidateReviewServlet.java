package com.example.internmanagement.controller;

import com.example.internmanagement.dao.CandidateDAO;
import com.example.internmanagement.dao.ReviewEmailQueueDAO;
import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.EmailUtility;
import com.example.internmanagement.util.ReviewEmailProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Set;

@WebServlet("/applications")
public class CandidateReviewServlet extends HttpServlet {
    private final CandidateDAO candidates=new CandidateDAO();
    private final ReviewEmailQueueDAO emails=new ReviewEmailQueueDAO();
    private static final Set<String> FILTERS=Set.of("NEW","INTERVIEWING","ACCEPTED","REJECTED");

    @Override public void init() throws ServletException { try{emails.ensureSchema();}catch(Exception e){throw new ServletException(e);} }

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String status=clean(req.getParameter("status"));if(!FILTERS.contains(status))status="";
        String q=clean(req.getParameter("q"));
        try{
            req.setAttribute("candidates",candidates.findAll(status,q));req.setAttribute("selectedStatus",status);req.setAttribute("keyword",q);
            req.getRequestDispatcher("/WEB-INF/views/candidate-review.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException("Không thể tải danh sách ứng viên.",e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8");
        try{
            int candidateId=Integer.parseInt(req.getParameter("candidateId"));
            String decision=clean(req.getParameter("decision"));String reason=clean(req.getParameter("reason"));
            if(!Set.of("PASSED","FAILED").contains(decision))throw new IllegalArgumentException("Kết quả không hợp lệ.");
            if(reason.length()<5)throw new IllegalArgumentException("Vui lòng nhập lý do ít nhất 5 ký tự.");
            String candidateStatus="PASSED".equals(decision)?"ACCEPTED":"REJECTED";
            try(Connection c=DBConnection.getConnection()){
                c.setAutoCommit(false);try{
                    if(!candidates.decide(c,candidateId,candidateStatus,reason))throw new IllegalStateException("Ứng viên đã được xác nhận kết quả trước đó.");
                    if("PASSED".equals(decision)) promoteToIntern(c,candidateId);
                    if(emails.enqueue(c,candidateId,decision,reason,LocalDateTime.now())==0)throw new IllegalStateException("Ứng viên chưa có địa chỉ email.");
                    c.commit();
                }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
            }
            int sent=0;if(EmailUtility.isConfigured())sent=new ReviewEmailProcessor().processDue();
            resp.sendRedirect(req.getContextPath()+"/applications?result="+(sent>0?"sent":"queued"));
        }catch(Exception e){resp.sendRedirect(req.getContextPath()+"/applications?error="+java.net.URLEncoder.encode(e.getMessage(),java.nio.charset.StandardCharsets.UTF_8));}
    }
    private String clean(String value){return value==null?"":value.trim();}

    private void promoteToIntern(Connection connection,int candidateId)throws Exception{
        String sql="SELECT u.id,c.university_name,c.major_name,c.phone FROM candidates c JOIN users u ON LOWER(u.email)=LOWER(c.email) WHERE c.id=?";
        int userId;String university,major,phone;
        try(var statement=connection.prepareStatement(sql)){statement.setInt(1,candidateId);try(var result=statement.executeQuery()){if(!result.next())throw new IllegalStateException("Không tìm thấy tài khoản của ứng viên.");userId=result.getInt("id");university=result.getString("university_name");major=result.getString("major_name");phone=result.getString("phone");}}
        int internRoleId;
        try(var statement=connection.prepareStatement("SELECT id FROM roles WHERE role_code='INTERN'");var result=statement.executeQuery()){if(!result.next())throw new IllegalStateException("Hệ thống chưa cấu hình vai trò INTERN.");internRoleId=result.getInt(1);}
        try(var statement=connection.prepareStatement("DELETE ur FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.role_code='CANDIDATE'")){statement.setInt(1,userId);statement.executeUpdate();}
        try(var statement=connection.prepareStatement("INSERT IGNORE INTO user_roles(user_id,role_id) VALUES(?,?)")){statement.setInt(1,userId);statement.setInt(2,internRoleId);statement.executeUpdate();}
        String internCode=String.format("TTS%d%04d",java.time.Year.now().getValue(),candidateId);
        try(var statement=connection.prepareStatement("INSERT INTO intern_profiles(user_id,intern_code,university_name,major_name,phone_number,internship_status) VALUES(?,?,?,?,?,'ACTIVE') ON DUPLICATE KEY UPDATE university_name=VALUES(university_name),major_name=VALUES(major_name),phone_number=VALUES(phone_number),internship_status='ACTIVE'")){statement.setInt(1,userId);statement.setString(2,internCode);statement.setString(3,university);statement.setString(4,major);statement.setString(5,phone);statement.executeUpdate();}
    }
}
