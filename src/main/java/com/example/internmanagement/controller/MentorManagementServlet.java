package com.example.internmanagement.controller;

import com.example.internmanagement.dao.MentorManagementDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/mentors")
public class MentorManagementServlet extends HttpServlet {
    private static final Pattern EMAIL=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final MentorManagementDAO dao=new MentorManagementDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("user");
        try{
            String q=value(req,"q");String status=value(req,"status");if(status.isEmpty())status="ALL";Long department=id(req.getParameter("departmentId"));
            req.setAttribute("mentors",dao.findAll(q,department,status));req.setAttribute("departments",dao.departments());req.setAttribute("q",q);req.setAttribute("selectedStatus",status);req.setAttribute("selectedDepartment",department);
            NotificationDAO n=new NotificationDAO();req.setAttribute("currentUser",user);req.setAttribute("recentNotifications",n.findRecent(user.getId(),5));req.setAttribute("unreadNotificationCount",n.unreadCount(user.getId()));
            req.getRequestDispatcher("/WEB-INF/views/mentor-management.jsp").forward(req,resp);
        }catch(SQLException e){throw new ServletException("Không thể tải danh sách mentor.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8");User user=(User)req.getSession().getAttribute("user");
        try{
            String name=value(req,"fullName"),email=value(req,"email").toLowerCase();Long department=id(req.getParameter("departmentId"));
            if(name.length()<2||name.length()>150)throw new IllegalArgumentException("Họ tên phải từ 2 đến 150 ký tự.");
            if(email.length()>254||!EMAIL.matcher(email).matches())throw new IllegalArgumentException("Email không đúng định dạng.");
            if(department==null)throw new IllegalArgumentException("Vui lòng chọn phòng ban.");
            dao.create(name,email,department,user.getId(),req.getRemoteAddr());resp.sendRedirect(req.getContextPath()+"/mentors?success=created");
        }catch(IllegalArgumentException e){resp.sendRedirect(req.getContextPath()+"/mentors?error="+URLEncoder.encode(e.getMessage(),StandardCharsets.UTF_8));}
        catch(SQLException e){throw new ServletException("Không thể thêm mentor.",e);}
    }
    private String value(HttpServletRequest r,String n){String v=r.getParameter(n);return v==null?"":v.trim();}
    private Long id(String v){try{long x=Long.parseLong(v);return x>0?x:null;}catch(Exception e){return null;}}
}
