<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân quyền hệ thống - IMS Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .rbac-layout { display:grid; grid-template-columns:280px 1fr; gap:24px; align-items:start; }
        .role-picker { display:flex; flex-direction:column; gap:8px; }
        .role-picker label { font-size:12px; font-weight:700; color:var(--text-muted); letter-spacing:.4px; text-transform:uppercase; }
        .role-picker select { width:100%; padding:10px 12px; border:1px solid var(--border-color); border-radius:8px; color:var(--text-body); font-size:14px; background:#fff; }
        .role-help { margin-top:12px; color:var(--text-muted); font-size:13px; line-height:1.55; }
        .permission-card { padding:0; overflow:hidden; }
        .permission-card .card-header { padding:20px 22px; border-bottom:1px solid var(--border-color); }
        .permission-grid { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:12px; padding:20px 22px; }
        .permission-item { display:flex; align-items:flex-start; gap:12px; min-height:92px; padding:15px; border:1px solid var(--border-color); border-radius:9px; background:#fff; cursor:pointer; transition:.2s ease; }
        .permission-item:hover { border-color:var(--action-accent); box-shadow:0 4px 12px rgba(44,82,130,.10); }
        .permission-item:has(input:checked) { background:#F2F7FC; border-color:#9AB9D8; }
        .permission-item input { width:18px; height:18px; flex:0 0 auto; margin-top:2px; accent-color:var(--action-accent); }
        .permission-item strong { display:block; color:var(--primary-dark); font-size:13px; margin-bottom:4px; }
        .permission-item span { color:var(--text-muted); font-size:12px; line-height:1.45; }
        .permission-footer { display:flex; align-items:center; justify-content:space-between; gap:16px; padding:18px 22px; background:#F7FAFC; border-top:1px solid var(--border-color); }
        .permission-footer small { color:var(--text-muted); }
        .empty-state { margin:20px 22px; padding:25px; color:var(--text-muted); text-align:center; border:1px dashed var(--border-color); border-radius:8px; }
        @media (max-width:1000px) { .rbac-layout { grid-template-columns:1fr; } }
        @media (max-width:700px) { .permission-grid { grid-template-columns:1fr; } .permission-footer { align-items:stretch; flex-direction:column; } .permission-footer button { width:100%; justify-content:center; } }
    </style>
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-shield-halved"></i></div><div><h2>IMS Admin</h2><span>Quản trị hệ thống</span></div></div>
        <nav class="menu">
            <p class="menu-title">Quản trị</p>
            <a href="${pageContext.request.contextPath}/admin/users" class="menu-item"><i class="fa-solid fa-users-gear"></i><span>Tạo & Quản lý tài khoản</span></a>
            <a href="${pageContext.request.contextPath}/admin/permissions" class="menu-item active"><i class="fa-solid fa-user-lock"></i><span>Phân quyền chi tiết (RBAC)</span></a>
            <p class="menu-title">Hệ thống</p>
            <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-chart-line"></i><span>Bảng điều khiển chung</span></a>
            <a href="${pageContext.request.contextPath}/departments" class="menu-item"><i class="fa-solid fa-building"></i><span>Danh mục phòng ban</span></a>
        </nav>
        <div class="sidebar-bottom"><a href="${pageContext.request.contextPath}/logout" class="menu-item logout"><i class="fa-solid fa-right-from-bracket"></i><span>Đăng xuất</span></a></div>
    </aside>

    <main class="main">
        <header class="header">
            <div class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input type="text" placeholder="Tìm kiếm tài khoản, quyền hạn..."></div>
            <div class="header-right">
                <a class="notification" href="${pageContext.request.contextPath}/notifications" aria-label="Thông báo"><i class="fa-regular fa-bell"></i><c:if test="${unreadNotificationCount > 0}"><span>${unreadNotificationCount}</span></c:if></a>
                <div class="user"><div class="avatar">${currentUser.shortName != null ? currentUser.shortName : 'ADM'}</div><div class="user-info"><strong>${currentUser.fullName != null ? currentUser.fullName : 'Quản trị viên'}</strong><small>System Administrator (ADMIN)</small></div><i class="fa-solid fa-chevron-down"></i></div>
            </div>
        </header>

        <section class="content">
            <div class="page-title"><div><h1>Phân quyền chi tiết</h1><p>Thiết lập các quyền thao tác cho từng vai trò trong hệ thống quản lý thực tập sinh.</p></div></div>
            <c:if test="${param.updated == '1'}"><div style="background:var(--success-bg);color:var(--success-badge);border:1px solid var(--success-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-circle-check"></i> Đã lưu cấu hình phân quyền thành công.</div></c:if>
            <c:if test="${not empty param.error}"><div style="background:var(--danger-bg);color:var(--danger-badge);border:1px solid var(--danger-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-triangle-exclamation"></i> ${param.error}</div></c:if>

            <div class="rbac-layout">
                <aside class="card">
                    <div class="card-header"><div><h3>Chọn vai trò</h3><p>Xem cấu hình hiện tại</p></div><i class="fa-solid fa-users-gear card-header-icon"></i></div>
                    <form class="role-picker" action="${pageContext.request.contextPath}/admin/permissions" method="get">
                        <label for="role">Vai trò đang cấu hình</label>
                        <select id="role" name="role" onchange="this.form.submit()">
                            <c:forEach var="roleCode" items="${roles}"><option value="${roleCode}" ${roleCode == selectedRole ? 'selected' : ''}>${roleCode}</option></c:forEach>
                        </select>
                    </form>
                    <p class="role-help"><i class="fa-solid fa-circle-info"></i> Chọn vai trò để xem và chỉnh sửa các quyền được cấp.</p>
                </aside>

                <form class="card permission-card" action="${pageContext.request.contextPath}/admin/permissions" method="post">
                    <input type="hidden" name="role" value="${selectedRole}">
                    <div class="card-header"><div><h3>Quyền của ${selectedRole}</h3><p>Chọn các chức năng mà vai trò này được phép thực hiện.</p></div><i class="fa-solid fa-user-shield card-header-icon"></i></div>
                    <c:choose>
                        <c:when test="${empty permissions}"><div class="empty-state">Chưa có danh mục quyền. Hãy chạy db/migrations/001-rbac.sql.</div></c:when>
                        <c:otherwise>
                            <div class="permission-grid">
                                <c:forEach var="permissionCode" items="${permissions}">
                                    <label class="permission-item"><input type="checkbox" name="permission" value="${permissionCode}" ${selectedPermissions.contains(permissionCode) ? 'checked' : ''}><span><strong>${permissionCode}</strong><c:choose><c:when test="${permissionCode == 'INTERN_CREATE'}"><span>Tạo hồ sơ thực tập sinh mới.</span></c:when><c:when test="${permissionCode == 'INTERN_EDIT'}"><span>Cập nhật thông tin và trạng thái hồ sơ.</span></c:when><c:when test="${permissionCode == 'DOCUMENT_REVIEW'}"><span>Xem, duyệt hoặc từ chối tài liệu đã nộp.</span></c:when><c:when test="${permissionCode == 'DOCUMENT_UPLOAD'}"><span>Tải lên CV và tài liệu thực tập.</span></c:when><c:otherwise><span>Cho phép sử dụng chức năng ${permissionCode}.</span></c:otherwise></c:choose></span></label>
                                </c:forEach>
                            </div>
                            <div class="permission-footer"><small><i class="fa-solid fa-circle-info"></i> Thay đổi chỉ có hiệu lực sau khi lưu.</small><button type="submit" class="primary-btn"><i class="fa-solid fa-floppy-disk"></i> Lưu phân quyền</button></div>
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </section>
    </main>
</div>
</body>
</html>
