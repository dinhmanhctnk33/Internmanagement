<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi mật khẩu - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        body { min-height:100vh; margin:0; background:#f5f8fc; }
        .password-page { min-height:100vh; display:grid; place-items:center; padding:32px 18px; box-sizing:border-box; }
        .password-card { width:min(500px,100%); padding:30px; box-sizing:border-box; background:#fff; border:1px solid #dce5f0; border-radius:16px; box-shadow:0 16px 42px rgba(24,54,92,.12); }
        .password-heading { display:flex; align-items:center; gap:12px; margin-bottom:24px; }
        .password-heading .icon { width:44px; height:44px; display:grid; place-items:center; border-radius:12px; background:#e8f0fb; color:#2c5282; font-size:20px; }
        .password-heading h1 { margin:0; color:#163a69; font-size:25px; }
        .password-heading p { margin:5px 0 0; color:#718096; font-size:14px; }
        .form-group { margin:0 0 17px; }
        .form-group label { display:block; margin-bottom:7px; color:#163a69; font-weight:650; font-size:14px; }
        .password-input { width:100%; box-sizing:border-box; padding:12px 14px; border:1px solid #cfd9e7; border-radius:9px; font-size:15px; }
        .password-input:focus { outline:none; border-color:#2c5282; box-shadow:0 0 0 3px rgba(44,82,130,.13); }
        .alert { padding:12px 14px; margin-bottom:18px; border-radius:9px; font-size:14px; font-weight:600; }
        .alert.error { color:#b42318; background:#fff2f0; border:1px solid #fecdc8; }
        .alert.success { color:#177245; background:#ecfdf3; border:1px solid #b7e8c8; }
        .actions { display:flex; gap:12px; align-items:center; justify-content:flex-end; margin-top:25px; }
        .back-link { color:#56708f; text-decoration:none; font-weight:600; padding:11px 8px; }
        .save-button { border:0; border-radius:9px; padding:12px 18px; color:#fff; background:#2c5282; cursor:pointer; font-size:14px; font-weight:700; box-shadow:0 4px 10px rgba(44,82,130,.2); }
        .save-button:hover { background:#1f4678; }
    </style>
</head>
<body>
    <main class="password-page">
        <section class="password-card">
            <div class="password-heading">
                <div class="icon"><i class="fa-solid fa-key"></i></div>
                <div><h1>Đổi mật khẩu</h1><p>Bảo vệ tài khoản IMS Portal của bạn.</p></div>
            </div>
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert error"><i class="fa-solid fa-triangle-exclamation"></i> <%= request.getAttribute("error") %></div>
            <% } %>
            <% if ("1".equals(request.getParameter("success"))) { %>
                <div class="alert success"><i class="fa-solid fa-circle-check"></i> Đổi mật khẩu thành công.</div>
            <% } %>
            <form method="post" id="changePasswordForm">
                <div class="form-group"><label for="currentPassword">Mật khẩu hiện tại</label><input class="password-input" id="currentPassword" name="currentPassword" type="password" autocomplete="current-password" required></div>
                <div class="form-group"><label for="newPassword">Mật khẩu mới</label><input class="password-input" id="newPassword" name="newPassword" type="password" autocomplete="new-password" minlength="6" required></div>
                <div class="form-group"><label for="confirmPassword">Nhập lại mật khẩu mới</label><input class="password-input" id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required></div>
                <div class="actions"><a class="back-link" href="${pageContext.request.contextPath}/home"><i class="fa-solid fa-arrow-left"></i> Quay lại</a><button class="save-button" type="submit"><i class="fa-solid fa-floppy-disk"></i> Lưu mật khẩu mới</button></div>
            </form>
        </section>
    </main>
    <script>
        const newPassword = document.getElementById('newPassword');
        const confirmPassword = document.getElementById('confirmPassword');
        const validateMatch = () => confirmPassword.setCustomValidity(
            confirmPassword.value && confirmPassword.value !== newPassword.value ? 'Mật khẩu nhập lại không khớp.' : '');
        newPassword.addEventListener('input', validateMatch);
        confirmPassword.addEventListener('input', validateMatch);
    </script>
</body>
</html>
