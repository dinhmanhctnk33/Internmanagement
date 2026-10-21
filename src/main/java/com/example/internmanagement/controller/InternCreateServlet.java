package com.example.internmanagement.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Giữ tương thích với URL cũ. Luồng tạo hồ sơ chính thức được xử lý tập trung
 * tại {@code /interns/new} và {@link InternServlet}.
 */
@WebServlet("/interns/create")
public class InternCreateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/interns/new");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/interns/new");
    }
}
