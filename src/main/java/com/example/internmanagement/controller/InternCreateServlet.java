package com.example.internmanagement.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/interns/create")
public class InternCreateServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Kiểm tra đăng nhập
        Object user = request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login.jsp"
            );
            return;
        }

        // Thông tin giao diện
        request.setAttribute(
                "pageTitle",
                "Thêm mới và nhập hồ sơ thực tập sinh"
        );

        request.setAttribute(
                "screenCode",
                "UI-05"
        );

        request.setAttribute(
                "activeScreen",
                "intern-create"
        );

        // Xóa thông báo cũ nếu có
        request.removeAttribute("successMessage");
        request.removeAttribute("errorMessage");

        // Hiển thị form
        request.getRequestDispatcher(
                "/WEB-INF/views/intern/intern-create.jsp"
        ).forward(request, response);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // Kiểm tra đăng nhập
        Object user = request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login.jsp"
            );
            return;
        }

        /*
         * Nhận dữ liệu từ form
         */
        String personalInfo =
                request.getParameter("personalInfo");

        String educationInfo =
                request.getParameter("educationInfo");

        String excelFile =
                request.getParameter("excelFile");

        String templateFile =
                request.getParameter("templateFile");

        String note =
                request.getParameter("note");


        /*
         * Chuẩn hóa dữ liệu
         */
        if (personalInfo != null) {
            personalInfo = personalInfo.trim();
        }

        if (educationInfo != null) {
            educationInfo = educationInfo.trim();
        }

        if (excelFile != null) {
            excelFile = excelFile.trim();
        }

        if (templateFile != null) {
            templateFile = templateFile.trim();
        }

        if (note != null) {
            note = note.trim();
        }


        /*
         * Kiểm tra dữ liệu bắt buộc
         */
        if (personalInfo == null || personalInfo.isEmpty()) {

            request.setAttribute(
                    "errorMessage",
                    "Vui lòng nhập thông tin cá nhân."
            );

            request.setAttribute(
                    "personalInfo",
                    personalInfo
            );

            request.setAttribute(
                    "educationInfo",
                    educationInfo
            );

            request.setAttribute(
                    "note",
                    note
            );

            setPageAttributes(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/intern/intern-create.jsp"
            ).forward(request, response);

            return;
        }


        if (educationInfo == null || educationInfo.isEmpty()) {

            request.setAttribute(
                    "errorMessage",
                    "Vui lòng nhập thông tin học vấn."
            );

            request.setAttribute(
                    "personalInfo",
                    personalInfo
            );

            request.setAttribute(
                    "educationInfo",
                    educationInfo
            );

            request.setAttribute(
                    "note",
                    note
            );

            setPageAttributes(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/intern/intern-create.jsp"
            ).forward(request, response);

            return;
        }


        /*
         * ------------------------------------------------
         * TODO:
         * Sau khi xác định nghiệp vụ tạo TTS,
         * dữ liệu sẽ được lưu vào:
         *
         * users
         * intern_profiles
         * ------------------------------------------------
         *
         * Ví dụ:
         *
         * UserDAO.createUser(...)
         * InternProfileDAO.createInternProfile(...)
         *
         * Hiện tại chưa gọi DAO vì form UI-05
         * chưa có các field thực tế tương ứng
         * với InternProfile.
         */


        /*
         * Thông báo thành công tạm thời
         */
        request.setAttribute(
                "successMessage",
                "Thông tin thực tập sinh đã được tiếp nhận."
        );


        /*
         * Giữ lại dữ liệu để hiển thị
         */
        request.setAttribute(
                "personalInfo",
                personalInfo
        );

        request.setAttribute(
                "educationInfo",
                educationInfo
        );

        request.setAttribute(
                "note",
                note
        );

        setPageAttributes(request);


        /*
         * Forward lại trang
         */
        request.getRequestDispatcher(
                "/WEB-INF/views/intern/intern-create.jsp"
        ).forward(request, response);
    }


    /**
     * Thiết lập các attribute dùng chung cho UI-05
     */
    private void setPageAttributes(
            HttpServletRequest request) {

        request.setAttribute(
                "pageTitle",
                "Thêm mới và nhập hồ sơ thực tập sinh"
        );

        request.setAttribute(
                "screenCode",
                "UI-05"
        );

        request.setAttribute(
                "activeScreen",
                "intern-create"
        );
    }
}