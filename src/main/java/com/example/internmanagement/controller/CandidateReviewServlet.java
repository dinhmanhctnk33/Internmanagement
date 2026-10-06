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
                    if(emails.enqueue(c,candidateId,decision,reason,LocalDateTime.now())==0)throw new IllegalStateException("Ứng viên chưa có địa chỉ email.");
                    c.commit();
                }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
            }
            int sent=0;if(EmailUtility.isConfigured())sent=new ReviewEmailProcessor().processDue();
            resp.sendRedirect(req.getContextPath()+"/applications?result="+(sent>0?"sent":"queued"));
        }catch(Exception e){resp.sendRedirect(req.getContextPath()+"/applications?error="+java.net.URLEncoder.encode(e.getMessage(),java.nio.charset.StandardCharsets.UTF_8));}
    }
    private String clean(String value){return value==null?"":value.trim();}
}
