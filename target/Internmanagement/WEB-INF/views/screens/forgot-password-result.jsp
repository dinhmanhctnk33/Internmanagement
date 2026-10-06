<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khôi phục mật khẩu - IMS Portal</title>
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
                <h1 class="text-2xl font-bold mb-3 leading-snug">Khôi phục mật khẩu</h1>
                <p class="text-blue-200 text-sm leading-relaxed">Chúng tôi giúp bạn khôi phục quyền truy cập tài khoản một cách an toàn.</p>
            </div>
            <div class="z-10 text-xs text-blue-200 leading-relaxed">
                <p class="font-semibold text-white mb-1">Kiểm tra hộp thư</p>
                <p>Nếu chưa thấy email, hãy kiểm tra thư mục Spam hoặc gửi lại yêu cầu sau ít phút.</p>
            </div>
            <div class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/5 rounded-full blur-xl pointer-events-none"></div>
        </section>

        <section class="bg-white md:w-7/12 p-8 md:p-10 flex flex-col justify-center">
            <c:choose>
                <c:when test="${emailSent}">
                    <div class="w-12 h-12 rounded-full bg-green-100 text-green-700 flex items-center justify-center mb-5">
                        <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
                    </div>
                    <p class="text-xs font-semibold text-action-accent uppercase tracking-wider mb-2">Yêu cầu thành công</p>
                    <h2 class="text-2xl font-bold text-brand-dark">Email đã được gửi</h2>
                    <p class="text-gray-500 text-sm mt-3 leading-relaxed">Liên kết đặt lại mật khẩu đã được gửi đến email của bạn. Liên kết có hiệu lực trong 24 giờ.</p>
                </c:when>
                <c:otherwise>
                    <div class="w-12 h-12 rounded-full bg-red-50 text-red-600 flex items-center justify-center mb-5">
                        <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M5.07 19h13.86c1.54 0 2.5-1.67 1.73-3L13.73 4c-.77-1.33-2.69-1.33-3.46 0L3.34 16c-.77 1.33.19 3 1.73 3z"/></svg>
                    </div>
                    <p class="text-xs font-semibold text-red-600 uppercase tracking-wider mb-2">Không thể gửi email</p>
                    <h2 class="text-2xl font-bold text-brand-dark">Yêu cầu đã được tạo</h2>
                    <p class="text-gray-500 text-sm mt-3 leading-relaxed">${emailError}</p>
                    <div class="mt-5 p-3 rounded-lg border alert-warning text-xs break-all">
                        <p class="font-semibold mb-1">Liên kết cục bộ để kiểm tra</p>
                        <a href="${resetUrl}" class="text-action-accent underline">${resetUrl}</a>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="mt-8 space-y-3">
                <a href="${pageContext.request.contextPath}/login" class="w-full inline-flex justify-center items-center py-3 px-4 bg-action-accent text-white font-semibold text-sm rounded-lg shadow-md hover:shadow-lg transition duration-150">Quay lại đăng nhập</a>
                <a href="${pageContext.request.contextPath}/forgot-password" class="w-full inline-flex justify-center items-center py-2 text-xs font-semibold text-action-accent hover:underline">Gửi lại yêu cầu</a>
            </div>
        </section>
    </main>
</body>
</html>
