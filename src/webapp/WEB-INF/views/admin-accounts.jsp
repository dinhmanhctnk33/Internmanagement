<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý tài khoản hệ thống - IMS Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-shield-halved"></i></div><div><h2>IMS Admin</h2><span>Quản trị hệ thống</span></div></div>
        <nav class="menu">
            <p class="menu-title">QUẢN TRỊ</p>
            <a href="${pageContext.request.contextPath}/admin/users" class="menu-item"><i class="fa-solid fa-user-plus"></i><span>Tạo tài khoản</span></a>
            <a href="${pageContext.request.contextPath}/admin/accounts" class="menu-item active"><i class="fa-solid fa-user-gear"></i><span>Quản lý tài khoản hệ thống</span></a>
            <a href="${pageContext.request.contextPath}/admin/permissions" class="menu-item"><i class="fa-solid fa-user-lock"></i><span>Phân quyền chi tiết (RBAC)</span></a>
            <p class="menu-title">HỆ THỐNG</p>
            <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-chart-line"></i><span>Bảng điều khiển chung</span></a>
        </nav>
        <div class="sidebar-bottom"><a href="${pageContext.request.contextPath}/logout" class="menu-item logout"><i class="fa-solid fa-right-from-bracket"></i><span>Đăng xuất</span></a></div>
    </aside>
    <main class="main">
        <header class="header"><div class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input type="text" placeholder="Tìm kiếm tài khoản..."></div><div class="header-right"><div class="user"><div class="avatar">${currentUser.shortName}</div><div class="user-info"><strong>${currentUser.fullName}</strong><small>System Administrator</small></div></div></div></header>
        <section class="content">
            <div class="page-title"><div><h1>Quản lý tài khoản hệ thống</h1><p>Khóa, kích hoạt và đặt lại mật khẩu cho các tài khoản người dùng.</p></div><a href="${pageContext.request.contextPath}/admin/users" class="primary-btn"><i class="fa-solid fa-user-plus"></i> Tạo tài khoản mới</a></div>
            <c:if test="${param.result == 'status-updated'}"><div style="background:var(--success-bg);color:var(--success-badge);border:1px solid var(--success-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-circle-check"></i> Đã cập nhật trạng thái tài khoản.</div></c:if>
            <c:if test="${param.result == 'password-reset'}"><div style="background:var(--success-bg);color:var(--success-badge);border:1px solid var(--success-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-circle-check"></i> Đã đặt lại mật khẩu tài khoản.</div></c:if>
            <c:if test="${not empty param.error}"><div style="background:#FFF5F5;color:var(--danger-badge);border:1px solid #FED7D7;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-triangle-exclamation"></i> ${param.error}</div></c:if>
            <div class="card"><div class="card-header"><div><h3>Danh sách tài khoản</h3><p>Thao tác sẽ được áp dụng ngay sau khi xác nhận.</p></div></div><div class="table-container"><table><thead><tr><th>ID</th><th>Tên đăng nhập</th><th>Họ và tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
                <c:choose><c:when test="${not empty users}"><c:forEach var="u" items="${users}"><tr><td>#${u.id}</td><td><strong>${u.username}</strong></td><td>${u.full_name}</td><td>${u.email}</td><td><span class="badge ${u.role_code == 'ADMIN' ? 'active' : (u.role_code == 'HR' ? 'pending' : 'inactive')}">${u.role_code}</span></td><td><span class="badge ${u.status == 'ACTIVE' ? 'active' : 'inactive'}"><i class="fa-solid ${u.status == 'ACTIVE' ? 'fa-check' : 'fa-lock'}"></i> ${u.status}</span></td><td><div style="display:flex;gap:7px;flex-wrap:wrap;white-space:nowrap;"><form action="${pageContext.request.contextPath}/admin/accounts" method="post" onsubmit="return confirm('${u.status == 'ACTIVE' ? 'Khóa' : 'Kích hoạt'} tài khoản ${u.username}?');"><input type="hidden" name="action" value="change-status"><input type="hidden" name="userId" value="${u.id}"><input type="hidden" name="status" value="${u.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}"><button type="submit" class="outline-btn" style="padding:6px 9px;font-size:12px;">${u.status == 'ACTIVE' ? 'Khóa' : 'Kích hoạt'}</button></form><form action="${pageContext.request.contextPath}/admin/accounts" method="post" onsubmit="return resetUserPassword(this);" data-username="${u.username}"><input type="hidden" name="action" value="reset-password"><input type="hidden" name="userId" value="${u.id}"><input type="hidden" name="newPassword"><button type="submit" class="outline-btn" style="padding:6px 9px;font-size:12px;">Reset mật khẩu</button></form></div></td></tr></c:forEach></c:when><c:otherwise><tr><td colspan="7" style="text-align:center;padding:36px;color:var(--text-muted);">Chưa có tài khoản nào.</td></tr></c:otherwise></c:choose>
            </tbody></table></div></div>
        </section>
    </main>
</div>
<div class="password-reset-modal" id="passwordResetModal" hidden>
    <div class="password-reset-backdrop" data-close-reset-modal></div>
    <section class="password-reset-dialog" role="dialog" aria-modal="true" aria-labelledby="passwordResetTitle">
        <button type="button" class="password-reset-close" data-close-reset-modal aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button>
        <div class="password-reset-icon"><i class="fa-solid fa-key"></i></div>
        <div class="password-reset-heading"><p>Bảo mật tài khoản</p><h2 id="passwordResetTitle">Đặt lại mật khẩu</h2><span>Mật khẩu mới sẽ được áp dụng cho <strong id="passwordResetUsername"></strong>.</span></div>
        <form id="passwordResetDialogForm" novalidate>
            <label for="adminNewPassword">Mật khẩu mới</label><div class="password-reset-input"><input id="adminNewPassword" type="password" minlength="6" autocomplete="new-password" placeholder="Tối thiểu 6 ký tự"><button type="button" data-toggle-password aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div>
            <label for="adminConfirmPassword">Xác nhận mật khẩu</label><div class="password-reset-input"><input id="adminConfirmPassword" type="password" minlength="6" autocomplete="new-password" placeholder="Nhập lại mật khẩu mới"><button type="button" data-toggle-password aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div>
            <p class="password-reset-error" id="passwordResetError" aria-live="polite"></p>
            <div class="password-reset-actions"><button type="button" class="password-reset-cancel" data-close-reset-modal>Hủy</button><button type="submit" class="password-reset-submit"><i class="fa-solid fa-rotate"></i> Xác nhận reset</button></div>
        </form>
    </section>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/admin-password-reset.js?v=20261007-1"></script>
</body>
</html>
