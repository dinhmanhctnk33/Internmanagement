<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String resetError = (String) request.getAttribute("error");
    boolean canResetPassword = resetError == null;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/style.css">
</head>
<body class="min-h-screen flex items-center justify-center p-4">
    <main class="bg-white rounded-xl shadow-2xl overflow-hidden max-w-4xl w-full flex flex-col md:flex-row min-h-[560px]">
        <section class="bg-brand-dark md:w-5/12 p-8 text-white flex flex-col justify-between relative overflow-hidden">
            <div class="z-10">
                <div class="flex items-center space-x-3 mb-8">
                    <div class="w-10 h-10 bg-white/10 rounded-lg flex items-center justify-center font-bold text-xl border border-white/20">IMS</div>
                    <div>
                        <span class="font-bold text-base tracking-wide uppercase block">IMS Portal</span>
                        <span class="text-[10px] text-blue-200 tracking-wider uppercase block">Internship Management</span>
                    </div>
                </div>

                <h1 class="text-2xl font-bold mb-3 leading-snug">Đặt lại mật khẩu</h1>
                <p class="text-blue-200 text-sm leading-relaxed">
                    Tạo mật khẩu mới để tiếp tục sử dụng hệ thống quản lý thực tập sinh.
                </p>
            </div>

            <div class="z-10 text-xs text-blue-200 leading-relaxed">
                <p class="font-semibold text-white mb-1">Lưu ý bảo mật</p>
                <p>Hãy dùng mật khẩu có ít nhất 6 ký tự và không chia sẻ liên kết này với người khác.</p>
            </div>
            <div class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/5 rounded-full blur-xl pointer-events-none"></div>
        </section>

        <section class="bg-white md:w-7/12 p-8 md:p-10 flex flex-col justify-center">
            <div class="mb-6">
                <p class="text-sm font-semibold text-action-accent mb-2">Bảo mật tài khoản</p>
                <h2 class="text-2xl font-bold text-brand-dark">Thiết lập mật khẩu mới</h2>
                <p class="text-gray-500 text-sm mt-2">Nhập mật khẩu mới và xác nhận để hoàn tất.</p>
            </div>

            <% if (resetError != null) { %>
                <div class="mb-5 p-3 rounded-lg border alert-danger text-sm flex items-start">
                    <svg class="w-5 h-5 mr-2 mt-0.5 shrink-0 fill-current" viewBox="0 0 20 20" aria-hidden="true"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11v4a1 1 0 11-2 0V7a1 1 0 112 0zm-1 7a1.25 1.25 0 100-2.5A1.25 1.25 0 0010 14z"/></svg>
                    <span><%= resetError %></span>
                </div>
            <% } %>

            <% if (canResetPassword) { %>
                <form action="${pageContext.request.contextPath}/reset-password" method="post" class="space-y-4">
                    <input type="hidden" name="token" value="${param.token}">

                    <div>
                        <label for="newPassword" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">Mật khẩu mới</label>
                        <div class="relative">
                            <input id="newPassword" type="password" name="newPassword" minlength="6" required autocomplete="new-password"
                                   placeholder="Tối thiểu 6 ký tự"
                                   class="w-full px-4 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 placeholder-gray-400 input-focus transition duration-150">
                        </div>
                    </div>

                    <div>
                        <label for="confirmPassword" class="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">Xác nhận mật khẩu</label>
                        <input id="confirmPassword" type="password" name="confirmPassword" minlength="6" required autocomplete="new-password"
                               placeholder="Nhập lại mật khẩu mới"
                               class="w-full px-4 py-2.5 bg-gray-50 border border-gray-300 rounded-lg text-sm text-gray-800 placeholder-gray-400 input-focus transition duration-150">
                    </div>

                    <div class="pt-2">
                        <button type="submit" class="w-full py-3 px-4 bg-action-accent text-white font-semibold text-sm rounded-lg shadow-md hover:shadow-lg focus:outline-none transition duration-150">
                            Đặt lại mật khẩu
                        </button>
                    </div>
                </form>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/forgot-password" class="w-full inline-flex justify-center items-center py-3 px-4 bg-action-accent text-white font-semibold text-sm rounded-lg shadow-md hover:shadow-lg transition duration-150">
                    Gửi yêu cầu mới
                </a>
            <% } %>

            <div class="text-center pt-6">
                <a href="${pageContext.request.contextPath}/login" class="text-xs font-semibold text-action-accent hover:underline inline-flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
                    Quay lại đăng nhập
                </a>
            </div>
        </section>
    </main>
</body>
</html>
