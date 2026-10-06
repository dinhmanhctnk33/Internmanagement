<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản trị hệ thống (Admin) - Quyền truy cập & Tài khoản</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .account-menu { position:relative; }.account-trigger { display:flex; align-items:center; gap:12px; border:0; background:transparent; color:inherit; cursor:pointer; padding:5px 8px; border-radius:9px; text-align:left; }.account-trigger:hover,.account-trigger[aria-expanded="true"] { background:rgba(255,255,255,.09); }.account-dropdown { position:absolute; z-index:100; top:calc(100% + 10px); right:0; min-width:210px; padding:7px; border:1px solid #dce5f0; border-radius:10px; background:#fff; box-shadow:0 14px 30px rgba(17,45,78,.18); }.account-dropdown[hidden] { display:none; }.account-dropdown a { display:flex; align-items:center; gap:10px; padding:10px 11px; border-radius:7px; color:#234a7d; text-decoration:none; font-size:14px; font-weight:600; }.account-dropdown a:hover { background:#edf3fb; }.account-dropdown .logout-link { color:#bd3f3f; }
    </style>
</head>

<body>

<div class="layout">

    <!-- ================= SIDEBAR ADMIN ================= -->
    <aside class="sidebar">

        <div class="logo">
            <div class="logo-icon">
                <i class="fa-solid fa-shield-halved"></i>
            </div>
            <div>
                <h2>IMS Admin</h2>
                <span>Quản trị hệ thống</span>
            </div>
        </div>

        <nav class="menu">
            <p class="menu-title">QUẢN TRỊ</p>

            <a href="${pageContext.request.contextPath}/admin/users" class="menu-item active">
                <i class="fa-solid fa-users-gear"></i>
                <span>Tạo tài khoản</span>
            </a>

            <a href="${pageContext.request.contextPath}/admin/accounts" class="menu-item">
                <i class="fa-solid fa-user-gear"></i>
                <span>Quản lý tài khoản hệ thống</span>
            </a>

            <a href="${pageContext.request.contextPath}/admin/permissions" class="menu-item">
                <i class="fa-solid fa-user-lock"></i>
                <span>Phân quyền chi tiết (RBAC)</span>
            </a>

            <p class="menu-title">HỆ THỐNG</p>

            <a href="${pageContext.request.contextPath}/home" class="menu-item">
                <i class="fa-solid fa-chart-line"></i>
                <span>Bảng điều khiển chung</span>
            </a>

            <a href="${pageContext.request.contextPath}/departments" class="menu-item">
                <i class="fa-solid fa-building"></i>
                <span>Danh mục phòng ban</span>
            </a>
        </nav>

        <div class="sidebar-bottom">
            <a href="${pageContext.request.contextPath}/change-password" class="menu-item">
                <i class="fa-solid fa-key"></i><span>Đổi mật khẩu</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">
                <i class="fa-solid fa-right-from-bracket"></i>
                <span>Đăng xuất</span>
            </a>
        </div>

    </aside>


    <!-- ================= MAIN ================= -->
    <main class="main">

        <!-- HEADER -->
        <header class="header">
            <div class="search-box">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input type="text" placeholder="Tìm kiếm tài khoản, quyền hạn...">
            </div>

            <div class="header-right">
                <a class="notification" href="${pageContext.request.contextPath}/notifications" aria-label="Thông báo">
                    <i class="fa-regular fa-bell"></i>
                    <c:if test="${unreadNotificationCount > 0}"><span>${unreadNotificationCount}</span></c:if>
                </a>

                <div class="account-menu">
                    <div class="account-trigger user" id="accountMenuButton" role="button" tabindex="0" aria-expanded="false" aria-controls="accountMenu">
                    <div class="avatar">
                        ${currentUser.shortName != null ? currentUser.shortName : 'ADM'}
                    </div>

                    <div class="user-info">
                        <strong>${currentUser.fullName != null ? currentUser.fullName : 'Quản trị viên'}</strong>
                        <small>System Administrator (ADMIN)</small>
                    </div>

                    <i class="fa-solid fa-chevron-down"></i>
                    </div>
                    <div class="account-dropdown" id="accountMenu" hidden>
                        <a href="${pageContext.request.contextPath}/change-password"><i class="fa-solid fa-key"></i> Đổi mật khẩu</a>
                        <a class="logout-link" href="${pageContext.request.contextPath}/logout"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a>
                    </div>
                </div>
            </div>
        </header>


        <!-- CONTENT -->
        <section class="content">

            <div class="page-title">
                <div>
                    <h1>Quản lý Tài khoản & Phân quyền hệ thống</h1>
                    <p>Tạo tài khoản mới cho HR, Mentor, TTS và thiết lập phân quyền chi tiết cho từng vai trò.</p>
                </div>
            </div>

            <c:if test="${param.created == '1'}">
                <div style="background: var(--success-bg); color: var(--success-badge); border: 1px solid var(--success-border); padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    <i class="fa-solid fa-circle-check"></i> Đã tạo tài khoản người dùng thành công!
                </div>
            </c:if>

            <c:if test="${param.notification == 'sent'}">
                <div style="background:#EFFAF3;color:#1C7C45;border:1px solid #BDE7CA;padding:12px 16px;border-radius:8px;margin:-10px 0 20px;font-weight:600;">
                    <i class="fa-solid fa-envelope-circle-check"></i> Đã gửi email thông tin tài khoản đến người dùng.
                </div>
            </c:if>
            <c:if test="${param.notification == 'failed'}">
                <div style="background:#FFF8E8;color:#9A5A00;border:1px solid #F5D68A;padding:12px 16px;border-radius:8px;margin:-10px 0 20px;font-weight:600;">
                    <i class="fa-solid fa-triangle-exclamation"></i> Tài khoản đã được tạo, nhưng chưa gửi được email thông báo. Vui lòng kiểm tra cấu hình SMTP.
                </div>
            </c:if>

            <c:if test="${param.updated == '1'}">
                <div style="background: var(--success-bg); color: var(--success-badge); border: 1px solid var(--success-border); padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    <i class="fa-solid fa-circle-check"></i> Đã cập nhật phân quyền chi tiết (RBAC) thành công!
                </div>
            </c:if>


            <!-- GRID: TẠO TÀI KHOẢN & PHÂN QUYỀN -->
            <c:if test="${param.result == 'status-updated'}">
                <div style="background:var(--success-bg);color:var(--success-badge);border:1px solid var(--success-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-circle-check"></i> Đã cập nhật trạng thái tài khoản.</div>
            </c:if>
            <c:if test="${param.result == 'password-reset'}">
                <div style="background:var(--success-bg);color:var(--success-badge);border:1px solid var(--success-border);padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-circle-check"></i> Đã đặt lại mật khẩu tài khoản.</div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div style="background:#FFF5F5;color:var(--danger-badge);border:1px solid #FED7D7;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-triangle-exclamation"></i> ${param.error}</div>
            </c:if>
            <div class="dashboard-grid">

                <!-- FORM TẠO TÀI KHOẢN MỚI (HR, MENTOR, TTS) -->
                <div class="card">
                    <div class="card-header">
                        <div>
                            <h3>Tạo tài khoản người dùng mới</h3>
                            <p>Cấp tài khoản cho Nhân sự (HR), Cán bộ hướng dẫn (Mentor) hoặc Thực tập sinh (TTS)</p>
                        </div>
                        <i class="fa-solid fa-user-plus card-header-icon"></i>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: flex; flex-direction: column; gap: 14px;">
                        
                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Tên đăng nhập (Username) <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <input type="text" name="username" required placeholder="Ví dụ: hr_nhansu / mentor_nam" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px;">
                        </div>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Mật khẩu <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <input type="password" name="password" required placeholder="••••••••" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px;">
                        </div>

                        <div>
                            <label style="display:block;font-size:13px;font-weight:600;color:var(--primary-dark);margin-bottom:4px;">Nhập lại mật khẩu <span style="color:var(--danger-badge)">*</span></label>
                            <input type="password" name="confirmPassword" required minlength="6" autocomplete="new-password" placeholder="Nhập lại mật khẩu" style="width:100%;padding:9px 12px;border:1px solid var(--border-color);border-radius:8px;font-size:14px;">
                        </div>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Email <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <input type="email" name="email" required maxlength="254" autocomplete="email" inputmode="email" pattern="[^\s@]+@[^\s@]+\.[^\s@]+" title="Nhập email hợp lệ, ví dụ user@example.com" placeholder="user@example.com" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px;">
                        </div>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Họ và tên đầy đủ <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <input type="text" name="fullName" required placeholder="Ví dụ: Nguyễn Văn Nam" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px;">
                        </div>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Phân vai trò (Role) <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <select name="role" required style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px; background: #FFFFFF;">
                                <option value="HR">Phòng nhân sự (HR)</option>
                                <option value="MENTOR">Cán bộ Hướng dẫn (Mentor)</option>
                                <option value="INTERN">Thực tập sinh (INTERN)</option>
                                <option value="ADMIN">Quản trị viên (ADMIN)</option>
                            </select>
                        </div>

                        <button type="submit" class="primary-btn" style="width: 100%; justify-content: center; margin-top: 6px;">
                            <i class="fa-solid fa-user-check"></i> TẠO TÀI KHOẢN NGƯỜI DÙNG
                        </button>
                    </form>
                </div>


                <!-- PHÂN QUYỀN CHI TIẾT (RBAC) -->
                <div class="card">
                    <div class="card-header">
                        <div>
                            <h3>Phân quyền chi tiết (RBAC)</h3>
                            <p>Cấu hình quyền thao tác chi tiết cho từng Vai trò trong hệ thống</p>
                        </div>
                        <i class="fa-solid fa-user-lock card-header-icon"></i>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/permissions" method="post" style="display: flex; flex-direction: column; gap: 14px;">
                        
                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Chọn vai trò để phân quyền
                            </label>
                            <select name="role" required style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px; background: #FFFFFF;">
                                <option value="HR">Phòng nhân sự (HR)</option>
                                <option value="INTERN">Thực tập sinh (INTERN)</option>
                                <option value="MENTOR">Cán bộ Hướng dẫn (MENTOR)</option>
                            </select>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 8px; background: #F7FAFC; padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
                            <strong style="font-size: 12px; color: var(--primary-dark);">Gắn quyền thao tác:</strong>

                            <label style="font-size: 13px; display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input type="checkbox" name="permission" value="INTERN_CREATE" checked>
                                <span>Thêm mới hồ sơ Thực tập sinh (`INTERN_CREATE`)</span>
                            </label>

                            <label style="font-size: 13px; display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input type="checkbox" name="permission" value="INTERN_EDIT" checked>
                                <span>Chỉnh sửa hồ sơ Thực tập sinh (`INTERN_EDIT`)</span>
                            </label>

                            <label style="font-size: 13px; display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input type="checkbox" name="permission" value="DOCUMENT_REVIEW" checked>
                                <span>Xem & Duyệt tài liệu TTS (`DOCUMENT_REVIEW`)</span>
                            </label>

                            <label style="font-size: 13px; display: flex; align-items: center; gap: 8px; cursor: pointer;">
                                <input type="checkbox" name="permission" value="DOCUMENT_UPLOAD" checked>
                                <span>Upload CV & Đơn xin thực tập (`DOCUMENT_UPLOAD`)</span>
                            </label>
                        </div>

                        <button type="submit" class="outline-btn" style="margin-top: 4px;">
                            <i class="fa-solid fa-floppy-disk"></i> LƯU CẤU HÌNH PHÂN QUYỀN
                        </button>
                    </form>
                </div>


                <!-- BẢNG DANH SÁCH TÀI KHOẢN HIỆN TẠI -->
                <div class="card large-card" style="grid-column: span 2;">
                    <div class="card-header">
                        <div>
                            <h3>Danh sách tài khoản hệ thống</h3>
                            <p>Tất cả tài khoản Admin, HR, Mentor và Thực tập sinh</p>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Tên đăng nhập</th>
                                    <th>Họ và tên</th>
                                    <th>Email</th>
                                    <th>Vai trò</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                 <c:choose>
                                    <c:when test="${not empty users}">
                                        <c:forEach var="u" items="${users}">
                                            <tr>
                                                <td>#${u.id}</td>
                                                <td><strong>${u.username}</strong></td>
                                                <td>${u.full_name}</td>
                                                <td>${u.email}</td>
                                                <td>
                                                    <span class="badge ${u.role_code == 'ADMIN' ? 'active' : (u.role_code == 'HR' ? 'pending' : 'inactive')}">
                                                        ${u.role_code}
                                                    </span>
                                                </td>
                                                <td><span class="badge ${u.status == 'ACTIVE' ? 'active' : 'inactive'}"><i class="fa-solid ${u.status == 'ACTIVE' ? 'fa-check' : 'fa-lock'}"></i> ${u.status}</span></td>
                                                <td>
                                                    <div style="display:flex;flex-wrap:wrap;gap:6px;">
                                                        <form action="${pageContext.request.contextPath}/admin/users" method="post" onsubmit="return confirm('${u.status == 'ACTIVE' ? 'Khóa' : 'Kích hoạt'} tài khoản ${u.username}?');">
                                                            <input type="hidden" name="action" value="change-status"><input type="hidden" name="userId" value="${u.id}"><input type="hidden" name="status" value="${u.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                                                            <button type="submit" class="outline-btn" style="padding:6px 9px;font-size:12px;">${u.status == 'ACTIVE' ? 'Khóa' : 'Kích hoạt'}</button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/admin/users" method="post" onsubmit="return resetUserPassword(this);" data-username="${u.username}">
                                                            <input type="hidden" name="action" value="reset-password"><input type="hidden" name="userId" value="${u.id}"><input type="hidden" name="newPassword">
                                                            <button type="submit" class="outline-btn" style="padding:6px 9px;font-size:12px;">Reset mật khẩu</button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td>#1</td>
                                            <td><strong>admin_demo</strong></td>
                                            <td>Quản trị viên Demo</td>
                                            <td>admin.demo@ims.local</td>
                                            <td><span class="badge active">ADMIN</span></td>
                                            <td><span class="badge active"><i class="fa-solid fa-check"></i> ACTIVE</span></td>
                                        </tr>
                                        <tr>
                                            <td>#2</td>
                                            <td><strong>hr_demo</strong></td>
                                            <td>Nhân sự Demo</td>
                                            <td>hr.demo@ims.local</td>
                                            <td><span class="badge pending">HR</span></td>
                                            <td><span class="badge active"><i class="fa-solid fa-check"></i> ACTIVE</span></td>
                                        </tr>
                                        <tr>
                                            <td>#3</td>
                                            <td><strong>mentor_demo</strong></td>
                                            <td>Mentor Demo</td>
                                            <td>mentor.demo@ims.local</td>
                                            <td><span class="badge inactive">MENTOR</span></td>
                                            <td><span class="badge active"><i class="fa-solid fa-check"></i> ACTIVE</span></td>
                                        </tr>
                                        <tr>
                                            <td>#4</td>
                                            <td><strong>intern_demo</strong></td>
                                            <td>Thực tập sinh Demo</td>
                                            <td>intern.demo@ims.local</td>
                                            <td><span class="badge inactive">INTERN</span></td>
                                            <td><span class="badge active"><i class="fa-solid fa-check"></i> ACTIVE</span></td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                </div>

            </div>

        </section>

    </main>

</div>

<div class="password-reset-modal" id="passwordResetModal" hidden>
    <div class="password-reset-backdrop" data-close-reset-modal></div>
    <section class="password-reset-dialog" role="dialog" aria-modal="true" aria-labelledby="passwordResetTitle">
        <button type="button" class="password-reset-close" data-close-reset-modal aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button>
        <div class="password-reset-icon"><i class="fa-solid fa-key"></i></div>
        <div class="password-reset-heading">
            <p>Bảo mật tài khoản</p>
            <h2 id="passwordResetTitle">Đặt lại mật khẩu</h2>
            <span>Mật khẩu mới sẽ được áp dụng cho <strong id="passwordResetUsername"></strong>.</span>
        </div>
        <form id="passwordResetDialogForm" novalidate>
            <label for="adminNewPassword">Mật khẩu mới</label>
            <div class="password-reset-input">
                <input id="adminNewPassword" type="password" minlength="6" autocomplete="new-password" placeholder="Tối thiểu 6 ký tự">
                <button type="button" data-toggle-password aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button>
            </div>
            <label for="adminConfirmPassword">Xác nhận mật khẩu</label>
            <div class="password-reset-input">
                <input id="adminConfirmPassword" type="password" minlength="6" autocomplete="new-password" placeholder="Nhập lại mật khẩu mới">
                <button type="button" data-toggle-password aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button>
            </div>
            <p class="password-reset-error" id="passwordResetError" aria-live="polite"></p>
            <div class="password-reset-actions">
                <button type="button" class="password-reset-cancel" data-close-reset-modal>Hủy</button>
                <button type="submit" class="password-reset-submit"><i class="fa-solid fa-rotate"></i> Xác nhận reset</button>
            </div>
        </form>
    </section>
</div>

<script>
    (() => { const button=document.getElementById('accountMenuButton'), menu=document.getElementById('accountMenu'); if(!button||!menu)return; const close=()=>{menu.hidden=true;button.setAttribute('aria-expanded','false');}; button.addEventListener('click',()=>{const open=menu.hidden;menu.hidden=!open;button.setAttribute('aria-expanded',String(open));}); document.addEventListener('click',event=>{if(!event.target.closest('.account-menu'))close();}); document.addEventListener('keydown',event=>{if(event.key==='Escape')close();}); })();
</script>
<script src="${pageContext.request.contextPath}/frontend/js/admin-password-reset.js?v=20261007-1"></script>
</body>
</html>
