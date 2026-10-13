package com.example.internmanagement.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import com.example.internmanagement.dao.UserDAO;
import com.example.internmanagement.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (fullName.isEmpty() || email.isEmpty() || password == null || confirmPassword == null) {
            setError(request, "Vui lòng điền đầy đủ thông tin bắt buộc.");
            forwardRegister(request, response);
            return;
        }

        if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            setError(request, "Email không hợp lệ.");
            forwardRegister(request, response);
            return;
        }

        if (fullName.length() > 100) {
            setError(request, "Họ và tên không được vượt quá 100 ký tự.");
            forwardRegister(request, response);
            return;
        }

        if (password.length() < 8 || password.length() > 255) {
            setError(request, "Mật khẩu phải có từ 8 đến 255 ký tự.");
            forwardRegister(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            setError(request, "Mật khẩu xác nhận không khớp.");
            forwardRegister(request, response);
            return;
        }

        UserDAO userDAO = new UserDAO();
        if (userDAO.existsByEmail(email)) {
            setError(request, "Email này đã được sử dụng. Vui lòng dùng email khác.");
            forwardRegister(request, response);
            return;
        }

        String username = generateUniqueUsername(userDAO, email);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setFull_name(fullName);
        user.setStatus("ACTIVE");
        user.setCreate_at(LocalDate.now());
        user.setUpdate_at(LocalDate.now());
        user.setC_password(false);
        user.setAct_token(null);
        user.setEx_date_at(null);
        user.setPhone_number(null);

        try {
            userDAO.insertWithRole(user, "CANDIDATE");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState()) || "23505".equals(e.getSQLState())) {
                setError(request, "Email này đã được sử dụng. Vui lòng dùng email khác.");
                forwardRegister(request, response);
                return;
            }
            throw new ServletException("Không thể tạo tài khoản ứng viên.", e);
        }

        request.setAttribute("success", "Đăng ký tài khoản thành công. Hãy đăng nhập để hoàn thiện hồ sơ ứng tuyển.");
        request.setAttribute("verifyUrl", request.getContextPath() + "/login");
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String generateUniqueUsername(UserDAO userDAO, String email) {
        String base = sanitizeUsername(email.split("@")[0]);
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String username = base;
        int counter = 1;

        while (userDAO.findByUsername(username) != null) {
            username = base + counter;
            counter++;
        }
        return username;
    }

    private String sanitizeUsername(String raw) {
        String value = raw.toLowerCase();
        value = value.replaceAll("[^a-z0-9]", "");
        if (value.length() < 4) {
            value = "intern" + value;
        }
        return value;
    }

    private void setError(HttpServletRequest request, String message) {
        request.setAttribute("error", message);
    }

    private void forwardRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
