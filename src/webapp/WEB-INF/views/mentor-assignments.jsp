<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Phân công mentor - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-assignments.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
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
            <a href="${pageContext.request.contextPath}/programs" class="menu-item"><i class="fa-solid fa-calendar-days"></i><span>Chương trình thực tập</span></a>
            <a href="${pageContext.request.contextPath}/applications" class="menu-item"><i class="fa-solid fa-file-signature"></i><span>Xét duyệt hồ sơ</span></a>
            <a href="${pageContext.request.contextPath}/contracts" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng thực tập</span></a>
            <a href="${pageContext.request.contextPath}/mentor-assignments" class="menu-item active"><i class="fa-solid fa-user-group"></i><span>Phân công mentor</span></a>
            <a href="${pageContext.request.contextPath}/attendance-report" class="menu-item"><i class="fa-solid fa-chart-column"></i><span>Báo cáo chuyên cần</span></a>
        </nav>
        
    </aside>
    <main class="main">
        <header class="header intern-header"><div class="page-context header-context"><i class="fa-solid fa-people-arrows-left-right"></i><span>Tổ chức hướng dẫn</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content mentor-page">
            <div class="page-heading"><div><p class="eyebrow">Tổ chức hướng dẫn</p><h1>Phân công mentor</h1><p>Ghép thực tập sinh với mentor phù hợp theo phòng ban và sức chứa hiện tại.</p></div><div class="heading-note"><i class="fa-solid fa-circle-info"></i><span>Hệ thống tự kiểm tra phòng ban và giới hạn hướng dẫn trước khi lưu.</span></div></div>

            <c:if test="${param.success=='assigned'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã phân công mentor thành công.</div></c:if>
            <c:if test="${param.success=='unassigned'}"><div class="alert success"><i class="fa-solid fa-circle-check"></i> Đã hủy phân công mentor.</div></c:if>
            <c:if test="${not empty param.error}"><div class="alert error"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>

            <div class="metric-grid">
                <article><span class="metric-icon blue"><i class="fa-solid fa-user-graduate"></i></span><div><small>Thực tập sinh</small><strong>${metrics.total_interns}</strong><p>Đang hoạt động</p></div></article>
                <article><span class="metric-icon green"><i class="fa-solid fa-user-check"></i></span><div><small>Đã phân công</small><strong>${metrics.assigned_interns}</strong><p>Đã có người hướng dẫn</p></div></article>
                <article><span class="metric-icon amber"><i class="fa-solid fa-user-clock"></i></span><div><small>Chưa phân công</small><strong>${metrics.unassigned_interns}</strong><p>Cần HR xử lý</p></div></article>
                <article><span class="metric-icon purple"><i class="fa-solid fa-chalkboard-user"></i></span><div><small>Mentor hoạt động</small><strong>${metrics.active_mentors}</strong><p>Tổng sức chứa ${metrics.total_capacity}</p></div></article>
            </div>

            <form method="get" class="filter-bar">
                <label><span>Chương trình</span><select name="programId"><option value="">Tất cả chương trình</option><c:forEach items="${programs}" var="p"><option value="${p.id}" ${selectedProgramId==p.id?'selected':''}><c:out value="${p.program_name}"/></option></c:forEach></select></label>
                <label><span>Trạng thái phân công</span><select name="assignment"><option value="ALL" ${selectedAssignment=='ALL'?'selected':''}>Tất cả</option><option value="UNASSIGNED" ${selectedAssignment=='UNASSIGNED'?'selected':''}>Chưa có mentor</option><option value="ASSIGNED" ${selectedAssignment=='ASSIGNED'?'selected':''}>Đã có mentor</option></select></label>
                <label class="search-field"><span>Tìm thực tập sinh</span><div><i class="fa-solid fa-magnifying-glass"></i><input name="q" value="<c:out value='${keyword}'/>" placeholder="Tên hoặc mã TTS"></div></label>
                <button type="submit"><i class="fa-solid fa-filter"></i> Lọc danh sách</button>
                <a href="${pageContext.request.contextPath}/mentor-assignments" title="Xóa bộ lọc"><i class="fa-solid fa-rotate-left"></i></a>
            </form>

            <div class="workspace-grid">
                <section class="assignment-card">
                    <div class="card-heading"><div><h2>Danh sách thực tập sinh</h2><p>Ưu tiên các trường hợp chưa được phân công.</p></div><span>${interns.size()} kết quả</span></div>
                    <div class="intern-list">
                        <c:forEach items="${interns}" var="intern">
                            <article class="intern-row ${empty intern.mentor_id?'needs-assignment':''}">
                                <div class="intern-person"><span class="person-avatar"><i class="fa-solid fa-user-graduate"></i></span><div><strong><c:out value="${intern.full_name}"/></strong><small><c:out value="${intern.intern_code}"/> · <c:out value="${intern.email}"/></small></div></div>
                                <div class="program-info"><small>CHƯƠNG TRÌNH</small><strong><c:out value="${intern.program_name}" default="Chưa phân chương trình"/></strong><span><i class="fa-regular fa-building"></i> <c:out value="${intern.dept_name}" default="Chưa xác định phòng ban"/></span></div>
                                <div class="current-mentor"><small>MENTOR HIỆN TẠI</small><c:choose><c:when test="${not empty intern.mentor_id}"><strong><i class="fa-solid fa-circle-check"></i> <c:out value="${intern.mentor_name}"/></strong><span><c:out value="${intern.mentor_code}"/></span></c:when><c:otherwise><strong class="unassigned"><i class="fa-regular fa-circle"></i> Chưa phân công</strong><span>Cần chọn người hướng dẫn</span></c:otherwise></c:choose></div>
                                <div class="assignment-action">
                                    <form method="post">
                                        <input type="hidden" name="profileId" value="${intern.id}"><input type="hidden" name="action" value="assign">
                                        <select name="mentorId" required aria-label="Chọn mentor cho ${intern.full_name}">
                                            <option value="">Chọn mentor phù hợp</option>
                                            <c:forEach items="${mentors}" var="mentor">
                                                <c:set var="wrongDepartment" value="${not empty intern.department_id and not empty mentor.department_id and intern.department_id != mentor.department_id}"/>
                                                <c:set var="fullMentor" value="${mentor.load_count >= mentor.max_capacity and intern.mentor_id != mentor.id}"/>
                                                <option value="${mentor.id}" ${intern.mentor_id==mentor.id?'selected':''} ${wrongDepartment or fullMentor?'disabled':''}><c:out value="${mentor.full_name}"/> · <c:out value="${mentor.dept_name}" default="Liên phòng ban"/> · ${mentor.load_count}/${mentor.max_capacity}${wrongDepartment?' · khác phòng ban':fullMentor?' · đã đủ':''}</option>
                                            </c:forEach>
                                        </select>
                                        <button type="submit"><i class="fa-solid fa-user-plus"></i> ${empty intern.mentor_id?'Phân công':'Cập nhật'}</button>
                                    </form>
                                    <c:if test="${not empty intern.mentor_id}"><form method="post" class="unassign-form" onsubmit="return confirm('Hủy phân công mentor của thực tập sinh này?')"><input type="hidden" name="profileId" value="${intern.id}"><input type="hidden" name="action" value="unassign"><button type="submit" title="Hủy phân công"><i class="fa-solid fa-link-slash"></i></button></form></c:if>
                                </div>
                            </article>
                        </c:forEach>
                        <c:if test="${empty interns}"><div class="empty-state"><i class="fa-regular fa-folder-open"></i><strong>Không có thực tập sinh phù hợp</strong><p>Hãy thay đổi chương trình, trạng thái hoặc từ khóa tìm kiếm.</p></div></c:if>
                    </div>
                </section>

                <aside class="mentor-panel">
                    <div class="card-heading"><div><h2>Tải hướng dẫn</h2><p>Số TTS đang phụ trách trên sức chứa.</p></div><span>${mentors.size()} mentor</span></div>
                    <div class="mentor-list">
                        <c:forEach items="${mentors}" var="mentor">
                            <article class="mentor-item">
                                <div class="mentor-title"><span><i class="fa-solid fa-chalkboard-user"></i></span><div><strong><c:out value="${mentor.full_name}"/></strong><small><c:out value="${mentor.mentor_code}"/> · <c:out value="${mentor.dept_name}" default="Liên phòng ban"/></small></div><b class="${mentor.load_count>=mentor.max_capacity?'full':''}">${mentor.load_count}/${mentor.max_capacity}</b></div>
                                <div class="capacity-track"><span style="width:${mentor.max_capacity>0 ? (mentor.load_count*100/mentor.max_capacity > 100 ? 100 : mentor.load_count*100/mentor.max_capacity) : 100}%"></span></div>
                                <p><c:choose><c:when test="${mentor.load_count>=mentor.max_capacity}">Đã đủ số lượng hướng dẫn</c:when><c:otherwise>Còn ${mentor.max_capacity-mentor.load_count} vị trí</c:otherwise></c:choose></p>
                            </article>
                        </c:forEach>
                        <c:if test="${empty mentors}"><div class="empty-state compact">Chưa có mentor đang hoạt động.</div></c:if>
                    </div>
                </aside>
            </div>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
