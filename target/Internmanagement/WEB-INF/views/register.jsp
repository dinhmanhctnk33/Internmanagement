<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Đăng ký ứng viên - IMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/business.css?v=20261004-4"></head>
<body class="auth-page">
<main class="auth-shell">
    <section class="auth-intro"><div class="auth-logo">IMS Portal</div><h1>Bắt đầu hành trình thực tập</h1><p>Tạo tài khoản ứng viên để xem chương trình đang mở, lưu hồ sơ nháp và theo dõi kết quả xét duyệt.</p><ul><li>Nộp hồ sơ theo từng chương trình</li><li>Theo dõi trạng thái trực tuyến</li><li>Nhận kết quả qua email đã xác thực</li></ul></section>
    <section class="auth-card"><div class="auth-heading"><p>Ứng viên mới</p><h2>Đăng ký tài khoản</h2><span>Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></span></div>
        <% if(request.getAttribute("error")!=null){%><div class="alert error"><%=request.getAttribute("error")%></div><%}%>
        <% if(request.getAttribute("success")!=null){%><div class="alert success"><%=request.getAttribute("success")%><br><a href="<%=request.getAttribute("verifyUrl")%>">Xác thực email ngay</a></div><%}%>
        <form method="post" class="auth-form"><label>Họ và tên *<input name="fullName" autocomplete="name" required placeholder="Nguyễn Văn A"></label><label>Email *<input type="email" name="email" autocomplete="email" required placeholder="email@example.com"></label><label>Mật khẩu *<input type="password" name="password" autocomplete="new-password" minlength="8" required placeholder="Tối thiểu 8 ký tự"></label><p class="field-help">Nên có chữ hoa, chữ thường và số.</p><label>Xác nhận mật khẩu *<input type="password" name="confirmPassword" autocomplete="new-password" minlength="8" required></label><div class="auth-actions"><a href="${pageContext.request.contextPath}/login" class="cancel-link">Hủy</a><button>Đăng ký</button></div></form>
    </section>
</main>
</body></html>
