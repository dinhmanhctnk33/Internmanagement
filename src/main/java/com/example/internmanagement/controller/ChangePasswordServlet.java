package com.example.internmanagement.controller;

import com.example.internmanagement.dao.UserDAO;
import com.example.internmanagement.model.User;
import com.example.internmanagement.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = value(request, "currentPassword");
        String newPassword = value(request, "newPassword");
        String confirmPassword = value(request, "confirmPassword");
        if (!PasswordUtil.matches(currentPassword, user.getPassword())) {
            showForm(request, response, "Mật khẩu hiện tại không chính xác.");
            return;
        }
        if (newPassword.length() < 6) {
            showForm(request, response, "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showForm(request, response, "Nhập lại mật khẩu mới không khớp.");
            return;
        }
        if (newPassword.equals(currentPassword)) {
            showForm(request, response, "Mật khẩu mới cần khác mật khẩu hiện tại.");
            return;
        }
        if (!userDAO.updatePassword(user.getId(), newPassword)) {
            showForm(request, response, "Không thể đổi mật khẩu. Vui lòng thử lại.");
            return;
        }
        user.setPassword(PasswordUtil.hash(newPassword));
        request.getSession().setAttribute("user", user);
        request.getSession().setAttribute("currentUser", user);
        response.sendRedirect(request.getContextPath() + "/change-password?success=1");
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/change-password.jsp").forward(request, response);
    }
}
