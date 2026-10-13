<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - Hệ thống Quản lý Thực tập sinh</title>
    
    <!-- CSS dùng chung của hệ thống -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/style.css">
</head>
<body class="min-h-screen flex items-center justify-center p-4">

    <!-- Container Card (Split Screen Layout) -->
    <div class="bg-white rounded-xl shadow-2xl overflow-hidden max-w-4xl w-full flex flex-col md:flex-row min-h-[560px]">
        
        <!-- Cột Trái: Brand Banner (#1B365D) -->
        <div class="bg-brand-dark md:w-5/12 p-8 text-white flex flex-col justify-between relative overflow-hidden">
            <div class="z-10">
                <!-- Logo & Header -->
                <div class="flex items-center space-x-3 mb-8">
                    <div class="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-bold text-xl border border-white/20">
                        🎓
                    </div>
                    <div>
                        <span class="font-bold text-base tracking-wide uppercase block">IMS Portal</span>
                        <span class="text-[10px] text-blue-200 tracking-wider uppercase block">Internship Management</span>
                    </div>
                </div>
                
                <h1 class="text-2xl font-bold mb-3 leading-snug">Khôi phục mật khẩu</h1>
                <p class="text-blue-200 text-sm leading-relaxed">
                    Hệ thống hỗ trợ đặt lại mật khẩu an toàn. Vui lòng làm theo các hướng dẫn để xác minh tài khoản của bạn.
                </p>
            </div>

            <!-- Visual Blur -->
            <div class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/5 rounded-full blur-xl pointer-events-none"></div>

            <div class="z-10 text-xs text-blue-200">
                Cần trợ giúp trực tiếp? <br>
                <span class="text-white font-medium">Email: support@university.edu.vn</span>
            </div>
        </div>

        <!-- Cột Phải: Form Quên mật khẩu (#FFFFFF) -->
        <div class="bg-white md:w-7/12 p-8 md:p-10 flex flex-col justify-center">
            
            <div class="mb-5">
                <h2 class="text-2xl font-bold text-brand-dark">Quên mật khẩu?</h2>
                <p class="text-gray-500 text-sm mt-1">Nhập thông tin tài khoản để nhận mã khôi phục OTP</p>
            </div>

            <%-- Hiển thị thông báo phản hồi từ Servlet/Controller --%>
            <% 
                String errorMessage = (String) request.getAttribute("error");
                String warningMessage = (String) request.getAttribute("warningMessage");
                String successMessage = (String) request.getAttribute("successMessage");

                if (errorMessage != null) { 
            %>
                <!-- Danger Badge Color (#E53E3E) -->
                <div class="mb-4 p-3 rounded-lg border alert-danger text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"/></svg>
                    <span><%= errorMessage %></span>
                </div>
            <% } else if (warningMessage != null) { %>
                <!-- Warning Badge Color (#DD6B20) -->
                <div class="mb-4 p-3 rounded-lg border alert-warning text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/></svg>
                    <span><%= warningMessage %></span>
                </div>
            <% } else if (successMessage != null) { %>
                <!-- Success Badge Color (#2F855A) -->
                <div class="mb-4 p-3 rounded-lg border alert-success text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/></svg>
                    <span><%= successMessage %></span>
                </div>
            <% } %>

            <!-- Form gửi dữ liệu sang Servlet xử lý -->
            <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="space-y-4">
                
                <!-- Input Email / Mã Sinh Viên -->
                <div>
                        <label for="email" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                        Mã Sinh Viên / Email đăng ký
                    </label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                        </div>
                        <input type="email" id="email" name="email" required autocomplete="email"
                            value="${param.email}"
                            placeholder="Ví dụ: SV123456 hoặc user@st.edu.vn" 
                            class="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 placeholder-gray-400 input-focus transition duration-150">
                    </div>
                </div>

                <!-- Action Accent Button (#2C5282) -->
                <div class="pt-2">
                    <button type="submit" 
                        class="w-full py-3 px-4 bg-action-accent text-white font-semibold text-sm rounded-lg shadow-md hover:shadow-lg focus:outline-none transition duration-150">
                        GỬI MÃ XÁC MINH (OTP)
                    </button>
                </div>

                <!-- Back to Login Link -->
                <div class="text-center pt-3">
                    <a href="${pageContext.request.contextPath}/login" class="text-xs font-semibold text-action-accent hover:underline inline-flex items-center">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
                        Quay lại trang Đăng nhập
                    </a>
                </div>
            </form>

        </div>
    </div>

</body>
</html>
