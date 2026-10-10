<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty intern ? 'Chỉnh sửa hồ sơ thực tập sinh' : 'Thêm mới hồ sơ thực tập sinh'} - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .form-grid { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:18px 22px; }
        .form-group { display:flex; flex-direction:column; gap:7px; }
        .form-label { font-size:13px; font-weight:600; color:var(--primary-dark); }
        .form-input { width:100%; box-sizing:border-box; padding:12px 14px; border:1px solid var(--border-color); border-radius:9px; font-size:14px; background:#fff; }
        .form-input:focus { outline:none; border-color:var(--action-accent); box-shadow:0 0 0 3px rgba(44,82,130,.15); }
        .form-input:read-only { background:#f7fafc; color:var(--text-muted); cursor:not-allowed; }
        .required { color:var(--danger-badge); }
        .field-hint { color:var(--text-muted); font-size:11px; margin:0; }
        .confirm-backdrop { position:fixed; inset:0; z-index:1000; display:none; align-items:center; justify-content:center; padding:20px; background:rgba(17, 38, 68, .52); }
        .confirm-backdrop.open { display:flex; }
        .confirm-modal { width:min(560px, 100%); max-height:calc(100vh - 40px); overflow:auto; background:#fff; border-radius:14px; box-shadow:0 24px 64px rgba(19, 42, 76, .28); }
        .confirm-modal-head { display:flex; gap:12px; align-items:flex-start; padding:22px 24px 16px; border-bottom:1px solid var(--border-color); }
        .confirm-icon { width:38px; height:38px; flex:0 0 38px; display:grid; place-items:center; border-radius:10px; background:#e7f0fb; color:var(--action-accent); }
        .confirm-details { margin:18px 24px; border:1px solid var(--border-color); border-radius:10px; overflow:hidden; }
        .confirm-row { display:grid; grid-template-columns:150px 1fr; gap:12px; padding:11px 14px; font-size:13px; border-bottom:1px solid #edf2f7; }
        .confirm-row:last-child { border-bottom:0; }
        .confirm-row dt { color:var(--text-muted); font-weight:600; }
        .confirm-row dd { margin:0; color:var(--primary-dark); font-weight:600; word-break:break-word; }
        .confirm-actions { display:flex; justify-content:flex-end; gap:10px; padding:0 24px 22px; }
        .confirm-cancel { padding:10px 18px; border:1px solid var(--border-color); border-radius:8px; background:#fff; color:var(--text-muted); font-weight:600; cursor:pointer; }
        @media (max-width:520px) { .confirm-row { grid-template-columns:1fr; gap:3px; } .confirm-actions { flex-direction:column-reverse; } .confirm-actions button { width:100%; justify-content:center; } }
        @media (max-width:760px) { .form-grid { grid-template-columns:1fr; } }
    </style>
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-tie"></i></div><div><h2>IMS Portal</h2><span>Phòng Nhân sự (HR)</span></div></div>
        <nav class="menu">
            <p class="menu-title">NGHIỆP VỤ HR</p>
            <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-chart-line"></i><span>Dashboard HR</span></a>
            <a href="${pageContext.request.contextPath}/interns" class="menu-item"><i class="fa-solid fa-users"></i><span>Thực tập sinh &amp; Lọc</span></a>
            <a href="${pageContext.request.contextPath}/interns/new" class="menu-item ${empty intern ? 'active' : ''}"><i class="fa-solid fa-user-plus"></i><span>Thêm hồ sơ TTS</span></a>
            <a href="${pageContext.request.contextPath}/documents/review" class="menu-item"><i class="fa-solid fa-file-circle-check"></i><span>Duyệt tài liệu &amp; CV</span></a>
        </nav>
        
    </aside>

    <main class="main">
        <header class="header intern-header"><div class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input type="text" placeholder="Tìm kiếm thực tập sinh..."></div><%@ include file="../includes/hr-header-actions.jspf" %></header>
        <section class="content">
            <div class="page-title">
                <div><h1><i class="fa-solid fa-${not empty intern ? 'pen-to-square' : 'user-plus'}" style="color:var(--action-accent);margin-right:10px;"></i>${not empty intern ? 'Chỉnh sửa hồ sơ Thực tập sinh' : 'Thêm mới hồ sơ Thực tập sinh'}</h1><p>${not empty intern ? 'Cập nhật thông tin hồ sơ thực tập sinh' : 'Điền thông tin để tạo hồ sơ mới'}</p></div>
                <a href="${pageContext.request.contextPath}/interns" class="primary-btn" style="background:var(--text-muted);"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</a>
            </div>

            <c:if test="${not empty errorMessage}"><div style="background:#FFF5F5;color:var(--danger-badge);border:1px solid #FED7D7;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;"><i class="fa-solid fa-triangle-exclamation"></i> ${errorMessage}</div></c:if>

            <div class="card">
                <form id="internProfileForm" action="${pageContext.request.contextPath}/home" method="post">
                    <c:if test="${not empty intern}"><input type="hidden" name="id" value="${intern.id}"><input type="hidden" name="userId" value="${intern.userId}"></c:if>
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="internCode" class="form-label">Mã thực tập sinh <span class="required">*</span></label>
                            <input id="internCode" type="text" name="internCode" class="form-input" value="${intern.internCode}" placeholder="VD: TTS2024001" required>
                        </div>

                        <c:choose>
                            <c:when test="${empty intern}">
                                <div class="form-group">
                                    <label for="userId" class="form-label">Tên sinh viên <span class="required">*</span></label>
                                    <select id="userId" name="userId" class="form-input" required>
                                        <option value="">-- Chọn sinh viên --</option>
                                        <c:forEach var="account" items="${internAccounts}"><option value="${account.id}" ${intern.userId == account.id ? 'selected' : ''}>${account.fullName} — ${account.email}</option></c:forEach>
                                    </select>
                                    <p class="field-hint">Chọn theo họ tên; hệ thống tự liên kết tài khoản Intern tương ứng.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="form-group">
                                    <label for="fullName" class="form-label">Tên sinh viên</label>
                                    <input id="fullName" type="text" class="form-input" value="${intern.fullName}" readonly>
                                    <p class="field-hint">Tên lấy từ tài khoản Intern đã liên kết.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="form-group"><label for="phoneNumber" class="form-label">Số điện thoại</label><input id="phoneNumber" type="text" name="phoneNumber" class="form-input" value="${intern.phoneNumber}" placeholder="0912345678"></div>
                        <div class="form-group"><label for="universityName" class="form-label">Trường Đại học <span class="required">*</span></label><input id="universityName" type="text" name="universityName" class="form-input" value="${intern.universityName}" placeholder="VD: ĐH Bách Khoa Hà Nội" required></div>
                        <div class="form-group"><label for="majorName" class="form-label">Ngành học <span class="required">*</span></label><input id="majorName" type="text" name="majorName" class="form-input" value="${intern.majorName}" placeholder="VD: Công nghệ thông tin" required></div>
                        <div class="form-group"><label for="internshipStatus" class="form-label">Trạng thái thực tập <span class="required">*</span></label><select id="internshipStatus" name="internshipStatus" class="form-input" required><option value="ACTIVE" ${intern.internshipStatus == 'ACTIVE' ? 'selected' : ''}>Đang thực tập</option><option value="COMPLETED" ${intern.internshipStatus == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option><option value="PAUSED" ${intern.internshipStatus == 'PAUSED' ? 'selected' : ''}>Tạm dừng</option><option value="DROPPED" ${intern.internshipStatus == 'DROPPED' ? 'selected' : ''}>Bỏ giữa chừng</option></select></div>
                    </div>
                    <div style="display:flex;gap:12px;justify-content:flex-end;margin-top:24px;"><a href="${pageContext.request.contextPath}/interns" style="padding:10px 24px;border:1px solid var(--border-color);border-radius:8px;color:var(--text-muted);text-decoration:none;font-weight:600;">Hủy bỏ</a><button type="submit" class="primary-btn"><i class="fa-solid fa-${not empty intern ? 'floppy-disk' : 'plus'}"></i> ${not empty intern ? 'Lưu thay đổi' : 'Tạo hồ sơ mới'}</button></div>
                </form>
            </div>
        </section>
    </main>
</div>
<div id="confirmBackdrop" class="confirm-backdrop" role="dialog" aria-modal="true" aria-labelledby="confirmTitle" hidden>
    <div class="confirm-modal">
        <div class="confirm-modal-head">
            <div class="confirm-icon"><i class="fa-solid fa-circle-check"></i></div>
            <div><h2 id="confirmTitle" style="margin:0;color:var(--primary-dark);font-size:20px;">Xác nhận tạo hồ sơ</h2><p style="margin:5px 0 0;color:var(--text-muted);font-size:13px;">Vui lòng kiểm tra lại thông tin trước khi lưu.</p></div>
        </div>
        <dl id="confirmDetails" class="confirm-details"></dl>
        <div class="confirm-actions">
            <button id="cancelCreate" type="button" class="confirm-cancel">Quay lại chỉnh sửa</button>
            <button id="confirmCreate" type="button" class="primary-btn"><i class="fa-solid fa-check"></i> Xác nhận tạo hồ sơ</button>
        </div>
    </div>
</div>
<script>
    (() => {
        const form = document.getElementById('internProfileForm');
        const backdrop = document.getElementById('confirmBackdrop');
        const details = document.getElementById('confirmDetails');
        const confirmButton = document.getElementById('confirmCreate');
        const cancelButton = document.getElementById('cancelCreate');
        if (!form || !backdrop) return;
        const phoneInput = form.elements.phoneNumber;
        if (phoneInput) {
            phoneInput.setAttribute('inputmode', 'numeric');
            phoneInput.setAttribute('maxlength', '10');
            phoneInput.setAttribute('title', 'Số điện thoại gồm 10 chữ số và bắt đầu bằng số 0.');
            phoneInput.addEventListener('input', () => {
                phoneInput.value = phoneInput.value.replace(/\D/g, '').slice(0, 10);
                const phone = phoneInput.value.trim();
                phoneInput.setCustomValidity(phone && !/^0\d{9}$/.test(phone)
                    ? 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.' : '');
            });
        }

        const fields = [
            ['internCode', 'Mã thực tập sinh'], ['userId', 'Tên sinh viên'],
            ['phoneNumber', 'Số điện thoại'], ['universityName', 'Trường Đại học'],
            ['majorName', 'Ngành học'], ['internshipStatus', 'Trạng thái thực tập']
        ];
        const valueOf = (name) => {
            const element = form.elements[name];
            if (!element) return 'Chưa cập nhật';
            if (element.tagName === 'SELECT') {
                return element.options[element.selectedIndex]?.text.trim() || 'Chưa chọn';
            }
            return element.value.trim() || 'Chưa cập nhật';
        };
        const close = () => { backdrop.classList.remove('open'); backdrop.hidden = true; };

        form.addEventListener('submit', (event) => {
            if (form.dataset.confirmed === 'true') return;
            if (!form.checkValidity()) return;
            event.preventDefault();
            details.replaceChildren();
            fields.forEach(([name, label]) => {
                const row = document.createElement('div');
                row.className = 'confirm-row';
                const title = document.createElement('dt'); title.textContent = label;
                const value = document.createElement('dd'); value.textContent = valueOf(name);
                row.append(title, value); details.appendChild(row);
            });
            backdrop.hidden = false; backdrop.classList.add('open');
            confirmButton.focus();
        });
        confirmButton.addEventListener('click', () => { form.dataset.confirmed = 'true'; form.requestSubmit(); });
        cancelButton.addEventListener('click', close);
        backdrop.addEventListener('click', (event) => { if (event.target === backdrop) close(); });
        document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && !backdrop.hidden) close(); });
    })();
</script>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
