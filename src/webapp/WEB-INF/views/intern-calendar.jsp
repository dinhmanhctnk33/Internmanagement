<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Lịch thực tập của tôi - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/sprint2-schedule.css?v=20261010-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261008-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-graduate"></i></div><div><h2>IMS Portal</h2><span>Dành cho Thực tập sinh</span></div></div>
        <nav class="menu"><p class="menu-title">TRANG CÁ NHÂN</p><a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-house"></i><span>Trang cá nhân</span></a><a href="${pageContext.request.contextPath}/interns/profile" class="menu-item"><i class="fa-solid fa-id-card"></i><span>Hồ sơ của tôi</span></a><a href="${pageContext.request.contextPath}/my-contract" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng của tôi</span></a><a href="${pageContext.request.contextPath}/my-calendar" class="menu-item active"><i class="fa-solid fa-calendar-days"></i><span>Lịch thực tập</span></a><a href="${pageContext.request.contextPath}/attendance" class="menu-item"><i class="fa-solid fa-clock"></i><span>Check-in / Check-out</span></a></nav>
        
    </aside>
    <main class="main">
        <header class="header intern-header"><div class="header-context"><i class="fa-regular fa-calendar"></i><span>Kế hoạch cá nhân</span></div><div class="header-right"><div class="notification-wrap"><button class="notification" id="notificationButton" type="button" aria-label="Thông báo" aria-expanded="false"><i class="fa-regular fa-bell"></i></button><div class="notification-menu" id="notificationMenu" hidden><h3><i class="fa-solid fa-bell"></i> Thông báo</h3><div class="notification-empty">Xem các thông báo mới nhất của bạn.</div><a class="notification-item notification-all" href="${pageContext.request.contextPath}/notifications">Xem tất cả thông báo <i class="fa-solid fa-arrow-right"></i></a></div></div><div class="account-menu"><button class="account-trigger user" id="accountMenuButton" type="button" aria-expanded="false"><div class="avatar">${currentUser.shortName}</div><div class="user-info"><strong><c:out value="${currentUser.fullName}"/></strong><small>Thực tập sinh (INTERN)</small></div><i class="fa-solid fa-chevron-down account-chevron"></i></button><div class="account-dropdown" id="accountMenu" hidden><a href="${pageContext.request.contextPath}/change-password"><i class="fa-solid fa-key"></i> Đổi mật khẩu</a><a class="logout-link" href="${pageContext.request.contextPath}/logout"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a></div></div></div></header>
        <section class="content schedule-page">
            <div class="schedule-heading"><div><p class="eyebrow">Kế hoạch cá nhân</p><h1>Lịch thực tập của tôi</h1><p>Theo dõi thời gian chương trình, hợp đồng và các mốc quan trọng trong cùng một lịch.</p></div><a class="today-action" href="${pageContext.request.contextPath}/my-calendar"><i class="fa-solid fa-location-crosshairs"></i> Tháng hiện tại</a></div>
            <c:choose>
                <c:when test="${empty schedule}"><div class="schedule-card empty-state large"><i class="fa-regular fa-calendar-xmark"></i><h2>Chưa có lịch thực tập</h2><p>Hồ sơ thực tập chưa được thiết lập. Vui lòng liên hệ HR để được hỗ trợ.</p></div></c:when>
                <c:otherwise>
                    <section class="calendar-summary">
                        <div class="summary-main"><span class="summary-icon"><i class="fa-solid fa-briefcase"></i></span><div><small>CHƯƠNG TRÌNH</small><h2><c:out value="${schedule.program_name}" default="Chưa phân chương trình"/></h2><p><c:out value="${schedule.program_code}"/> <c:if test="${not empty schedule.dept_name}">· <c:out value="${schedule.dept_name}"/></c:if></p></div></div>
                        <div class="summary-date"><small>BẮT ĐẦU</small><strong>${schedule.effective_start}</strong></div><div class="summary-line"><span></span><i class="fa-solid fa-arrow-right"></i></div><div class="summary-date"><small>KẾT THÚC</small><strong>${schedule.effective_end}</strong></div>
                    </section>
                    <div class="calendar-layout">
                        <section class="schedule-card calendar-card">
                            <div class="calendar-toolbar"><a href="?month=${previousMonth}" aria-label="Tháng trước"><i class="fa-solid fa-chevron-left"></i></a><h2>${monthLabel}</h2><a href="?month=${nextMonth}" aria-label="Tháng sau"><i class="fa-solid fa-chevron-right"></i></a></div>
                            <div class="calendar-grid calendar-weekdays"><span>Thứ 2</span><span>Thứ 3</span><span>Thứ 4</span><span>Thứ 5</span><span>Thứ 6</span><span>Thứ 7</span><span>CN</span></div>
                            <div class="calendar-grid calendar-days"><c:forEach items="${calendarWeeks}" var="week"><c:forEach items="${week}" var="day"><div class="calendar-day ${day.inMonth?'':'outside'} ${day.today?'today':''}"><span class="day-number">${day.day}</span><div class="day-events"><c:forEach items="${day.events}" var="event"><div class="calendar-event ${event.type}" title="${event.description}"><i class="fa-solid ${event.type=='contract'?'fa-file-signature':event.type=='end'?'fa-flag-checkered':'fa-circle'}"></i><c:out value="${event.title}"/></div></c:forEach></div></div></c:forEach></c:forEach></div>
                        </section>
                        <aside class="calendar-side">
                            <section class="schedule-card details-card"><div class="section-title compact"><div><h2>Thông tin kỳ thực tập</h2><p>Dữ liệu chỉ đọc</p></div></div><dl><div><dt><i class="fa-solid fa-id-badge"></i> Mã thực tập sinh</dt><dd><c:out value="${schedule.intern_code}"/></dd></div><div><dt><i class="fa-solid fa-user-tie"></i> Mentor</dt><dd><c:out value="${schedule.mentor_name}" default="Chưa phân công"/></dd></div><div><dt><i class="fa-solid fa-file-contract"></i> Hợp đồng</dt><dd><c:out value="${schedule.contract_number}" default="Chưa có"/></dd></div></dl></section>
                            <section class="schedule-card event-list"><div class="section-title compact"><div><h2>Các mốc quan trọng</h2><p>${events.size()} sự kiện</p></div></div><c:forEach items="${events}" var="event"><div class="event-item"><span class="event-dot ${event.type}"></span><div><time>${event.date}</time><strong><c:out value="${event.title}"/></strong><c:if test="${not empty event.description}"><small><c:out value="${event.description}"/></small></c:if></div></div></c:forEach><c:if test="${empty events}"><p class="muted-empty">Chưa có mốc thời gian.</p></c:if></section>
                        </aside>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261008-1"></script>
</body>
</html>
