package com.example.internmanagement.controller;

import com.example.internmanagement.dao.*;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet("/intern/weekly-reports")
public class InternWeeklyReportServlet extends HttpServlet{
 private final InternWorkDAO dao=new InternWorkDAO();
 protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{User u=intern(q,s);if(u==null)return;try{Long id=dao.findActiveProfileId(u.getId());q.setAttribute("currentUser",u);q.setAttribute("reports",id==null?java.util.List.of():dao.reports(id));NotificationDAO n=new NotificationDAO();q.setAttribute("recentNotifications",n.findRecent(u.getId(),5));q.setAttribute("unreadNotificationCount",n.unreadCount(u.getId()));q.getRequestDispatcher("/WEB-INF/views/intern-weekly-reports.jsp").forward(q,s);}catch(SQLException e){throw new ServletException("Không thể tải báo cáo tuần.",e);}}
 protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException{q.setCharacterEncoding("UTF-8");User u=intern(q,s);if(u==null)return;try{Long id=dao.findActiveProfileId(u.getId());if(id==null)throw new IllegalArgumentException("Không tìm thấy kỳ thực tập đang hoạt động.");int week=Integer.parseInt(q.getParameter("week"));LocalDate start=LocalDate.parse(q.getParameter("startDate")),end=LocalDate.parse(q.getParameter("endDate"));String work=clean(q.getParameter("completedWork")),learn=clean(q.getParameter("keyLearnings")),obstacles=clean(q.getParameter("obstacles")),attachment=clean(q.getParameter("attachmentUrl"));if(week<1||week>53||end.isBefore(start)||work.length()<10||work.length()>4000||learn.length()>4000||obstacles.length()>4000||attachment.length()>500)throw new IllegalArgumentException("Thông tin báo cáo chưa hợp lệ.");dao.saveWeeklyReport(id,week,start,end,work,learn,obstacles,attachment);s.sendRedirect(q.getContextPath()+"/intern/weekly-reports?success=submitted");}catch(IllegalArgumentException e){s.sendRedirect(q.getContextPath()+"/intern/weekly-reports?error="+java.net.URLEncoder.encode(e.getMessage(),java.nio.charset.StandardCharsets.UTF_8));}catch(SQLException e){throw new ServletException("Không thể nộp báo cáo tuần.",e);}}
 private User intern(HttpServletRequest r,HttpServletResponse p)throws IOException{Object x=r.getSession().getAttribute("user");String role=(String)r.getSession().getAttribute("userRole");if(!(x instanceof User u)){p.sendRedirect(r.getContextPath()+"/login");return null;}if(!"INTERN".equalsIgnoreCase(role==null?u.getRoleCode():role)){p.sendError(403);return null;}return u;}private String clean(String s){return s==null?"":s.trim();}
}
