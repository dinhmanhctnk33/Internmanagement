package com.example.internmanagement.controller;

import com.example.internmanagement.dao.InternWorkDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/intern/progress")
public class InternProgressServlet extends HttpServlet {
    private final InternWorkDAO dao=new InternWorkDAO();
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{User u=intern(req,resp);if(u==null)return;try{Long id=dao.findActiveProfileId(u.getId());req.setAttribute("currentUser",u);req.setAttribute("tasks",id==null?java.util.List.of():dao.tasks(id));NotificationDAO n=new NotificationDAO();req.setAttribute("recentNotifications",n.findRecent(u.getId(),5));req.setAttribute("unreadNotificationCount",n.unreadCount(u.getId()));req.getRequestDispatcher("/WEB-INF/views/intern-progress.jsp").forward(req,resp);}catch(SQLException e){throw new ServletException("Không thể tải tiến độ công việc.",e);}}
    protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{req.setCharacterEncoding("UTF-8");User u=intern(req,resp);if(u==null)return;try{Long id=dao.findActiveProfileId(u.getId());if(id==null)throw new IllegalArgumentException("Không tìm thấy kỳ thực tập đang hoạt động.");long task=Long.parseLong(req.getParameter("taskId"));int percent=Integer.parseInt(req.getParameter("percent"));String status=req.getParameter("status");String note=clean(req.getParameter("note"));if(percent<0||percent>100||note.length()>500||!java.util.Set.of("TODO","IN_PROGRESS","COMPLETED").contains(status))throw new IllegalArgumentException("Dữ liệu tiến độ không hợp lệ.");if(percent==100)status="COMPLETED";dao.updateProgress(id,task,percent,status,note);resp.sendRedirect(req.getContextPath()+"/intern/progress?success=updated");}catch(IllegalArgumentException e){resp.sendRedirect(req.getContextPath()+"/intern/progress?error="+java.net.URLEncoder.encode(e.getMessage(),java.nio.charset.StandardCharsets.UTF_8));}catch(SQLException e){throw new ServletException("Không thể cập nhật tiến độ.",e);}}
    private User intern(HttpServletRequest r,HttpServletResponse p)throws IOException{Object x=r.getSession().getAttribute("user");String role=(String)r.getSession().getAttribute("userRole");if(!(x instanceof User u)){p.sendRedirect(r.getContextPath()+"/login");return null;}if(!"INTERN".equalsIgnoreCase(role==null?u.getRoleCode():role)){p.sendError(403);return null;}return u;}private String clean(String s){return s==null?"":s.trim();}
}
