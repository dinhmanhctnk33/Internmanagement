package com.example.internmanagement.controller;

import com.example.internmanagement.model.User;
import com.example.internmanagement.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@WebServlet("/leave-requests")
public class LeaveRequestServlet extends HttpServlet {
    private static final Set<String> TYPES=Set.of("SICK_LEAVE","PERSONAL_LEAVE","UNPAID_LEAVE");
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        String role=role(req);resp.sendRedirect(req.getContextPath()+("INTERN".equals(role)?"/attendance":"/attendance-report"));
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");String role=role(req);
        try(Connection c=DBConnection.getConnection()){
            if("INTERN".equals(role))submit(req,c);else if("HR".equals(role)||"ADMIN".equals(role))review(req,c);else{resp.sendError(403);return;}
            resp.sendRedirect(req.getContextPath()+("INTERN".equals(role)?"/attendance?leave=created":"/attendance-report?leave=reviewed"));
        }catch(Exception e){String message=URLEncoder.encode(e.getMessage()==null?"Không thể xử lý đơn nghỉ.":e.getMessage(),StandardCharsets.UTF_8);resp.sendRedirect(req.getContextPath()+("INTERN".equals(role)?"/attendance":"/attendance-report")+"?error="+message);}
    }
    private void submit(HttpServletRequest req,Connection c)throws Exception{
        User user=(User)req.getSession().getAttribute("user");String type=clean(req.getParameter("leaveType"));if(!TYPES.contains(type))throw new IllegalArgumentException("Loại nghỉ không hợp lệ.");
        LocalDate from=LocalDate.parse(clean(req.getParameter("startDate"))),to=LocalDate.parse(clean(req.getParameter("endDate")));if(to.isBefore(from))throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");if(from.isBefore(LocalDate.now()))throw new IllegalArgumentException("Không thể gửi đơn cho ngày đã qua.");
        String reason=clean(req.getParameter("reason"));if(reason.length()<5)throw new IllegalArgumentException("Vui lòng nhập lý do ít nhất 5 ký tự.");
        long days=ChronoUnit.DAYS.between(from,to)+1;
        String sql="INSERT INTO leave_requests(intern_profile_id,leave_type,start_date,end_date,total_days,reason) SELECT ip.id,?,?,?,?,? FROM intern_profiles ip WHERE ip.user_id=? AND ip.internship_status='ACTIVE'";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,type);s.setDate(2,Date.valueOf(from));s.setDate(3,Date.valueOf(to));s.setLong(4,days);s.setString(5,reason);s.setInt(6,user.getId());if(s.executeUpdate()!=1)throw new IllegalArgumentException("Tài khoản chưa có hồ sơ thực tập đang hoạt động.");}
    }
    private void review(HttpServletRequest req,Connection c)throws Exception{
        long id=Long.parseLong(clean(req.getParameter("leaveId")));String decision=clean(req.getParameter("decision"));if(!Set.of("APPROVED","REJECTED").contains(decision))throw new IllegalArgumentException("Kết quả duyệt không hợp lệ.");String note=clean(req.getParameter("note"));
        try(PreparedStatement s=c.prepareStatement("UPDATE leave_requests SET approval_status=?,approver_notes=? WHERE id=? AND approval_status='PENDING'")){s.setString(1,decision);s.setString(2,note.isBlank()?null:note);s.setLong(3,id);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Đơn nghỉ đã được xử lý hoặc không tồn tại.");}
    }
    private String role(HttpServletRequest req){return String.valueOf(req.getSession().getAttribute("userRole")).toUpperCase();}
    private String clean(String value){return value==null?"":value.trim();}
}
