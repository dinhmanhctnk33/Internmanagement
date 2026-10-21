<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch làm việc linh hoạt - IMS Portal</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261014">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261014">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/work-schedules.css?v=20261014-4">
</head>
<body>
<div class="layout">
    <c:set var="activeMenu" value="work-schedules"/>
    <%@ include file="includes/final-report-sidebar.jspf" %>
    <main class="main">
        <header class="header intern-header">
            <div class="report-context header-context"><i class="fa-regular fa-calendar-check"></i><span>Lịch làm việc linh hoạt</span></div>
            <%@ include file="includes/hr-header-actions.jspf" %>
        </header>

        <section class="content schedule-page">
            <section class="page-heading">
                <div>
                    <span class="eyebrow">QUẢN LÝ THỜI GIAN</span>
                    <h1>Thiết lập lịch làm việc theo nhóm</h1>
                    <p>Cấu hình khung giờ và ngày làm việc riêng cho từng phòng ban.</p>
                </div>
                <div class="summary-chip"><i class="fa-solid fa-layer-group"></i><strong>${fn:length(schedules)}</strong><span>nhóm đã thiết lập</span></div>
            </section>

            <c:if test="${not empty param.success}">
                <div class="flash success"><i class="fa-solid fa-circle-check"></i><span>${param.success == 'deleted' ? 'Đã xóa lịch làm việc.' : param.success == 'updated' ? 'Đã cập nhật lịch làm việc.' : 'Đã tạo lịch làm việc.'}</span></div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="flash error"><i class="fa-solid fa-circle-exclamation"></i><span><c:out value="${param.error}"/></span></div>
            </c:if>

            <section class="schedule-card editor-card" id="scheduleEditor">
                <div class="card-heading">
                    <div class="heading-icon"><i class="fa-solid fa-calendar-plus"></i></div>
                    <div><h2>${empty editing ? 'Tạo lịch mới' : 'Cập nhật lịch làm việc'}</h2><p>Mỗi nhóm có một lịch riêng và có thể điều chỉnh bất cứ lúc nào.</p></div>
                </div>
                <form action="${pageContext.request.contextPath}/work-schedules" method="post" class="schedule-form">
                    <input type="hidden" name="action" value="${empty editing ? 'create' : 'update'}">
                    <c:if test="${not empty editing}"><input type="hidden" name="id" value="${editing.id}"></c:if>
                    <div class="form-grid">
                        <label class="field group-field"><span>Nhóm / phòng ban <b>*</b></span><select name="groupId" required><option value="">-- Chọn nhóm --</option><c:forEach var="group" items="${groups}"><option value="${group.id}" ${not empty editing && editing.groupId == group.id ? 'selected' : ''}><c:out value="${group.name}"/> (${group.code})</option></c:forEach></select></label>
                        <label class="field name-field"><span>Tên lịch <b>*</b></span><input type="text" name="shiftName" maxlength="100" required placeholder="Ví dụ: Lịch hành chính" value="<c:out value='${editing.shiftName}'/>"/></label>
                        <label class="field start-field"><span>Giờ bắt đầu <b>*</b></span><input type="time" name="startTime" required value="${empty editing ? '08:00' : editing.startTime}"></label>
                        <label class="field end-field"><span>Giờ kết thúc <b>*</b></span><input type="time" name="endTime" required value="${empty editing ? '17:30' : editing.endTime}"></label>
                        <fieldset class="field days-field"><legend>Ngày làm việc <b>*</b></legend><div class="day-options">
                            <c:set var="defaultDays" value="MON,TUE,WED,THU,FRI"/>
                            <c:forEach var="day" items="MON,TUE,WED,THU,FRI,SAT,SUN" varStatus="status"><label class="day-option"><input type="checkbox" name="workDays" value="${day}" ${(empty editing && fn:contains(defaultDays, day)) || (not empty editing && fn:contains(editing.workDays, day)) ? 'checked' : ''}><span>${status.index == 0 ? 'T2' : status.index == 1 ? 'T3' : status.index == 2 ? 'T4' : status.index == 3 ? 'T5' : status.index == 4 ? 'T6' : status.index == 5 ? 'T7' : 'CN'}</span></label></c:forEach>
                        </div></fieldset>
                        <label class="field note-field"><span>Ghi chú <small>(không bắt buộc)</small></span><textarea name="note" maxlength="1000" rows="2" placeholder="Thời gian nghỉ trưa, quy định riêng hoặc lưu ý cho nhóm..."><c:out value="${editing.note}"/></textarea></label>
                    </div>
                    <div class="form-actions"><c:if test="${not empty editing}"><a class="button secondary" href="${pageContext.request.contextPath}/work-schedules">Hủy chỉnh sửa</a></c:if><button class="button primary" type="submit"><i class="fa-solid fa-floppy-disk"></i>${empty editing ? 'Lưu lịch làm việc' : 'Lưu thay đổi'}</button></div>
                </form>
            </section>

            <section class="schedule-card list-card">
                <div class="card-heading list-heading"><div><h2>Lịch đang áp dụng</h2><p>Khung giờ hiện tại của các nhóm trong hệ thống.</p></div><span class="count-badge">${fn:length(schedules)} lịch</span></div>
                <c:choose><c:when test="${empty schedules}"><div class="empty-state"><div><i class="fa-regular fa-calendar"></i></div><h3>Chưa có lịch làm việc</h3><p>Hãy tạo lịch đầu tiên cho một nhóm ở biểu mẫu phía trên.</p></div></c:when><c:otherwise>
                    <div class="schedule-list"><c:forEach var="item" items="${schedules}"><article class="schedule-row">
                        <div class="group-mark"><i class="fa-solid fa-users"></i></div>
                        <div class="schedule-info"><h3><c:out value="${item.groupName}"/></h3><span><c:out value="${item.shiftName}"/></span><c:if test="${not empty item.note}"><p><c:out value="${item.note}"/></p></c:if></div>
                        <div class="schedule-detail"><span class="detail-label">KHUNG GIỜ</span><strong><i class="fa-regular fa-clock"></i> ${item.startTime} – ${item.endTime}</strong></div>
                        <div class="schedule-detail"><span class="detail-label">NGÀY LÀM VIỆC</span><div class="compact-days"><c:forEach var="day" items="MON,TUE,WED,THU,FRI,SAT,SUN" varStatus="status"><span class="${fn:contains(item.workDays, day) ? 'active' : ''}">${status.index == 0 ? 'T2' : status.index == 1 ? 'T3' : status.index == 2 ? 'T4' : status.index == 3 ? 'T5' : status.index == 4 ? 'T6' : status.index == 5 ? 'T7' : 'CN'}</span></c:forEach></div></div>
                        <div class="row-actions"><a class="icon-button" href="${pageContext.request.contextPath}/work-schedules?edit=${item.id}#scheduleEditor" title="Chỉnh sửa"><i class="fa-solid fa-pen"></i></a><button class="icon-button danger delete-trigger" type="button" data-id="${item.id}" data-name="<c:out value='${item.groupName}'/>" title="Xóa"><i class="fa-regular fa-trash-can"></i></button></div>
                    </article></c:forEach></div>
                </c:otherwise></c:choose>
            </section>
        </section>
    </main>
</div>

<div class="modal-backdrop" id="deleteModal" hidden><div class="confirm-modal" role="dialog" aria-modal="true" aria-labelledby="deleteTitle"><div class="modal-icon"><i class="fa-regular fa-trash-can"></i></div><h2 id="deleteTitle">Xóa lịch làm việc?</h2><p>Lịch của <strong id="deleteGroupName"></strong> sẽ bị xóa khỏi hệ thống.</p><div class="modal-actions"><button class="button secondary" type="button" id="cancelDelete">Hủy</button><form method="post" action="${pageContext.request.contextPath}/work-schedules"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" id="deleteId"><button class="button delete" type="submit">Xóa lịch</button></form></div></div></div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261013-2"></script>
<script src="${pageContext.request.contextPath}/frontend/js/work-schedules.js?v=20261014"></script>
</body>
</html>
