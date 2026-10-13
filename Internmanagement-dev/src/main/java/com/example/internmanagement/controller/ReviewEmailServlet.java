package com.example.internmanagement.controller;

import com.example.internmanagement.dao.ReviewEmailQueueDAO;
import com.example.internmanagement.util.EmailUtility;
import com.example.internmanagement.util.ReviewEmailProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/review-emails")
public class ReviewEmailServlet extends HttpServlet {
    private final ReviewEmailQueueDAO dao = new ReviewEmailQueueDAO();

    @Override public void init() throws ServletException {
        try { dao.ensureSchema(); } catch (Exception error) { throw new ServletException(error); }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("jobs", dao.findAll());
            request.setAttribute("smtpConfigured", EmailUtility.isConfigured());
            request.getRequestDispatcher("/WEB-INF/views/review-emails.jsp").forward(request, response);
        } catch (Exception error) { throw new ServletException("Không thể tải hàng đợi email", error); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int sent = new ReviewEmailProcessor().processDue();
            response.sendRedirect(request.getContextPath() + "/review-emails?processed=" + sent);
        } catch (Exception error) {
            response.sendRedirect(request.getContextPath() + "/review-emails?error=1");
        }
    }
}
