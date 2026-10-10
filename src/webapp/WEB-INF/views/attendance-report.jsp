<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Báo cáo chuyên cần - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261008-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
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
            <a href="${pageContext.request.contextPath}/attendance-report" class="menu-item active"><i class="fa-solid fa-chart-column"></i><span>Báo cáo chuyên cần</span></a>
        </nav>
        
    </aside>
    <main class="main">
        <header class="header intern-header"><div class="report-context header-context"><i class="fa-solid fa-chart-simple"></i><span>Theo dõi chuyên cần</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content attendance-page">
            <div class="report-heading"><div><p class="eyebrow">Báo cáo nhân sự</p><h1>Đi làm và nghỉ phép</h1><p>Tổng hợp giờ làm, bất thường chấm công và tình trạng nghỉ phép của thực tập sinh.</p></div><span class="report-period"><i class="fa-regular fa-calendar"></i> ${fromDate} — ${toDate}</span></div>

            <c:if test="${not empty errorMessage}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${errorMessage}"/></div></c:if>

            <form method="get" class="report-filter">
                <label><span>Chương trình</span><select name="programId"><option value="">Tất cả chương trình</option><c:forEach items="${programs}" var="p"><option value="${p.id}" ${selectedProgramId==p.id?'selected':''}><c:out value="${p.program_name}"/></option></c:forEach></select></label>
                <label class="search-field"><span>Thực tập sinh</span><div><i class="fa-solid fa-magnifying-glass"></i><input name="q" value="${keyword}" placeholder="Tên hoặc mã TTS"></div></label>
                <label><span>Từ ngày</span><input type="date" name="from" value="${fromDate}" required></label>
                <label><span>Đến ngày</span><input type="date" name="to" value="${toDate}" required></label>
                <button type="submit"><i class="fa-solid fa-filter"></i> Áp dụng</button>
                <a href="${pageContext.request.contextPath}/attendance-report" title="Xóa bộ lọc"><i class="fa-solid fa-rotate-left"></i></a>
            </form>

            <div class="metric-grid">
                <article><span class="metric-icon blue"><i class="fa-solid fa-users"></i></span><div><small>Thực tập sinh</small><strong>${metrics.interns}</strong><p>Trong kết quả lọc</p></div></article>
                <article><span class="metric-icon green"><i class="fa-solid fa-calendar-check"></i></span><div><small>Ngày đi làm</small><strong>${metrics.workDays}</strong><p>${metrics.totalHours} giờ ghi nhận</p></div></article>
                <article><span class="metric-icon amber"><i class="fa-solid fa-clock"></i></span><div><small>Đi muộn</small><strong>${metrics.lateDays}</strong><p>${metrics.missingCheckout} lượt thiếu check-out</p></div></article>
                <article><span class="metric-icon purple"><i class="fa-solid fa-person-walking-arrow-right"></i></span><div><small>Nghỉ đã duyệt</small><strong>${metrics.leaveDays}</strong><p>Ngày nghỉ trong kỳ</p></div></article>
            </div>

            <section class="report-card summary-card">
                <div class="card-heading"><div><h2>Tổng hợp theo thực tập sinh</h2><p>Đối chiếu nhanh mức độ chuyên cần trong khoảng thời gian đã chọn.</p></div><span>${report.size()} thực tập sinh</span></div>
                <div class="report-table-wrap"><table><thead><tr><th>THỰC TẬP SINH</th><th>CHƯƠNG TRÌNH</th><th>NGÀY LÀM</th><th>TỔNG GIỜ</th><th>ĐI MUỘN</th><th>VỀ SỚM</th><th>THIẾU CHECK-OUT</th><th>NGHỈ PHÉP</th></tr></thead><tbody>
                <c:forEach items="${report}" var="r"><tr><td><strong><c:out value="${r.full_name}"/></strong><small><c:out value="${r.intern_code}"/></small></td><td><c:out value="${r.program_name}" default="Chưa phân chương trình"/><small><c:out value="${r.dept_name}"/></small></td><td><b>${r.work_days}</b> ngày</td><td><b>${r.total_hours}</b> giờ</td><td><span class="number-pill ${r.late_days>0?'warning':'normal'}">${r.late_days}</span></td><td><span class="number-pill ${r.early_leave_days>0?'warning':'normal'}">${r.early_leave_days}</span></td><td><span class="number-pill ${r.missing_checkout>0?'danger':'normal'}">${r.missing_checkout}</span></td><td><b>${r.approved_leave_days}</b> ngày</td></tr></c:forEach>
                <c:if test="${empty report}"><tr><td colspan="8" class="empty-row"><i class="fa-regular fa-folder-open"></i><br>Không có thực tập sinh phù hợp bộ lọc.</td></tr></c:if>
                </tbody></table></div>
            </section>

            <div class="detail-grid">
                <section class="report-card">
                    <div class="card-heading"><div><h2>Nhật ký chấm công</h2><p>Tối đa 300 bản ghi gần nhất trong kỳ.</p></div><span>${attendanceRecords.size()} bản ghi</span></div>
                    <div class="report-table-wrap detail-table"><table><thead><tr><th>NGÀY</th><th>THỰC TẬP SINH</th><th>GIỜ VÀO</th><th>GIỜ RA</th><th>THỜI LƯỢNG</th><th>TRẠNG THÁI</th></tr></thead><tbody>
                    <c:forEach items="${attendanceRecords}" var="a"><tr><td><b>${a.work_date}</b></td><td><strong><c:out value="${a.full_name}"/></strong><small><c:out value="${a.intern_code}"/></small></td><td>${empty a.check_in_display?'—':a.check_in_display}</td><td>${empty a.check_out_display?'—':a.check_out_display}</td><td><c:choose><c:when test="${empty a.total_minutes}">—</c:when><c:otherwise>${a.total_minutes} phút</c:otherwise></c:choose></td><td><span class="status ${a.status}">${a.status=='ON_TIME'?'Đúng giờ':a.status=='LATE'?'Đi muộn':a.status=='EARLY_LEAVE'?'Về sớm':a.status=='ABSENT'?'Vắng':'Nghỉ phép'}</span></td></tr></c:forEach>
                    <c:if test="${empty attendanceRecords}"><tr><td colspan="6" class="empty-row">Chưa có dữ liệu chấm công trong kỳ.</td></tr></c:if>
                    </tbody></table></div>
                </section>

                <section class="report-card">
                    <div class="card-heading"><div><h2>Nghỉ phép</h2><p>Đơn có thời gian giao với kỳ báo cáo.</p></div><span>${leaveRequests.size()} đơn</span></div>
                    <div class="report-table-wrap detail-table"><table><thead><tr><th>THỰC TẬP SINH</th><th>LOẠI NGHỈ</th><th>THỜI GIAN</th><th>SỐ NGÀY</th><th>TRẠNG THÁI</th><th>THAO TÁC</th></tr></thead><tbody>
                    <c:forEach items="${leaveRequests}" var="l"><tr><td><strong><c:out value="${l.full_name}"/></strong><small><c:out value="${l.intern_code}"/></small></td><td>${l.leave_type=='SICK_LEAVE'?'Nghỉ ốm':l.leave_type=='PERSONAL_LEAVE'?'Nghỉ cá nhân':'Nghỉ không lương'}</td><td><b>${l.start_date}</b><small>đến ${l.end_date}</small></td><td>${l.total_days}</td><td><span class="leave-status ${l.approval_status}">${l.approval_status=='APPROVED'?'Đã duyệt':l.approval_status=='REJECTED'?'Từ chối':'Chờ duyệt'}</span></td><td><c:if test="${l.approval_status=='PENDING'}"><form action="${pageContext.request.contextPath}/leave-requests" method="post" class="leave-review-form"><input type="hidden" name="leaveId" value="${l.id}"><input name="note" placeholder="Ghi chú (không bắt buộc)" aria-label="Ghi chú xét duyệt"><button class="approve-action" name="decision" value="APPROVED"><i class="fa-solid fa-check"></i> Duyệt</button><button class="reject-action" name="decision" value="REJECTED"><i class="fa-solid fa-xmark"></i> Từ chối</button></form></c:if></td></tr></c:forEach>
                    <c:if test="${empty leaveRequests}"><tr><td colspan="6" class="empty-row">Không có đơn nghỉ phép trong kỳ.</td></tr></c:if>
                    </tbody></table></div>
                </section>
            </div>
        </section>
    </main>
</div>
<script>document.querySelector('.report-filter').addEventListener('submit',function(e){var f=this.querySelector('[name=from]'),t=this.querySelector('[name=to]');if(f.value&&t.value&&t.value<f.value){e.preventDefault();t.setCustomValidity('Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.');t.reportValidity();}else{t.setCustomValidity('');}});</script>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
