package com.example.internmanagement.controller;

import com.example.internmanagement.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet({"/reset-password", "/ResetPasswordServlet"})
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ============================================================
    // GET
    // ============================================================
    // Khi người dùng truy cập:
    //
    // /reset-password?token=abc123
    //
    // Servlet sẽ kiểm tra token trước khi hiển thị form.
    // ============================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");

        // Không có token
        if (token == null || token.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Liên kết đặt lại mật khẩu không hợp lệ."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }

        // Kiểm tra token
        if (!isValidToken(token)) {

            request.setAttribute(
                    "error",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }

        // Token hợp lệ
        // Hiển thị form reset password
        request.getRequestDispatcher(
                "/WEB-INF/views/reset-password.jsp"
        ).forward(request, response);
    }


    // ============================================================
    // POST
    // ============================================================
    // Nhận:
    // token
    // newPassword
    // confirmPassword
    //
    // Sau đó cập nhật mật khẩu.
    // ============================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");

        String newPassword =
                request.getParameter("newPassword");

        String confirmPassword =
                request.getParameter("confirmPassword");


        // ========================================================
        // 1. Kiểm tra dữ liệu
        // ========================================================

        if (token == null || token.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Token không hợp lệ."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        if (newPassword == null ||
            newPassword.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Vui lòng nhập mật khẩu mới."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        if (confirmPassword == null ||
            confirmPassword.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Vui lòng xác nhận mật khẩu."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        // ========================================================
        // 2. Kiểm tra hai mật khẩu có giống nhau không
        // ========================================================

        if (!newPassword.equals(confirmPassword)) {

            request.setAttribute(
                    "error",
                    "Mật khẩu xác nhận không khớp."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        // ========================================================
        // 3. Kiểm tra độ dài mật khẩu
        // ========================================================

        if (newPassword.length() < 6) {

            request.setAttribute(
                    "error",
                    "Mật khẩu phải có ít nhất 6 ký tự."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        // ========================================================
        // 4. Kiểm tra token
        // ========================================================

        if (!isValidToken(token)) {

            request.setAttribute(
                    "error",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);

            return;
        }


        // ========================================================
        // 5. Đổi mật khẩu
        // ========================================================

        boolean updated = updatePassword(
                token,
                newPassword
        );


        if (updated) {

            // Đổi mật khẩu thành công
            response.sendRedirect(
                    request.getContextPath()
                    + "/login?reset=success"
            );

        } else {

            request.setAttribute(
                    "error",
                    "Không thể đặt lại mật khẩu. Vui lòng thử lại."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset-password.jsp"
            ).forward(request, response);
        }
    }


    // ============================================================
    // KIỂM TRA TOKEN
    // ============================================================

    private boolean isValidToken(String token) {

        String sql =
                "SELECT id " +
                "FROM users " +
                "WHERE act_token = ? " +
                "AND ex_date_at >= CURDATE()";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, token);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ============================================================
    // CẬP NHẬT MẬT KHẨU
    // ============================================================

    private boolean updatePassword(
            String token,
            String newPassword) {

        String sql =
                "UPDATE users " +
                "SET password = ?, " +
                "act_token = NULL, " +
                "ex_date_at = NULL " +
                "WHERE act_token = ? " +
                "AND ex_date_at >= CURDATE()";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, newPassword);
            statement.setString(2, token);

            int rowsUpdated =
                    statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }
}

