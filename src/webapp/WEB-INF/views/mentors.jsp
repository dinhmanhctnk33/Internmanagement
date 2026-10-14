<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Quản lý mentor - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-assignments.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .mentor-table{width:100%;border-collapse:collapse;font-size:14px}
        .mentor-table th,.mentor-table td{padding:12px 10px;border-bottom:1px solid #e5e7eb;text-align:left;vertical-align:middle}
        .mentor-table th{font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:.03em}
        .badge{display:inline-block;padding:3px 10px;border-radius:999px;font-size:12px;font-weight:600}
        .badge.green{background:#dcfce7;color:#166534}.badge.gray{background:#e5e7eb;color:#475569}
        .badge.amber{background:#fef3c7;color:#92400e}.badge.red{background:#fee2e2;color:#991b1b}
        .row-actions{display:flex;gap:6px;flex-wrap:wrap}
        .row-actions form{margin:0}
        .btn{border:1px solid #cbd5e1;background:#fff;border-radius:8px;padding:6px 10px;cursor:pointer;font-size:13px}
        .btn.primary{background:#2563eb;border-color:#2563eb;color:#fff}.btn.danger{color:#b91c1c;border-color:#fecaca}
        .btn[disabled]{opacity:.45;cursor:not-allowed}
        .warning-banner{background:#fffbeb;border:1px solid #fcd34d;color:#92400e;border-radius:10px;padding:12px 16px;margin-bottom:16px;font-weight:600}
        .table-wrap{overflow-x:auto}
        dialog#mentorDialog{border:none;border-radius:14px;padding:0;width:min(520px,94vw);box-shadow:0 20px 50px rgba(0,0,0,.25)}
        dialog#mentorDialog::backdrop{background:rgba(15,23,42,.5)}
        .dialog-body{padding:22px}.dialog-body h2{margin:0 0 14px}
        .dialog-body label{display:block;margin-bottom:12px;font-size:13px;font-weight:600;color:#334155}
        .dialog-body input,.dialog-body select{width:100%;box-sizing:border-box;margin-top:4px;padding:9px 10px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px}
        .dialog-body input[readonly]{background:#f1f5f9}
        .field-hint{font-weight:400;color:#64748b;font-size:12px}
        .dialog-footer{display:flex;justify-content:flex-end;gap:8px;margin-top:8px}
        .toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:14px}
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
            <a href="${pageContext.request.contextPath}/documents/review" class="menu-item"><i class="fa-solid fa-file-circle-check"></i><span>Duyệt tài liệu &amp; CV</span></a>
            <a href="${pageContext.request.contextPath}/review-emails" class="menu-item"><i class="fa-solid fa-envelope-circle-check"></i><span>Email kết quả</span></a>
            <p class="menu-title">QUY TRÌNH THỰC TẬP</p>
            <a href="${pageContext.request.contextPath}/programs" class="menu-item"><i class="fa-solid fa-calendar-days"></i><span>Chương trình thực tập</span></a>
            <a href="${pageContext.request.contextPath}/applications" class="menu-item"><i class="fa-solid fa-file-signature"></i><span>Xét duyệt hồ sơ</span></a>
            <a href="${pageContext.request.contextPath}/contracts" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng thực tập</span></a>
            <a href="${pageContext.request.contextPath}/mentors" class="menu-item active"><i class="fa-solid fa-chalkboard-user"></i><span>Quản lý mentor</span></a>
            <a href="${pageContext.request.contextPath}/mentor-assignments" class="menu-item"><i class="fa-solid fa-user-group"></i><span>Phân công mentor</span></a>
            <a href="${pageContext.request.contextPath}/meetings" class="menu-item"><i class="fa-solid fa-video"></i><span>Lịch họp</span></a>
            <a href="${pageContext.request.contextPath}/attendance-report" class="menu-item"><i class="fa-solid fa-chart-column"></i><span>Báo cáo chuyên cần</span></a>
        </nav>
    </aside>
    <main class="main">
        <header class="header intern-header"><div class="page-context header-context"><i class="fa-solid fa-chalkboard-user"></i><span>Tổ chức hướng dẫn</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
                <section class="content mentor-page">
            <div class="page-heading"><div><p class="eyebrow">Tổ chức hướng dẫn</p><h1>Quản lý mentor</h1><p>Tạo hồ sơ mentor để phân công cho thực tập sinh. Tài khoản đăng nhập do quản trị viên cấp và tự gắn theo email.</p></div></div>

            <c:if test="${param.success=='created'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã tạo hồ sơ mentor.</div></c:if>
            <c:if test="${param.success=='updated'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã cập nhật hồ sơ mentor.</div></c:if>
            <c:if test="${param.success=='deactivated'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã ngừng hoạt động mentor.</div></c:if>
            <c:if test="${param.success=='activated'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã kích hoạt lại mentor.</div></c:if>
            <c:if test="${param.success=='deleted'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã xóa hồ sơ mentor.</div></c:if>
            <c:if test="${param.info=='duplicate'}"><div class="alert success"><i class="fa-solid fa-circle-info"></i> Yêu cầu này đã được xử lý trước đó.</div></c:if>
            <c:if test="${not empty param.error}"><div class="alert error"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>

            <c:choose>
            <c:when test="${not empty loadError}">
                <div class="alert error"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${loadError}"/>
                    <a class="btn" href="${pageContext.request.contextPath}/mentors">Thử lại</a></div>
            </c:when>
            <c:otherwise>
                <c:if test="${unlinkedInternCount > 0}">
                    <div class="warning-banner"><i class="fa-solid fa-triangle-exclamation"></i>
                        ${unlinkedInternCount} thực tập sinh đang được phân công cho mentor chưa liên kết tài khoản. Hãy nhắc quản trị viên cấp tài khoản cho mentor.</div>
                </c:if>

                <form method="get" class="filter-bar">
                    <label><span>Phòng ban</span><select name="departmentId"><option value="">Tất cả</option>
                        <c:forEach items="${departments}" var="d"><option value="${d.id}" ${selectedDepartmentId==d.id?'selected':''}><c:out value="${d.dept_name}"/></option></c:forEach></select></label>
                    <label><span>Hoạt động</span><select name="active"><option value="">Tất cả</option>
                        <option value="ACTIVE" ${selectedActive=='ACTIVE'?'selected':''}>Đang hoạt động</option>
                        <option value="INACTIVE" ${selectedActive=='INACTIVE'?'selected':''}>Ngừng hoạt động</option></select></label>
                    <label><span>Liên kết tài khoản</span><select name="link"><option value="">Tất cả</option>
                        <option value="LINKED" ${selectedLink=='LINKED'?'selected':''}>Đã liên kết</option>
                        <option value="UNLINKED" ${selectedLink=='UNLINKED'?'selected':''}>Chưa liên kết</option></select></label>
                    <label class="search-field"><span>Tìm mentor</span><div><i class="fa-solid fa-magnifying-glass"></i><input name="q" value="<c:out value='${keyword}'/>" placeholder="Tên hoặc email"></div></label>
                    <button type="submit"><i class="fa-solid fa-filter"></i> Lọc</button>
                    <a href="${pageContext.request.contextPath}/mentors" title="Xóa bộ lọc"><i class="fa-solid fa-rotate-left"></i></a>
                </form>

                <section class="assignment-card">
                    <div class="toolbar"><div class="card-heading"><div><h2>Danh sách mentor</h2></div><span>${mentors.size()} mentor</span></div>
                        <button type="button" class="btn primary" id="openCreate"><i class="fa-solid fa-user-plus"></i> Thêm mentor</button></div>
                    <div class="table-wrap">
                    <table class="mentor-table">
                        <thead><tr><th>Họ tên</th><th>Email</th><th>Phòng ban</th><th>Trạng thái</th><th>Tài khoản</th><th>TTS đang HD</th><th>Thao tác</th></tr></thead>
                        <tbody>
                        <c:forEach items="${mentors}" var="m">
                            <tr>
                                <td><strong><c:out value="${m.full_name}"/></strong><br><small><c:out value="${m.mentor_code}"/></small></td>
                                <td><c:out value="${m.email}"/></td>
                                <td><c:out value="${m.dept_name}" default="—"/></td>
                                <td><span class="badge ${m.status=='ACTIVE'?'green':'gray'}">${m.status=='ACTIVE'?'Đang hoạt động':'Ngừng hoạt động'}</span></td>
                                <td><c:choose>
                                    <c:when test="${empty m.user_id}"><span class="badge amber">Chưa liên kết tài khoản</span></c:when>
                                    <c:when test="${m.account_status!='ACTIVE'}"><span class="badge red">Tài khoản bị khóa</span></c:when>
                                    <c:otherwise><span class="badge green">Đã liên kết</span></c:otherwise></c:choose></td>
                                <td>${m.intern_count}</td>
                                <td><div class="row-actions">
                                    <button type="button" class="btn open-edit" data-id="${m.id}" data-version="${m.version}"
                                        data-name="<c:out value='${m.full_name}'/>" data-email="<c:out value='${m.email}'/>"
                                        data-department="${m.department_id}" data-linked="${empty m.user_id ? 0 : 1}"
                                        data-interns="${m.intern_count}"><i class="fa-solid fa-pen"></i> Sửa</button>
                                    <form method="post" class="toggle-form" data-interns="${m.intern_count}" data-active="${m.status=='ACTIVE'}">
                                        <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${m.id}">
                                        <input type="hidden" name="status" value="${m.status=='ACTIVE'?'INACTIVE':'ACTIVE'}">
                                        <input type="hidden" name="confirmDeactivate" value="0">
                                        <input type="hidden" name="f_departmentId" value="${selectedDepartmentId}"><input type="hidden" name="f_active" value="${selectedActive}"><input type="hidden" name="f_link" value="${selectedLink}"><input type="hidden" name="f_q" value="<c:out value='${keyword}'/>">
                                        <button class="btn" type="submit">${m.status=='ACTIVE'?'Ngừng':'Kích hoạt'}</button></form>
                                    <form method="post" onsubmit="return confirm('Xóa hồ sơ mentor này?')">
                                        <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${m.id}">
                                        <input type="hidden" name="f_departmentId" value="${selectedDepartmentId}"><input type="hidden" name="f_active" value="${selectedActive}"><input type="hidden" name="f_link" value="${selectedLink}"><input type="hidden" name="f_q" value="<c:out value='${keyword}'/>">
                                        <button class="btn danger" type="submit" ${m.assigned_total>0?'disabled title="Đã có phân công, chỉ có thể ngừng hoạt động"':''}><i class="fa-solid fa-trash"></i></button></form>
                                </div></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    </div>
                    <c:if test="${empty mentors}"><div class="empty-state"><i class="fa-regular fa-folder-open"></i><strong>Chưa có mentor phù hợp</strong><p>Hãy đổi bộ lọc hoặc thêm mentor mới.</p></div></c:if>
                </section>
            </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>

<dialog id="mentorDialog" <c:if test="${not empty form}">data-open="1"</c:if>>
    <form method="post" class="dialog-body" id="mentorForm">
        <h2 id="dialogTitle">Thêm mentor</h2>
        <c:if test="${not empty form.error}"><div class="alert error"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${form.error}"/></div></c:if>
        <input type="hidden" name="action" id="fAction" value="${empty form ? 'create' : form.mode}">
        <input type="hidden" name="token" value="${formToken}">
        <input type="hidden" name="id" id="fId" value="<c:out value='${form.id}'/>">
        <input type="hidden" name="version" id="fVersion" value="<c:out value='${form.version}'/>">
        <input type="hidden" name="linked" id="fLinked" value="${form.linked ? 1 : 0}">
        <input type="hidden" name="internCount" id="fInterns" value="<c:out value='${form.internCount}'/>">
        <input type="hidden" name="confirmDepartment" id="fConfirm" value="0">
        <input type="hidden" name="f_departmentId" value="${selectedDepartmentId}"><input type="hidden" name="f_active" value="${selectedActive}"><input type="hidden" name="f_link" value="${selectedLink}"><input type="hidden" name="f_q" value="<c:out value='${keyword}'/>">
        <label>Họ tên *<input name="fullName" id="fName" maxlength="100" required value="<c:out value='${form.fullName}'/>"></label>
        <label>Email *<input name="email" id="fEmail" type="email" maxlength="100" required value="<c:out value='${form.email}'/>">
            <span class="field-hint" id="emailHint"></span></label>
        <label>Phòng ban *<select name="departmentId" id="fDept" required><option value="">-- Chọn phòng ban --</option>
            <c:forEach items="${departments}" var="d"><option value="${d.id}" ${form.departmentId==d.id?'selected':''}><c:out value="${d.dept_name}"/></option></c:forEach></select></label>
        <div class="dialog-footer"><button type="button" class="btn" id="closeDialog">Đóng</button><button type="submit" class="btn primary" id="saveBtn">Lưu</button></div>
    </form>
</dialog>

<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
<script>
(function () {
    const dlg = document.getElementById('mentorDialog');
    if (!dlg) return;
    const $ = id => document.getElementById(id);
    let originalDept = $('fDept').value;

    function setMode(mode) {
        $('dialogTitle').textContent = mode === 'update' ? 'Sửa hồ sơ mentor' : 'Thêm mentor';
        $('fAction').value = mode;
        const linked = $('fLinked').value === '1' && mode === 'update';
        $('fEmail').readOnly = linked;
        $('emailHint').textContent = linked ? 'Đã liên kết tài khoản: email là khóa liên kết, liên hệ quản trị viên để đổi.' : '';
    }
    $('openCreate')?.addEventListener('click', () => {
        ['fId','fVersion','fName','fEmail','fInterns'].forEach(i => $(i).value = '');
        $('fLinked').value = '0'; $('fDept').value = ''; originalDept = ''; $('fConfirm').value = '0';
        dlg.querySelectorAll('.alert').forEach(a => a.remove());
        setMode('create'); dlg.showModal();
    });
    document.querySelectorAll('.open-edit').forEach(btn => btn.addEventListener('click', () => {
        $('fId').value = btn.dataset.id; $('fVersion').value = btn.dataset.version;
        $('fName').value = btn.dataset.name; $('fEmail').value = btn.dataset.email;
        $('fDept').value = btn.dataset.department; originalDept = btn.dataset.department;
        $('fLinked').value = btn.dataset.linked; $('fInterns').value = btn.dataset.interns; $('fConfirm').value = '0';
        dlg.querySelectorAll('.alert').forEach(a => a.remove());
        setMode('update'); dlg.showModal();
    }));
    $('closeDialog').addEventListener('click', () => dlg.close());

    // Đổi phòng ban của mentor đang có TTS -> bắt xác nhận, nêu rõ số người bị ảnh hưởng
    $('mentorForm').addEventListener('submit', e => {
        const n = Number($('fInterns').value || 0);
        if ($('fAction').value === 'update' && n > 0 && $('fDept').value !== originalDept) {
            if (!confirm('Mentor đang hướng dẫn ' + n + ' thực tập sinh. Phân công cũ giữ nguyên, '
                    + 'phân công mới phải cùng phòng ban. Tiếp tục đổi phòng ban?')) { e.preventDefault(); return; }
            $('fConfirm').value = '1';
        }
        $('saveBtn').disabled = true; // chặn bấm lưu 2 lần (server còn có token chống trùng)
    });

    // Ngừng hoạt động: nêu số TTS đang hướng dẫn
    document.querySelectorAll('.toggle-form').forEach(f => f.addEventListener('submit', e => {
        const n = Number(f.dataset.interns || 0);
        if (f.dataset.active === 'true' && n > 0) {
            if (!confirm('Mentor đang hướng dẫn ' + n + ' thực tập sinh. Họ vẫn giữ mentor này, hãy đổi mentor nếu cần. Ngừng hoạt động?')) {
                e.preventDefault(); return;
            }
            f.querySelector('[name=confirmDeactivate]').value = '1';
        }
    }));

    // Render lại sau lỗi: mở lại hộp thoại với dữ liệu đã nhập
    if (dlg.dataset.open === '1') { setMode($('fAction').value); dlg.showModal(); }
})();
</script>
</body>
</html>