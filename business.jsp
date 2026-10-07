<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Nghiệp vụ thực tập - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/business.css?v=20261004-4">
</head>
<body>
<aside>
    <h2>IMS Portal</h2>
    <div class="sidebar-user"><b><c:out value="${sessionScope.user.fullName}"/></b><small><c:out value="${role}"/></small></div>
    <nav>
        <a href="${pageContext.request.contextPath}/home">Dashboard</a>
        <a href="${pageContext.request.contextPath}/programs">Chương trình</a>
        <c:choose>
            <c:when test="${role=='HR'||role=='ADMIN'}">
                <a href="${pageContext.request.contextPath}/applications">Hồ sơ ứng tuyển</a>
                <a href="${pageContext.request.contextPath}/contracts">Hợp đồng</a>
                <a href="${pageContext.request.contextPath}/mentor-assignments">Phân công mentor</a>
                <a href="${pageContext.request.contextPath}/attendance-report">Báo cáo chuyên cần</a>
            </c:when>
            <c:when test="${role=='CANDIDATE'}"><a href="${pageContext.request.contextPath}/my-applications">Hồ sơ của tôi</a></c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/my-applications">Hồ sơ ứng tuyển</a>
                <a href="${pageContext.request.contextPath}/my-contract">Hợp đồng của tôi</a>
                <a href="${pageContext.request.contextPath}/my-calendar">Lịch của tôi</a>
                <a href="${pageContext.request.contextPath}/attendance">Chấm công</a>
            </c:otherwise>
        </c:choose>
    </nav>
    <div class="sidebar-bottom"><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></div>
</aside>

