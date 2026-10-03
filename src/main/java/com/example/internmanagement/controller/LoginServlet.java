package com.example.internmanagement.controller;

import com.example.internmanagement.dao.UserDAO;
import com.example.internmanagement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    // Hiển thị trang login
    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if ("success".equals(request.getParameter("reset"))) {
            request.setAttribute("successMessage", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
               .forward(request, response);
    }

    // Xử lý login
    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Kiểm tra dữ liệu nhập
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            request.setAttribute("errorMessage",
                    "Vui lòng nhập đầy đủ username và password.");

            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);

            return;
        }

        // Kiểm tra tài khoản
        User user = userDAO.login(username, password);

        if (user != null) {
            String roleCode = userDAO.getUserRoleCode(user.getId());
            user.setRole(roleCode);

            // Tạo session
            HttpSession session = request.getSession();

            // Lưu user vào session
            session.setAttribute("user", user);
            session.setAttribute("currentUser", user);
            session.setAttribute("userRole", roleCode);

            // Thời gian session: 30 phút
            session.setMaxInactiveInterval(30 * 60);

            // Chuyển đến trang chủ
            response.sendRedirect(request.getContextPath() + "/home");

        } else {

            request.setAttribute("errorMessage",
                    "Username hoặc password không chính xác.");

            request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                   .forward(request, response);
        }
    }
}
