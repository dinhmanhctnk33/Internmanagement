<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - Hệ thống Quản lý Thực tập sinh</title>
    
    <!-- Link tới file CSS riêng -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/style.css">
</head>
<body class="min-h-screen flex items-center justify-center p-4">

    <!-- Container Card (Split Screen Layout) -->
    <div class="bg-white rounded-xl shadow-2xl overflow-hidden max-w-4xl w-full flex flex-col md:flex-row min-h-[560px]">
        
        <!-- Left Column: Brand & Banner (#1B365D) -->
        <div class="bg-brand-dark md:w-5/12 p-8 text-white flex flex-col justify-between relative overflow-hidden">
            <div class="z-10">
                <!-- Logo & Brand Header -->
                <div class="flex items-center space-x-3 mb-8">
                    <div class="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-bold text-xl border border-white/20">
                        🎓
                    </div>
                    <div>
                        <span class="font-bold text-base tracking-wide uppercase block">IMS Portal</span>
                        <span class="text-[10px] text-blue-200 tracking-wider uppercase block">Internship Management</span>
                    </div>
                </div>
                
                <h1 class="text-2xl font-bold mb-3 leading-snug">Cổng Thông tin Thực tập sinh</h1>
                <p class="text-blue-200 text-sm leading-relaxed">
                    Kết nối Sinh viên, Nhà trường và Doanh nghiệp. Quản lý tiến độ, báo cáo thực tập và đánh giá hiệu quả tập trung.
                </p>
            </div>

            <!-- Visual Decorative Blur -->
            <div class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/5 rounded-full blur-xl pointer-events-none"></div>

            <div class="z-10 text-xs text-blue-300">
                &copy; 2026 IMS System. Phiên bản v2.4.0
            </div>
        </div>

        <!-- Right Column: Login Form (#FFFFFF) -->
        <div class="bg-white md:w-7/12 p-8 md:p-10 flex flex-col justify-center">
            
            <div class="mb-5">
                <h2 class="text-2xl font-bold text-brand-dark">Đăng nhập hệ thống</h2>
                <p class="text-gray-500 text-sm mt-1">Vui lòng chọn vai trò và nhập thông tin tài khoản</p>
            </div>

            <%-- Hiển thị thông báo phản hồi từ Controller/Servlet --%>
            <% 
                String errorMessage = (String) request.getAttribute("errorMessage");
                String warningMessage = (String) request.getAttribute("warningMessage");
                String successMessage = (String) request.getAttribute("successMessage");

                if (errorMessage != null) { 
            %>
                <div class="mb-4 p-3 rounded-lg border alert-danger text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"/></svg>
                    <span><%= errorMessage %></span>
                </div>
            <% } else if (warningMessage != null) { %>
                <div class="mb-4 p-3 rounded-lg border alert-warning text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/></svg>
                    <span><%= warningMessage %></span>
                </div>
            <% } else if (successMessage != null) { %>
                <div class="mb-4 p-3 rounded-lg border alert-success text-sm flex items-center">
                    <svg class="w-5 h-5 mr-2 shrink-0 fill-current" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/></svg>
                    <span><%= successMessage %></span>
                </div>
            <% } %>

            <!-- Form xử lý gửi dữ liệu sang Servlet/Controller -->
            <form action="${pageContext.request.contextPath}/login" method="post" class="space-y-4">
                
                <!-- Field: Select Role -->
                <div>
                    <label for="role" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                        Dành cho
                    </label>
                    <div class="relative">
                        <select id="role" name="role" required
                            class="w-full pl-3 pr-8 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 input-focus transition duration-150 appearance-none cursor-pointer">
                            <option value="INTERN" ${param.role == 'INTERN' ? 'selected' : ''}>Thực tập sinh (Sinh viên)</option>
                            <option value="MENTOR" ${param.role == 'MENTOR' ? 'selected' : ''}>Cán bộ Hướng dẫn (Mentor / Doanh nghiệp)</option>
                            <option value="ADMIN" ${param.role == 'ADMIN' ? 'selected' : ''}>Quản trị viên</option>
                            <option value="HR" ${param.role == 'HR' ? 'selected' : ''}>Phòng nhân sự</option>
                        </select>
                        <div class="absolute inset-y-0 right-0 flex items-center px-3 pointer-events-none text-gray-500">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
                        </div>
                    </div>
                </div>

                <!-- Field: Mã thực tập sinh / Email -->
                <div>
                    <label for="username" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                        Mã SV / Email / Tên đăng nhập
                    </label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                        </div>
                        <input type="text" id="username" name="username" required
                            value="${param.username}"
                            placeholder="Mã số sinh viên hoặc Email" 
                            class="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 placeholder-gray-400 input-focus transition duration-150">
                    </div>
                </div>

                <!-- Field: Password -->
                <div>
                    <label for="password" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                        Mật khẩu
                    </label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                        </div>
                        <input type="password" id="password" name="password" required
                            placeholder="••••••••" 
                            class="w-full pl-10 pr-10 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 placeholder-gray-400 input-focus transition duration-150">
                    </div>
                </div>

                <!-- Checkbox & Forgot Password -->
                <div class="flex items-center justify-between text-xs pt-1">
                    <label class="flex items-center text-gray-600 cursor-pointer">
                        <input type="checkbox" name="rememberMe" class="w-4 h-4 border-gray-300 rounded checkbox-custom">
                        <span class="ml-2">Ghi nhớ đăng nhập</span>
                    </label>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="text-action-accent font-semibold hover:underline">
                        Quên mật khẩu?
                    </a>
                </div>

                <!-- Submit Button (#2C5282) -->
                <div class="pt-2">
                            <button type="submit"
                        class="w-full py-3 px-4 bg-action-accent text-white font-semibold text-sm rounded-lg shadow-md hover:shadow-lg focus:outline-none transition duration-150">
                        ĐĂNG NHẬP
                            </button>
                            <p class="text-center text-sm text-slate-500">Chưa có tài khoản? <a class="font-semibold text-blue-700" href="${pageContext.request.contextPath}/register">Đăng ký ứng viên</a></p>
                </div>
            </form>

        </div>
    </div>

</body>
</html>
