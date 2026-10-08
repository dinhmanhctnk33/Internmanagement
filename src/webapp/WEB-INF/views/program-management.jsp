<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Chương trình thực tập - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/sprint2-schedule.css?v=20261008-2">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
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
            <a href="${pageContext.request.contextPath}/programs" class="menu-item active"><i class="fa-solid fa-calendar-days"></i><span>Chương trình thực tập</span></a>
            <a href="${pageContext.request.contextPath}/applications" class="menu-item"><i class="fa-solid fa-file-signature"></i><span>Xét duyệt hồ sơ</span></a>
            <a href="${pageContext.request.contextPath}/contracts" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng thực tập</span></a>
        </nav>
        <div class="sidebar-bottom"><a href="${pageContext.request.contextPath}/change-password" class="menu-item"><i class="fa-solid fa-key"></i><span>Đổi mật khẩu</span></a><a href="${pageContext.request.contextPath}/logout" class="menu-item logout"><i class="fa-solid fa-right-from-bracket"></i><span>Đăng xuất</span></a></div>
    </aside>
    <main class="main">
        <header class="header"><div class="header-context"><i class="fa-solid fa-calendar-check"></i><span>Quản lý kế hoạch thực tập</span></div><div class="user"><div class="avatar">${currentUser.shortName}</div><div class="user-info"><strong><c:out value="${currentUser.fullName}"/></strong><small>Nhân sự (HR)</small></div></div></header>
        <section class="content schedule-page">
            <div class="schedule-heading"><div><p class="eyebrow">KẾ HOẠCH THEO PHÒNG BAN</p><h1>Chương trình thực tập</h1><p>Tạo chương trình và quản lý chính xác ngày bắt đầu, ngày kết thúc của từng kỳ thực tập.</p></div><a class="primary-action" href="#create-program"><i class="fa-solid fa-plus"></i> Tạo chương trình</a></div>

            <c:if test="${param.success == 'created'}"><div class="feedback success"><i class="fa-solid fa-circle-check"></i> Đã tạo chương trình thực tập thành công.</div></c:if>
            <c:if test="${param.success == 'updated'}"><div class="feedback success"><i class="fa-solid fa-circle-check"></i> Đã cập nhật thời gian chương trình.</div></c:if>
            <c:if test="${not empty param.error}"><div class="feedback error"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>

            <div class="program-metrics">
                <div><span><i class="fa-solid fa-layer-group"></i></span><p><strong>${programs.size()}</strong> chương trình</p></div>
                <div><span><i class="fa-solid fa-building"></i></span><p><strong>${departments.size()}</strong> phòng ban hoạt động</p></div>
                <div><span><i class="fa-solid fa-circle-info"></i></span><p>Thời gian hợp lệ khi ngày kết thúc không trước ngày bắt đầu</p></div>
            </div>

            <section class="schedule-card list-section">
                <div class="section-title"><div><h2>Danh sách chương trình</h2><p>Cập nhật nhanh thời gian và trạng thái ngay trên từng chương trình.</p></div><span class="count-chip">${programs.size()} chương trình</span></div>
                <div class="program-list">
                    <c:forEach items="${programs}" var="p">
                        <article class="program-row">
                            <div class="program-identity"><span class="program-icon"><i class="fa-solid fa-briefcase"></i></span><div><h3><c:out value="${p.program_name}"/></h3><p><c:out value="${p.program_code}"/> · <c:out value="${p.dept_name}"/></p></div></div>
                            <div class="program-capacity"><small>THỰC TẬP SINH</small><strong>${p.intern_count}<span> / ${p.quota_count}</span></strong></div>
                            <form method="post" class="date-editor">
                                <input type="hidden" name="action" value="update-dates">
                                <input type="hidden" name="programId" value="${p.id}">
                                <label><span>Ngày bắt đầu</span><input type="date" name="startDate" value="${p.start_date}" required></label>
                                <span class="date-arrow"><i class="fa-solid fa-arrow-right"></i></span>
                                <label><span>Ngày kết thúc</span><input type="date" name="endDate" value="${p.end_date}" required></label>
                                <label class="status-field"><span>Trạng thái</span><select name="status"><option value="PLANNING" ${p.status=='PLANNING'?'selected':''}>Lập kế hoạch</option><option value="ACTIVE" ${p.status=='ACTIVE'?'selected':''}>Đang diễn ra</option><option value="COMPLETED" ${p.status=='COMPLETED'?'selected':''}>Đã kết thúc</option><option value="CANCELLED" ${p.status=='CANCELLED'?'selected':''}>Đã hủy</option></select></label>
                                <button type="submit" class="save-dates" title="Lưu thời gian"><i class="fa-solid fa-floppy-disk"></i><span>Lưu</span></button>
                            </form>
                        </article>
                    </c:forEach>
                    <c:if test="${empty programs}"><div class="empty-state"><i class="fa-regular fa-calendar-plus"></i><h3>Chưa có chương trình</h3><p>Tạo chương trình đầu tiên bằng biểu mẫu bên dưới.</p></div></c:if>
                </div>
            </section>

            <section class="schedule-card create-section" id="create-program">
                <div class="section-title"><div><h2>Tạo chương trình mới</h2><p>Thiết lập thông tin tổ chức và khoảng thời gian áp dụng.</p></div><span class="required-note">* Bắt buộc</span></div>
                <form method="post" class="program-form" id="programForm">
                    <input type="hidden" name="action" value="create">
                    <label><span>Mã chương trình *</span><input name="code" maxlength="50" required placeholder="VD: IT-2026-02"></label>
                    <label class="wide"><span>Tên chương trình *</span><input name="name" maxlength="200" required placeholder="VD: Thực tập sinh Java Backend"></label>
                    <label><span>Phòng ban *</span><select name="departmentId" required><option value="">Chọn phòng ban</option><c:forEach items="${departments}" var="d"><option value="${d.id}"><c:out value="${d.dept_name}"/></option></c:forEach></select></label>
                    <label><span>Chỉ tiêu *</span><input type="number" name="quota" min="1" required placeholder="10"></label>
                    <label><span>Ngày bắt đầu *</span><input type="date" name="startDate" required></label>
                    <label><span>Ngày kết thúc *</span><input type="date" name="endDate" required></label>
                    <label><span>Trạng thái</span><select name="status"><option value="PLANNING">Lập kế hoạch</option><option value="ACTIVE">Đang diễn ra</option></select></label>
                    <label class="full"><span>Mô tả chương trình</span><textarea name="description" rows="3" placeholder="Mục tiêu, nội dung hoặc lưu ý của chương trình"></textarea></label>
                    <div class="form-actions full"><button type="reset" class="secondary-action">Nhập lại</button><button type="submit" class="primary-action"><i class="fa-solid fa-plus"></i> Tạo chương trình</button></div>
                </form>
            </section>
        </section>
    </main>
</div>
<script>
document.querySelectorAll('form').forEach(function(form){
  form.addEventListener('submit',function(event){
    var start=form.querySelector('[name="startDate"]'), end=form.querySelector('[name="endDate"]');
    if(start&&end&&start.value&&end.value&&end.value<start.value){event.preventDefault();end.setCustomValidity('Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.');end.reportValidity();}
  });
  var end=form.querySelector('[name="endDate"]'); if(end)end.addEventListener('input',function(){this.setCustomValidity('');});
});
</script>
</body>
</html>
