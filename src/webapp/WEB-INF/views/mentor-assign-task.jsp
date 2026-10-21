<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Giao nhiệm vụ - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-workspace.css?v=20261012-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-tasks.css?v=20261013-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
    <aside class="sidebar mentor-sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-chalkboard-user"></i></div><div><h2>IMS Portal</h2><span>Không gian Mentor</span></div></div>
        <nav class="menu">
            <p class="menu-title">HƯỚNG DẪN THỰC TẬP</p>
            <a class="menu-item" href="${pageContext.request.contextPath}/mentor/dashboard"><i class="fa-solid fa-chart-pie"></i><span>Tổng quan</span></a>
            <a class="menu-item active" href="${pageContext.request.contextPath}/mentor/tasks/assign"><i class="fa-solid fa-list-check"></i><span>Giao nhiệm vụ</span></a>
            <a class="menu-item" href="${pageContext.request.contextPath}/mentor/reports"><i class="fa-solid fa-file-circle-check"></i><span>Báo cáo tuần</span></a>
            <a class="menu-item" href="${pageContext.request.contextPath}/mentor/final-evaluation"><i class="fa-solid fa-star-half-stroke"></i><span>Đánh giá tổng kết</span></a>
            <a class="menu-item" href="${pageContext.request.contextPath}/meetings"><i class="fa-solid fa-video"></i><span>Lịch họp</span></a>
        </nav>
    </aside>
    <main class="main">
        <header class="header intern-header"><div class="mentor-context header-context"><i class="fa-solid fa-list-check"></i><span>Giao nhiệm vụ</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content mentor-content task-content">
            <div class="report-page-heading">
                <div><p class="eyebrow">MENTOR · QUẢN LÝ CÔNG VIỆC</p><h1>Giao nhiệm vụ</h1><p>Tạo đầu việc cụ thể, đặt thời hạn và phân công cho một hoặc nhiều thực tập sinh trong nhóm.</p></div>
                <a href="${pageContext.request.contextPath}/mentor/dashboard"><i class="fa-solid fa-arrow-left"></i> Về tổng quan</a>
            </div>

            <c:if test="${not empty successMessage}"><div class="task-alert success" role="status"><i class="fa-solid fa-circle-check"></i><c:out value="${successMessage}"/></div></c:if>
            <c:if test="${not empty errorMessage}"><div class="task-alert error" role="alert"><i class="fa-solid fa-triangle-exclamation"></i><c:out value="${errorMessage}"/></div></c:if>

            <div class="task-grid">
                <section class="task-form-card">
                    <div class="task-card-heading"><span><i class="fa-solid fa-clipboard-list"></i></span><div><h2>Thông tin nhiệm vụ</h2><p>Các trường có dấu * là bắt buộc.</p></div></div>
                    <c:choose>
                        <c:when test="${empty internList}">
                            <div class="task-empty"><i class="fa-solid fa-users-slash"></i><h3>Chưa có thực tập sinh</h3><p>HR chưa phân công thực tập sinh đang hoạt động cho bạn.</p></div>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/mentor/tasks/assign" method="post" id="assignTaskForm" novalidate>
                                <label class="task-field"><span>Tiêu đề nhiệm vụ <em>*</em></span><input type="text" name="title" value="<c:out value='${param.title}'/>" maxlength="200" required placeholder="Ví dụ: Hoàn thiện API báo cáo tuần"><c:if test="${not empty titleError}"><small class="field-error"><c:out value="${titleError}"/></small></c:if></label>
                                <label class="task-field"><span>Mô tả công việc</span><textarea name="description" rows="6" maxlength="2000" placeholder="Nêu yêu cầu, kết quả mong đợi và tài liệu tham khảo..."><c:out value="${param.description}"/></textarea><c:if test="${not empty descriptionError}"><small class="field-error"><c:out value="${descriptionError}"/></small></c:if></label>
                                <label class="task-field"><span>Hạn hoàn thành</span><input type="date" name="dueDate" min="${todayDate}" value="<c:out value='${param.dueDate}'/>"><c:if test="${not empty dueDateError}"><small class="field-error"><c:out value="${dueDateError}"/></small></c:if></label>
                                <div class="task-field"><span>Người nhận <em>*</em></span><div class="assignee-toolbar"><label><input type="checkbox" id="selectAll"> Chọn tất cả</label><small>${internList.size()} thành viên đang hoạt động</small></div><div class="assignee-list"><c:forEach var="intern" items="${internList}"><label class="assignee-item" for="intern_${intern.id}"><input type="checkbox" class="intern-checkbox" name="internIds" value="${intern.id}" id="intern_${intern.id}" <c:if test="${selectedInterns != null && selectedInterns.contains(intern.id)}">checked</c:if>><span class="assignee-avatar"><i class="fa-solid fa-user-graduate"></i></span><span><strong><c:out value="${intern.fullName}"/></strong><small><c:out value="${intern.internCode}"/> · <c:out value="${intern.email}"/></small></span></label></c:forEach></div><c:if test="${not empty internIdsError}"><small class="field-error"><c:out value="${internIdsError}"/></small></c:if></div>
                                <div class="task-actions"><a href="${pageContext.request.contextPath}/mentor/dashboard">Hủy</a><button type="submit"><i class="fa-solid fa-paper-plane"></i> Giao nhiệm vụ</button></div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </section>
                <aside class="task-guide-card"><div class="guide-icon"><i class="fa-solid fa-lightbulb"></i></div><h2>Giao việc hiệu quả</h2><ul><li><i class="fa-solid fa-check"></i><span><strong>Tiêu đề rõ ràng</strong>Nêu cụ thể kết quả cần hoàn thành.</span></li><li><i class="fa-solid fa-check"></i><span><strong>Mô tả đủ ngữ cảnh</strong>Đính kèm yêu cầu và tài liệu liên quan.</span></li><li><i class="fa-solid fa-check"></i><span><strong>Thời hạn phù hợp</strong>Đảm bảo thực tập sinh có đủ thời gian xử lý.</span></li></ul><div class="guide-note"><i class="fa-solid fa-shield-halved"></i><p>Bạn chỉ có thể giao việc cho thực tập sinh đang thuộc nhóm hướng dẫn của mình.</p></div></aside>
            </div>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261013-2"></script>
<script>document.getElementById('selectAll')?.addEventListener('change', function () { document.querySelectorAll('.intern-checkbox').forEach(item => item.checked = this.checked); });</script>
</body>
</html>
