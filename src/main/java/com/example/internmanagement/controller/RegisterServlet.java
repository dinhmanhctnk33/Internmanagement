package com.example.internmanagement.controller;

import com.example.internmanagement.util.DBConnection;
import com.example.internmanagement.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.UUID;

@WebServlet({"/register","/candidate-files/*"})
@MultipartConfig(maxFileSize=8*1024*1024,maxRequestSize=9*1024*1024)
public class RegisterServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        if(req.getServletPath().startsWith("/candidate-files")){serveCv(req,resp);return;}
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");Path stored=null;
        try{
            String fullName=required(req,"fullName"),email=required(req,"email").toLowerCase(),password=required(req,"password"),confirm=required(req,"confirmPassword");
            String phone=value(req,"phone"),university=required(req,"university"),major=required(req,"major"),position=required(req,"desiredPosition");
            if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalArgumentException("Email không hợp lệ.");
            if(password.length()<8)throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
            if(!password.equals(confirm))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
            Part cv=req.getPart("cvFile");if(cv==null||cv.getSize()==0)throw new IllegalArgumentException("Vui lòng tải lên CV.");
            String original=Paths.get(cv.getSubmittedFileName()).getFileName().toString();if(!original.toLowerCase().endsWith(".pdf"))throw new IllegalArgumentException("CV phải là tệp PDF.");
            Path dir=Paths.get(System.getProperty("catalina.base",System.getProperty("java.io.tmpdir")),"candidate-uploads");Files.createDirectories(dir);
            stored=dir.resolve(UUID.randomUUID()+".pdf");try(InputStream in=cv.getInputStream()){Files.copy(in,stored,StandardCopyOption.REPLACE_EXISTING);}
            String cvUrl="/candidate-files/"+stored.getFileName();
            try(Connection c=DBConnection.getConnection()){
                c.setAutoCommit(false);try{
                    int roleId;try(PreparedStatement s=c.prepareStatement("SELECT id FROM roles WHERE role_code='CANDIDATE'" );ResultSet rs=s.executeQuery()){
                        if(rs.next())roleId=rs.getInt(1);else{try(PreparedStatement i=c.prepareStatement("INSERT INTO roles(role_code,role_name,description) VALUES('CANDIDATE','Ứng viên','Ứng viên đăng ký trực tuyến')",Statement.RETURN_GENERATED_KEYS)){i.executeUpdate();try(ResultSet keys=i.getGeneratedKeys()){keys.next();roleId=keys.getInt(1);}}}
                    }
                    int userId;try(PreparedStatement s=c.prepareStatement("INSERT INTO users(username,password,email,full_name,status,create_at,update_at,phone_number) VALUES(?,?,?,?, 'ACTIVE',CURDATE(),CURDATE(),?)",Statement.RETURN_GENERATED_KEYS)){
                        s.setString(1,email);s.setString(2,PasswordUtil.hash(password));s.setString(3,email);s.setString(4,fullName);s.setString(5,phone);s.executeUpdate();try(ResultSet keys=s.getGeneratedKeys()){keys.next();userId=keys.getInt(1);}
                    }
                    try(PreparedStatement s=c.prepareStatement("INSERT INTO user_roles(user_id,role_id) VALUES(?,?)")){s.setInt(1,userId);s.setInt(2,roleId);s.executeUpdate();}
                    try(PreparedStatement s=c.prepareStatement("INSERT INTO candidates(full_name,email,phone,university_name,major_name,desired_position,cv_file_url,application_status,applied_at) VALUES(?,?,?,?,?,?,?,'NEW',NOW())")){
                        s.setString(1,fullName);s.setString(2,email);s.setString(3,phone);s.setString(4,university);s.setString(5,major);s.setString(6,position);s.setString(7,cvUrl);s.executeUpdate();
                    }c.commit();
                }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
            }
            resp.sendRedirect(req.getContextPath()+"/login?registered=1");
        }catch(Exception e){if(stored!=null)Files.deleteIfExists(stored);String message=e instanceof SQLIntegrityConstraintViolationException?"Email đã được đăng ký.":e.getMessage();req.setAttribute("error",message);req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req,resp);}
    }
    private String required(HttpServletRequest r,String n){String v=value(r,n);if(v.isBlank())throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc.");return v;}
    private String value(HttpServletRequest r,String n){String v=r.getParameter(n);return v==null?"":v.trim();}
    private void serveCv(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{
        String info=req.getPathInfo();if(info==null||!info.matches("/[0-9a-fA-F-]{36}\\.pdf")){resp.sendError(404);return;}
        String role=String.valueOf(req.getSession().getAttribute("userRole"));
        if("CANDIDATE".equalsIgnoreCase(role)){
            com.example.internmanagement.model.User user=(com.example.internmanagement.model.User)req.getSession().getAttribute("user");
            String sql="SELECT 1 FROM candidates WHERE LOWER(email)=LOWER(?) AND SUBSTRING_INDEX(cv_file_url,'/',-1)=?";
            try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,user.getEmail());s.setString(2,info.substring(1));try(ResultSet rs=s.executeQuery()){if(!rs.next()){resp.sendError(403);return;}}}catch(SQLException e){throw new ServletException("Không thể kiểm tra quyền xem CV.",e);}
        }
        Path dir=Paths.get(System.getProperty("catalina.base",System.getProperty("java.io.tmpdir")),"candidate-uploads");Path file=dir.resolve(info.substring(1)).normalize();
        if(!file.startsWith(dir)||!Files.isRegularFile(file)){resp.sendError(404);return;}resp.setContentType("application/pdf");resp.setHeader("Content-Disposition","inline; filename=cv.pdf");resp.setContentLengthLong(Files.size(file));Files.copy(file,resp.getOutputStream());
    }
}
