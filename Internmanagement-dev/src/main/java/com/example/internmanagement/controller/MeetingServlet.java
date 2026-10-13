package com.example.internmanagement.controller;

import com.example.internmanagement.dao.MeetingDAO;
import com.example.internmanagement.dao.NotificationDAO;
import com.example.internmanagement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.*;

@WebServlet("/meetings")
public class MeetingServlet extends HttpServlet {
    private final MeetingDAO dao=new MeetingDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User user=(User)req.getSession().getAttribute("user");if(user==null){resp.sendRedirect(req.getContextPath()+"/login");return;}
        String role=role(req);boolean hr=Set.of("HR","ADMIN").contains(role);if(!hr&&!"MENTOR".equals(role)){resp.sendError(403);return;}
        String requestedStatus=clean(req.getParameter("status"));
        String status=Set.of("ACTIVE","CANCELLED").contains(requestedStatus)?requestedStatus:"ALL";
        try{req.setAttribute("meetings",dao.list(user.getId(),hr,status));req.setAttribute("interns",dao.interns(user.getId(),hr));req.setAttribute("selectedStatus",status);req.setAttribute("currentUser",user);NotificationDAO notifications=new NotificationDAO();req.setAttribute("recentNotifications",notifications.findRecent(user.getId(),5));req.setAttribute("unreadNotificationCount",notifications.unreadCount(user.getId()));
            Long edit=parseLong(req.getParameter("edit"));if(edit!=null){Map<String,Object> meeting=dao.find(edit,user.getId(),hr);if(meeting!=null){req.setAttribute("editingMeeting",meeting);req.setAttribute("editingRecipients",dao.recipients(edit));}}
            req.getRequestDispatcher("/WEB-INF/views/meetings.jsp").forward(req,resp);
        }catch(SQLException e){throw new ServletException("Không thể tải lịch họp.",e);}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");User user=(User)req.getSession().getAttribute("user");if(user==null){resp.sendRedirect(req.getContextPath()+"/login");return;}
        String role=role(req);boolean hr=Set.of("HR","ADMIN").contains(role);if(!hr&&!"MENTOR".equals(role)){resp.sendError(403);return;}
        try{String action=clean(req.getParameter("action"));if("cancel".equals(action)){dao.cancel(requiredLong(req.getParameter("meetingId")),user.getId(),requiredInt(req.getParameter("version")),hr);redirect(req,resp,"cancelled");return;}
            Form f=parseForm(req);if("update".equals(action)){long id=requiredLong(req.getParameter("meetingId"));dao.update(id,user.getId(),requiredInt(req.getParameter("version")),f.title,f.date,f.start,f.end,f.mode,f.place,f.description,f.recipients,hr);redirect(req,resp,"updated");}
            else{dao.create(user.getId(),f.title,f.date,f.start,f.end,f.mode,f.place,f.description,f.recipients,hr);redirect(req,resp,"created");}
        }catch(Exception e){String message=e instanceof IllegalArgumentException?e.getMessage():"Không thể lưu lịch họp. Vui lòng thử lại.";resp.sendRedirect(req.getContextPath()+"/meetings?error="+URLEncoder.encode(message,StandardCharsets.UTF_8));}
    }
    private Form parseForm(HttpServletRequest r){String title=clean(r.getParameter("title"));if(title.isBlank()||title.length()>200)throw new IllegalArgumentException("Tiêu đề là bắt buộc và không vượt quá 200 ký tự.");LocalDate date;LocalTime start,end;try{date=LocalDate.parse(clean(r.getParameter("meetingDate")));start=LocalTime.parse(clean(r.getParameter("startTime")));end=LocalTime.parse(clean(r.getParameter("endTime")));}catch(Exception e){throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngày và giờ họp.");}if(!end.isAfter(start))throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");if(!LocalDateTime.of(date,start).isAfter(LocalDateTime.now()))throw new IllegalArgumentException("Không thể tạo lịch họp ở thời điểm đã qua.");String mode=clean(r.getParameter("meetingMode"));if(!Set.of("ONLINE","OFFLINE").contains(mode))throw new IllegalArgumentException("Hình thức họp không hợp lệ.");String place=clean(r.getParameter("locationOrLink"));if(place.isBlank())throw new IllegalArgumentException("Vui lòng nhập "+("ONLINE".equals(mode)?"liên kết họp.":"địa điểm họp."));String[] raw=r.getParameterValues("recipientIds");Set<Long> recipients=new LinkedHashSet<>();if(raw!=null)for(String value:raw){Long id=parseLong(value);if(id!=null)recipients.add(id);}if(recipients.isEmpty())throw new IllegalArgumentException("Vui lòng chọn ít nhất một thực tập sinh.");return new Form(title,date,start,end,mode,place,clean(r.getParameter("description")),recipients);}
    private String role(HttpServletRequest r){Object value=r.getSession().getAttribute("userRole");return value==null?"":value.toString().toUpperCase(Locale.ROOT);}
    private String clean(String value){return value==null?"":value.trim();}private Long parseLong(String v){try{return Long.valueOf(v);}catch(Exception e){return null;}}private long requiredLong(String v){Long id=parseLong(v);if(id==null)throw new IllegalArgumentException("Lịch họp không hợp lệ.");return id;}private int requiredInt(String v){try{return Integer.parseInt(v);}catch(Exception e){throw new IllegalArgumentException("Phiên bản dữ liệu không hợp lệ.");}}
    private void redirect(HttpServletRequest req,HttpServletResponse resp,String success)throws IOException{resp.sendRedirect(req.getContextPath()+"/meetings?success="+success);}
    private record Form(String title,LocalDate date,LocalTime start,LocalTime end,String mode,String place,String description,Set<Long> recipients){}
}