<main>
    <header><h1>Nghiệp vụ chương trình thực tập</h1><div class="header-user"><span><c:out value="${sessionScope.user.fullName}"/></span><a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></div></header>
    <div class="content">
        <c:if test="${param.success=='1'}"><div class="alert success">Thao tác đã được ghi nhận thành công.</div></c:if>
        <c:if test="${not empty param.error}"><div class="alert error"><c:out value="${param.error}"/></div></c:if>
        <c:if test="${not empty param.message}"><div class="alert success"><c:out value="${param.message}"/></div></c:if>

        <c:if test="${page=='programs'}">
            <div class="page-title"><div><p class="eyebrow">Quản lý tuyển dụng</p><h2>Chương trình thực tập</h2><p>Theo dõi thời gian nhận hồ sơ, kỳ thực tập và chỉ tiêu theo từng phòng ban.</p></div><c:if test="${role=='HR'||role=='ADMIN'}"><a class="primary-link" href="#program-form">+ Tạo chương trình</a></c:if></div>
            <c:choose>
                <c:when test="${role=='HR'||role=='ADMIN'}">
                    <form class="filter-bar" method="get"><label>Tìm chương trình<input type="search" name="q" placeholder="Mã hoặc tên chương trình"></label><label>Phòng ban<select><option>Tất cả phòng ban</option><c:forEach items="${departments}" var="d"><option>${d.dept_name}</option></c:forEach></select></label><label>Trạng thái<select><option>Tất cả trạng thái</option><option>Đang nhận hồ sơ</option><option>Sắp mở</option><option>Đã đóng</option></select></label><button type="submit">Lọc</button></form>
                    <div class="table-card"><div class="table-heading"><div><h3>Danh sách chương trình</h3><p>Chọn chương trình để theo dõi hoặc cập nhật kế hoạch.</p></div><span class="count-chip">${programs.size()} chương trình</span></div><div class="table-scroll"><table><thead><tr><th>Chương trình</th><th>Phòng ban</th><th>Nhận hồ sơ</th><th>Kỳ thực tập</th><th>Chỉ tiêu</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody><c:forEach items="${programs}" var="p"><tr><td><strong>${p.program_name}</strong><small>${p.program_code}</small></td><td>${p.dept_name}</td><td>${p.application_open_date}<small>đến ${p.application_close_date}</small></td><td>${p.start_date}<small>đến ${p.end_date}</small></td><td>${p.quota}</td><td><span class="status-badge open">Đang theo dõi</span></td><td><a class="text-link" href="${pageContext.request.contextPath}/applications?programId=${p.id}">Xem hồ sơ</a></td></tr></c:forEach><c:if test="${empty programs}"><tr><td colspan="7" class="empty-state">Chưa có chương trình. Hãy tạo chương trình đầu tiên.</td></tr></c:if></tbody></table></div></div>
                    <section class="form-card" id="program-form"><div class="section-heading"><div><span class="section-icon">+</span><div><h3>Tạo chương trình thực tập</h3><p>Ngày bắt đầu và kết thúc được thiết lập ngay trong chương trình.</p></div></div><span class="required-note">* Bắt buộc</span></div><form method="post" class="business-form"><input type="hidden" name="action" value="save-program"><div class="form-grid"><label>Mã chương trình *<input name="code" required placeholder="VD: PRG-2026-01"></label><label class="span-2">Tên chương trình *<input name="name" required placeholder="VD: Backend Java Intern"></label><label>Phòng ban *<select name="departmentId" required><option value="">Chọn phòng ban</option><c:forEach items="${departments}" var="d"><option value="${d.id}">${d.dept_name}</option></c:forEach></select></label><label>Chỉ tiêu *<input name="quota" type="number" min="1" required placeholder="10"></label><label>Mở nhận hồ sơ *<input name="openDate" type="date" required></label><label>Đóng nhận hồ sơ *<input name="closeDate" type="date" required></label><label>Bắt đầu thực tập *<input name="startDate" type="date" required></label><label>Kết thúc thực tập *<input name="endDate" type="date" required></label><fieldset class="span-4"><legend>Thứ làm việc</legend><div class="check-row"><label><input type="checkbox" name="workDays" value="MON" checked>T2</label><label><input type="checkbox" name="workDays" value="TUE" checked>T3</label><label><input type="checkbox" name="workDays" value="WED" checked>T4</label><label><input type="checkbox" name="workDays" value="THU" checked>T5</label><label><input type="checkbox" name="workDays" value="FRI" checked>T6</label><label><input type="checkbox" name="workDays" value="SAT">T7</label><label><input type="checkbox" name="workDays" value="SUN">CN</label></div></fieldset><label class="span-4">Mô tả chương trình<textarea name="description" placeholder="Mục tiêu, yêu cầu và nội dung chính của chương trình"></textarea></label></div><div class="form-actions"><button type="reset" class="ghost">Hủy thay đổi</button><button type="submit">Tạo chương trình</button></div></form></section>
                </c:when>
                <c:otherwise>
                    <div class="program-grid"><c:forEach items="${programs}" var="p"><article class="program-card"><div class="card-top"><span class="dept-chip">${p.dept_name}</span><span class="status-badge open">Đang nhận hồ sơ</span></div><h3>${p.program_name}</h3><div class="meta-list"><p><b>Nhận hồ sơ</b><span>${p.application_open_date} - ${p.application_close_date}</span></p><p><b>Kỳ thực tập</b><span>${p.start_date} - ${p.end_date}</span></p><p><b>Chỉ tiêu</b><span>${p.quota}</span></p></div><a class="primary-link full" href="${pageContext.request.contextPath}/my-applications?programId=${p.id}">Bắt đầu hồ sơ</a></article></c:forEach></div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <c:if test="${page=='my-applications'}">
            <div class="page-title"><div><p class="eyebrow">Ứng tuyển</p><h2>Nộp hồ sơ trực tuyến</h2><p>Lưu nháp bất cứ lúc nào và kiểm tra đủ thông tin trước khi nộp.</p></div></div>
            <div class="stepper"><span class="active">1 Chương trình</span><span class="active">2 Học vấn</span><span>3 Tài liệu</span><span>4 Kiểm tra</span></div>
            <form method="post" class="form-card business-form"><div class="section-heading"><div><span class="section-icon">1</span><div><h3>Thông tin hồ sơ</h3><p>Các trường có dấu * là bắt buộc khi nộp.</p></div></div><span class="status-badge draft">Nháp</span></div><div class="form-grid"><label class="span-2">Chương trình *<select name="programId" required><option value="">Chọn chương trình đang mở</option><c:forEach items="${programs}" var="p"><c:if test="${p.is_open==1}"><option value="${p.id}">${p.program_name}</option></c:if></c:forEach></select></label><label>Trường học *<input name="university" required></label><label>Chuyên ngành *<input name="major" required></label><label>Năm học<input name="studyYear" type="number" min="1" max="8"></label><label>GPA<input name="gpa" type="number" min="0" max="4" step=".01" placeholder="Theo thang 4"></label><label class="span-4">Thư giới thiệu<textarea name="coverLetter" placeholder="Giới thiệu ngắn gọn về mục tiêu và kinh nghiệm phù hợp"></textarea></label></div><div class="form-actions"><button name="action" value="save-application" class="ghost">Lưu nháp</button><button name="action" value="submit-application">Nộp hồ sơ</button></div></form>
            <div class="table-card"><div class="table-heading"><div><h3>Hồ sơ của tôi</h3><p>Theo dõi trạng thái theo từng chương trình.</p></div></div><div class="table-scroll"><table><thead><tr><th>Chương trình</th><th>Trạng thái</th><th>Ngày nộp</th></tr></thead><tbody><c:forEach items="${myApplications}" var="a"><tr><td><strong>${a.program_name}</strong></td><td><span class="status-badge">${a.status}</span></td><td>${a.submitted_at}</td></tr></c:forEach><c:if test="${empty myApplications}"><tr><td colspan="3" class="empty-state">Bạn chưa có hồ sơ nào.</td></tr></c:if></tbody></table></div></div>
        </c:if>

        <c:if test="${page=='applications'}">
            <div class="page-title"><div><p class="eyebrow">Tuyển dụng</p><h2>Hồ sơ ứng tuyển</h2><p>Lọc, xem thông tin và ra quyết định cho từng hồ sơ.</p></div><div class="selection-actions"><span>Chọn hồ sơ trong bảng để xử lý</span></div></div>
            <form class="filter-bar" method="get"><label>Trạng thái<select name="status"><option value="">Tất cả</option><option value="DRAFT">Bản nháp</option><option value="SUBMITTED">Đã nộp</option><option value="UNDER_REVIEW">Đang duyệt</option><option value="APPROVED">Đã duyệt</option><option value="REJECTED">Từ chối</option></select></label><label class="grow">Tìm ứng viên<input type="search" name="q" placeholder="Tên hoặc email"></label><button>Lọc hồ sơ</button></form>
            <div class="table-card"><div class="table-heading"><div><h3>Danh sách hồ sơ</h3><p>Nháp được hiển thị cho HR; chỉ hồ sơ đã nộp mới có thể xét duyệt.</p></div><span class="count-chip">${applications.size()} hồ sơ</span></div><div class="table-scroll"><table><thead><tr><th>Ứng viên</th><th>Chương trình</th><th>Học vấn</th><th>GPA</th><th>Trạng thái</th><th>Quyết định</th></tr></thead><tbody><c:forEach items="${applications}" var="a"><tr><td><strong>${a.full_name}</strong><small>${a.email}</small></td><td>${a.program_name}<small>${a.dept_name}</small></td><td>${a.university_name}<small>${a.major_name}</small></td><td>${a.gpa}</td><td><span class="status-badge">${a.status}</span></td><td><c:choose><c:when test="${a.status == 'DRAFT'}"><span class="muted-value">Bản nháp - chưa thể xét duyệt</span></c:when><c:otherwise><form method="post" class="decision-form"><input type="hidden" name="action" value="decide"><input type="hidden" name="applicationId" value="${a.id}"><input name="reason" placeholder="Lý do khi từ chối"><div><button name="decision" value="APPROVED" class="small">Duyệt</button><button name="decision" value="REJECTED" class="small danger">Từ chối</button></div></form></c:otherwise></c:choose></td></tr></c:forEach><c:if test="${empty applications}"><tr><td colspan="6" class="empty-state">Không có hồ sơ phù hợp bộ lọc.</td></tr></c:if></tbody></table></div></div>
        </c:if>

        <c:if test="${page=='contracts'}">
            <div class="page-title"><div><p class="eyebrow">Thủ tục tiếp nhận</p><h2>Hợp đồng thực tập</h2><p>Theo dõi hợp đồng hiện hành và trạng thái xác nhận của thực tập sinh.</p></div></div>
            <div class="table-card"><div class="table-heading"><div><h3>Danh sách hợp đồng</h3><p>Chỉ tải hợp đồng cho ứng viên đã được tiếp nhận.</p></div></div><div class="table-scroll"><table><thead><tr><th>Thực tập sinh</th><th>Số hợp đồng</th><th>Chương trình</th><th>Trạng thái xác nhận</th></tr></thead><tbody><c:forEach items="${contracts}" var="c"><tr><td><strong>${c.full_name}</strong></td><td>${c.contract_number}</td><td>${c.program_name}</td><td><span class="status-badge">${c.signature_status}</span></td></tr></c:forEach><c:if test="${empty contracts}"><tr><td colspan="4" class="empty-state">Chưa có hợp đồng nào.</td></tr></c:if></tbody></table></div></div>
            <section class="form-card"><div class="section-heading"><div><span class="section-icon">PDF</span><div><h3>Tải hợp đồng mới</h3><p>Ngày được gợi ý theo chương trình và có thể điều chỉnh trước khi gửi.</p></div></div></div><form method="post" enctype="multipart/form-data" class="business-form"><input type="hidden" name="action" value="upload-contract"><div class="form-grid"><label class="span-2">Thực tập sinh *<select name="profileId" required><option value="">Chọn thực tập sinh</option><c:forEach items="${interns}" var="i"><option value="${i.id}">${i.full_name}</option></c:forEach></select></label><label>Số hợp đồng *<input name="contractNumber" required></label><label>Hạn xác nhận<input name="deadline" type="date"></label><label>Ngày bắt đầu<input name="effectiveDate" type="date"></label><label>Ngày kết thúc<input name="expirationDate" type="date"></label><label class="span-2">Tệp hợp đồng PDF *<input name="contractFile" type="file" accept="application/pdf" required></label></div><div class="form-actions"><button type="reset" class="ghost">Hủy</button><button>Gửi hợp đồng cho TTS</button></div></form></section>
        </c:if>

        <c:if test="${page=='my-contract'}">
            <div class="page-title"><div><p class="eyebrow">Thủ tục của tôi</p><h2>Hợp đồng của tôi</h2><p>Kiểm tra đúng phiên bản hiện hành trước khi xác nhận.</p></div></div>
            <c:choose><c:when test="${empty myContract}"><div class="empty-panel"><span>⌛</span><h3>Chưa có hợp đồng</h3><p>HR chưa gửi hợp đồng cho bạn. Hệ thống sẽ hiển thị thông báo khi có hợp đồng mới.</p></div></c:when><c:otherwise><article class="contract-card"><div class="contract-head"><div><span class="status-badge pending">Chờ xác nhận</span><h3>${myContract.contract_number}</h3></div><div><small>Hạn xác nhận</small><strong>${myContract.confirmation_deadline}</strong></div></div><div class="contract-meta"><p><small>Bắt đầu</small><strong>${myContract.effective_date}</strong></p><p><small>Kết thúc</small><strong>${myContract.expiration_date}</strong></p><p><small>Trạng thái</small><strong>${myContract.signature_status}</strong></p></div><form method="post" class="contract-confirm"><input type="hidden" name="action" value="confirm-contract"><input type="hidden" name="contractId" value="${myContract.id}"><label class="agreement"><input type="checkbox" required> Tôi đã đọc và đồng ý với nội dung của phiên bản hợp đồng hiện tại.</label><label>Lý do nếu không xác nhận<input name="reason" placeholder="Bắt buộc khi từ chối"></label><div class="form-actions"><button name="choice" value="reject" class="ghost danger-text">Không xác nhận</button><button name="choice" value="accept">Xác nhận hợp đồng</button></div></form></article></c:otherwise></c:choose>
        </c:if>

        <c:if test="${page=='mentor-assignments'}">
            <div class="page-title"><div><p class="eyebrow">Tổ chức hướng dẫn</p><h2>Phân công mentor</h2><p>Chỉ phân công mentor đang hoạt động và phù hợp với phòng ban chương trình.</p></div></div>
            <div class="info-strip"><span>Mentor khả dụng: <b>${mentors.size()}</b></span><span>Thực tập sinh cần theo dõi: <b>${interns.size()}</b></span><span>Kiểm tra tải hiện tại trước khi xác nhận</span></div>
            <div class="table-card"><div class="table-heading"><div><h3>Danh sách phân công</h3><p>Mỗi thay đổi có hiệu lực ngay sau khi xác nhận.</p></div></div><div class="table-scroll"><table><thead><tr><th>Thực tập sinh</th><th>Chương trình</th><th>Mentor hiện tại</th><th>Mentor đề xuất</th><th></th></tr></thead><tbody><c:forEach items="${interns}" var="i"><tr><td><strong>${i.full_name}</strong></td><td>${i.program_name}</td><td>${empty i.mentor_code?'Chưa phân công':i.mentor_code}</td><td colspan="2"><form method="post" class="assign-form"><input type="hidden" name="action" value="assign-mentor"><input type="hidden" name="profileId" value="${i.id}"><select name="mentorId" required><option value="">Chọn mentor</option><c:forEach items="${mentors}" var="m"><option value="${m.id}">${m.full_name} - ${m.load_count} TTS</option></c:forEach></select><button class="small">Xác nhận</button></form></td></tr></c:forEach><c:if test="${empty interns}"><tr><td colspan="5" class="empty-state">Chưa có thực tập sinh cần phân công.</td></tr></c:if></tbody></table></div></div>
        </c:if>

        <c:if test="${page=='my-calendar'}">
            <div class="page-title"><div><p class="eyebrow">Kế hoạch cá nhân</p><h2>Lịch thực tập của tôi</h2><p>Lịch chỉ đọc; mốc hợp đồng được ưu tiên hơn mốc chương trình.</p></div><div class="view-switch"><button class="active">Tháng</button><button>Tuần</button></div></div>
            <div class="period-summary"><div><small>Bắt đầu kỳ thực tập</small><strong>${not empty myContract.effective_date?myContract.effective_date:profile.start_date}</strong></div><span>→</span><div><small>Kết thúc kỳ thực tập</small><strong>${not empty myContract.expiration_date?myContract.expiration_date:profile.end_date}</strong></div></div>
            <div class="empty-panel calendar-empty"><span>📅</span><h3>Lịch sự kiện đang được cập nhật</h3><p>Các mốc chương trình, hợp đồng và sự kiện cá nhân sẽ xuất hiện tại đây.</p></div>
        </c:if>

        <c:if test="${page=='attendance'}">
            <div class="page-title"><div><p class="eyebrow">Ghi nhận thời gian</p><h2>Chấm công hôm nay</h2><p>Thời gian check-in và check-out được lấy từ máy chủ.</p></div></div>
            <section class="attendance-hero"><div><span class="status-dot"></span><small>Trạng thái hôm nay</small><h3>Chưa hoàn tất chấm công</h3><p>Hãy check-in khi bắt đầu và check-out khi kết thúc ngày làm việc.</p></div><form method="post"><button name="action" value="check-in">Check-in</button><button name="action" value="check-out" class="secondary">Check-out</button></form></section>
            <div class="table-card"><div class="table-heading"><div><h3>Lịch sử chấm công</h3><p>Các bản ghi thiếu giờ ra cần được bổ sung theo quy định.</p></div><span class="count-chip">${attendance.size()} ngày</span></div><div class="table-scroll"><table><thead><tr><th>Ngày</th><th>Giờ vào</th><th>Giờ ra</th><th>Tổng thời gian</th><th>Trạng thái</th></tr></thead><tbody><c:forEach items="${attendance}" var="a"><tr><td>${a.work_date}</td><td>${a.check_in_time}</td><td>${empty a.check_out_time?'-':a.check_out_time}</td><td>${empty a.total_minutes?'-':a.total_minutes} phút</td><td><span class="status-badge ${empty a.check_out_time?'warning':'done'}">${empty a.check_out_time?'Thiếu check-out':'Hoàn tất'}</span></td></tr></c:forEach><c:if test="${empty attendance}"><tr><td colspan="5" class="empty-state">Chưa có dữ liệu chấm công.</td></tr></c:if></tbody></table></div></div>
        </c:if>

        <c:if test="${page=='attendance-report'}">
            <div class="page-title"><div><p class="eyebrow">Theo dõi chuyên cần</p><h2>Báo cáo đi làm và nghỉ phép</h2><p>Số liệu tổng hợp trên toàn bộ nhóm lọc; nghỉ phép chưa có nguồn dữ liệu trong Sprint 2.</p></div></div>
            <form class="filter-bar"><label>Chương trình<select><option>Tất cả chương trình</option><c:forEach items="${programs}" var="p"><option>${p.program_name}</option></c:forEach></select></label><label class="grow">Thực tập sinh<input type="search" placeholder="Tên hoặc mã TTS"></label><label>Khoảng thời gian<select><option>Tháng hiện tại</option><option>Tháng trước</option></select></label><button>Lọc báo cáo</button></form>
            <div class="table-card"><div class="table-heading"><div><h3>Tổng hợp chuyên cần</h3><p>Hôm nay chưa kết thúc không được tính là vắng.</p></div></div><div class="table-scroll"><table><thead><tr><th>Thực tập sinh</th><th>Chương trình</th><th>Ngày đi làm</th><th>Tổng giờ</th><th>Thiếu check-out</th><th>Nghỉ phép</th></tr></thead><tbody><c:forEach items="${report}" var="r"><tr><td><strong>${r.full_name}</strong></td><td>${r.program_name}</td><td>${r.work_days}</td><td>${r.total_minutes/60}</td><td><span class="status-badge ${r.missing_checkout>0?'warning':'done'}">${r.missing_checkout}</span></td><td><span class="muted-value">Chưa có dữ liệu</span></td></tr></c:forEach><c:if test="${empty report}"><tr><td colspan="6" class="empty-state">Không có dữ liệu trong khoảng thời gian đã chọn.</td></tr></c:if></tbody></table></div></div>
        </c:if>
    </div>
</main>
<script>document.querySelectorAll('nav a').forEach(function(a){if(a.pathname===location.pathname)a.classList.add('active')});</script>
</body>
</html>