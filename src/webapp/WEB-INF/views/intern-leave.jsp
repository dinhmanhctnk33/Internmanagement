<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  SCRUM148 / US-S3-08 - Thực tập sinh đăng ký nghỉ phép (FRONTEND ONLY).

  Backend CHƯA cung cấp route cho trang này. Để hiển thị thật, Backend cần:
    1. Route GET (ví dụ /my-leave, role INTERN) forward tới /WEB-INF/views/intern-leave.jsp.
    2. Request attribute "leaveRequests": List<Map> với khóa
       id, leave_type, start_date, end_date, approval_status, reason, approver_notes
       (cùng cấu trúc InternAttendanceServlet.leaves(); nên bỏ LIMIT 20 nếu muốn xem toàn bộ đơn).
    3. Tùy chọn: request attribute "errorMessage" khi không tải được danh sách.
    4. Session attribute "currentUser" (fullName, shortName) như các trang thực tập sinh khác.

  Form gửi POST tới /leave-requests (LeaveRequestServlet hiện có): leaveType, startDate, endDate, reason.
  Quy tắc theo Backend: ngày bắt đầu không ở quá khứ, ngày kết thúc >= ngày bắt đầu, lý do >= 5 ký tự.

  Chưa được Backend hỗ trợ (không gọi API giả):
    - Trạng thái "Đã hủy" (CANCELLED) và thao tác hủy đơn: DB chỉ có PENDING/APPROVED/REJECTED.
    - Tìm kiếm / lọc / phân trang: xử lý phía trình duyệt trên dữ liệu đã nạp.
    - Số ngày làm việc: tính phía trình duyệt (T2-T6, chưa trừ ngày lễ); Backend lưu total_days theo ngày lịch.
    - Sau khi gửi, Backend redirect về /attendance (kèm ?leave=created hoặc ?error=...), không về trang này.
      Dữ liệu form được giữ tạm trong sessionStorage để khôi phục nếu quay lại trang này với ?error=.
    - Hạn báo trước: Backend chỉ chặn ngày đã qua nên "ngày nghỉ sớm nhất" là hôm nay.
  "Quá ngày bắt đầu" được suy ra ở giao diện: đơn PENDING có ngày bắt đầu đã qua.
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Đăng ký nghỉ phép - IMS</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-operations.css?v=20261010-1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261008-1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-leave.css?v=20261013-3">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
<aside class="sidebar">
    <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-graduate"></i></div><div><h2>IMS Portal</h2><span>Dành cho Thực tập sinh</span></div></div>
    <nav class="menu">
        <p class="menu-title">TRANG CÁ NHÂN</p>
        <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-house"></i><span>Trang cá nhân</span></a>
        <a href="${pageContext.request.contextPath}/interns/profile" class="menu-item"><i class="fa-solid fa-id-card"></i><span>Hồ sơ của tôi</span></a>
        <a href="${pageContext.request.contextPath}/my-contract" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng của tôi</span></a>
        <a href="${pageContext.request.contextPath}/my-calendar" class="menu-item"><i class="fa-solid fa-calendar-days"></i><span>Lịch thực tập</span></a>
        <a href="${pageContext.request.contextPath}/attendance" class="menu-item"><i class="fa-solid fa-clock"></i><span>Check-in / Check-out</span></a>
        <a href="#" class="menu-item active" aria-current="page"><i class="fa-solid fa-plane-departure"></i><span>Đăng ký nghỉ phép</span></a>
    </nav>
</aside>
<main class="main">
<header class="header intern-header">
    <div class="header-context"><i class="fa-solid fa-plane-departure"></i><span>Đăng ký nghỉ phép</span></div>
    <div class="header-right">
        <div class="notification-wrap"><button class="notification" id="notificationButton" type="button" aria-label="Thông báo" aria-expanded="false"><i class="fa-regular fa-bell"></i></button><div class="notification-menu" id="notificationMenu" hidden><h3><i class="fa-solid fa-bell"></i> Thông báo</h3><div class="notification-empty">Xem các thông báo mới nhất của bạn.</div><a class="notification-item notification-all" href="${pageContext.request.contextPath}/notifications">Xem tất cả thông báo <i class="fa-solid fa-arrow-right"></i></a></div></div>
        <div class="account-menu"><button class="account-trigger user" id="accountMenuButton" type="button" aria-expanded="false"><div class="avatar">${currentUser.shortName}</div><div class="user-info"><strong><c:out value="${currentUser.fullName}"/></strong><small>Thực tập sinh (INTERN)</small></div><i class="fa-solid fa-chevron-down account-chevron"></i></button><div class="account-dropdown" id="accountMenu" hidden><a href="${pageContext.request.contextPath}/change-password"><i class="fa-solid fa-key"></i> Đổi mật khẩu</a><a class="logout-link" href="${pageContext.request.contextPath}/logout"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a></div></div>
    </div>
</header>

<section class="content operation-page lv-page" id="lvPage" data-load-error="${not empty errorMessage ? 'true' : 'false'}">
    <div class="operation-heading"><div><h1>Đăng ký nghỉ phép</h1></div></div>

    <c:if test="${param.leave=='created'}"><div class="feedback success" role="status"><i class="fa-solid fa-circle-check"></i> Đơn nghỉ phép đã được gửi cho HR.</div></c:if>
    <c:if test="${not empty param.error}"><div class="feedback error" role="alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>
    <div class="feedback success" id="lvFeedback" role="status" hidden><i class="fa-solid fa-circle-check"></i> <span id="lvFeedbackText"></span></div>

    <!-- Form đăng ký nghỉ phép ngay trong nội dung trang, phía trên bảng danh sách -->
    <section class="history-card lv-form-card" id="lvFormCard" hidden>
        <div class="card-heading">
            <div><h2>Đăng ký nghỉ phép mới</h2></div>
            <button type="button" class="lv-close" id="lvFormClose" aria-label="Đóng form"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <p class="lv-error lv-form-error" id="lvFormError" role="alert" hidden></p>
        <form id="lvForm" class="lv-form" method="post" action="${pageContext.request.contextPath}/leave-requests" novalidate>
            <div class="lv-form-grid">
                <div class="lv-field">
                    <label for="lvType">Loại nghỉ <b>*</b></label>
                    <select id="lvType" name="leaveType" required>
                        <option value="PERSONAL_LEAVE">Nghỉ cá nhân</option>
                        <option value="SICK_LEAVE">Nghỉ ốm</option>
                        <option value="UNPAID_LEAVE">Nghỉ không lương</option>
                    </select>
                    <p class="lv-error" id="lvTypeError" role="alert" hidden></p>
                </div>
                <div class="lv-field">
                    <label for="lvStart">Từ ngày <b>*</b></label>
                    <input type="date" id="lvStart" name="startDate" required aria-describedby="lvStartError">
                    <p class="lv-error" id="lvStartError" role="alert" hidden></p>
                </div>
                <div class="lv-field">
                    <label for="lvEnd">Đến ngày <b>*</b></label>
                    <input type="date" id="lvEnd" name="endDate" required aria-describedby="lvEndError">
                    <p class="lv-error" id="lvEndError" role="alert" hidden></p>
                </div>
            </div>
            <div class="lv-field">
                <label for="lvReason">Lý do <b>*</b></label>
                <textarea id="lvReason" name="reason" rows="2" required placeholder="Nhập lý do xin nghỉ phép (tối thiểu 5 ký tự)" aria-describedby="lvReasonError"></textarea>
                <p class="lv-error" id="lvReasonError" role="alert" hidden></p>
            </div>
            <div class="lv-form-footer">
                <div class="lv-meta">
                    <span>Số ngày làm việc: <strong id="lvWorkdays">—</strong></span>
                    <span class="lv-hint">· Ngày nghỉ sớm nhất: <span id="lvEarliest"></span></span>
                    <p class="lv-warning" id="lvWorkdaysWarning" hidden>Khoảng ngày này không có ngày làm việc (T2–T6).</p>
                </div>
                <div class="lv-actions">
                    <button type="button" class="lv-btn lv-btn-ghost" id="lvCancel">Hủy</button>
                    <button type="submit" class="lv-btn lv-btn-primary" id="lvSubmit"><i class="fa-regular fa-paper-plane"></i> Gửi yêu cầu</button>
                </div>
            </div>
        </form>
    </section>

    <section class="history-card lv-card">
        <div class="lv-toolbar">
            <div class="lv-search"><i class="fa-solid fa-magnifying-glass"></i><input type="search" id="lvSearch" placeholder="Tìm kiếm" aria-label="Tìm kiếm đơn nghỉ"></div>
            <div class="lv-range" role="group" aria-label="Khoảng ngày">
                <input type="date" id="lvFilterFrom" aria-label="Lọc từ ngày">
                <span>–</span>
                <input type="date" id="lvFilterTo" aria-label="Lọc đến ngày">
            </div>
            <select id="lvFilterStatus" aria-label="Lọc theo trạng thái">
                <option value="">Tất cả trạng thái</option>
                <option value="PENDING">Chờ duyệt</option>
                <option value="APPROVED">Đã duyệt</option>
                <option value="REJECTED">Bị từ chối</option>
                <option value="CANCELLED">Đã hủy</option>
                <option value="EXPIRED">Quá ngày bắt đầu</option>
            </select>
            <button type="button" class="lv-btn lv-btn-link" id="lvFilterReset" hidden>Xóa lọc</button>
            <button type="button" class="lv-btn lv-btn-primary lv-new" id="lvNewRequest"><i class="fa-solid fa-plus"></i> Đăng ký nghỉ</button>
        </div>
        <p class="lv-error lv-filter-error" id="lvFilterError" role="alert" hidden></p>
        <p class="lv-stats" id="lvStats" aria-live="polite">
            <span>Chờ duyệt: <b data-count="PENDING">0</b></span>
            <span>Đã duyệt: <b data-count="APPROVED">0</b></span>
            <span>Bị từ chối: <b data-count="REJECTED">0</b></span>
            <span>Đã hủy: <b data-count="CANCELLED">0</b></span>
            <span>Quá ngày bắt đầu: <b data-count="EXPIRED">0</b></span>
        </p>

        <div class="table-wrap">
            <table class="lv-table" id="lvTable" aria-busy="true">
                <thead><tr><th>TỪ NGÀY</th><th>ĐẾN NGÀY</th><th>SỐ NGÀY LÀM VIỆC</th><th>TRẠNG THÁI</th></tr></thead>
                <tbody id="lvBody">
                <c:forEach items="${leaveRequests}" var="l">
                    <tr class="lv-row" data-status="${l.approval_status}" data-type="${l.leave_type}" data-start="${l.start_date}" data-end="${l.end_date}" data-reason="<c:out value="${l.reason}"/>">
                        <td data-label="Từ ngày"><strong class="js-start">${l.start_date}</strong></td>
                        <td data-label="Đến ngày"><span class="js-end">${l.end_date}</span></td>
                        <td data-label="Số ngày làm việc"><span class="js-workdays">—</span></td>
                        <td data-label="Trạng thái"><span class="lv-status js-status"></span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <div class="lv-state" id="lvLoading"><i class="fa-solid fa-spinner fa-spin"></i><span>Đang tải...</span></div>
            <div class="lv-state" id="lvError" hidden><i class="fa-solid fa-triangle-exclamation"></i><span>Không thể tải danh sách đơn nghỉ.</span><button type="button" class="lv-btn lv-btn-ghost" id="lvRetry">Thử lại</button></div>
            <div class="lv-state" id="lvEmpty" hidden><i class="fa-regular fa-folder-open"></i><span id="lvEmptyText">Chưa có đơn nghỉ phép.</span><button type="button" class="lv-btn lv-btn-ghost" id="lvEmptyReset" hidden>Xóa bộ lọc</button></div>
        </div>
        <div class="lv-pagination" id="lvPagination" hidden><span id="lvPageInfo"></span><div class="lv-pages" id="lvPages"></div></div>
    </section>
</section>
</main>
</div>

<!-- Hộp thoại xác nhận nhỏ khi gửi yêu cầu -->
<div class="lv-modal" id="lvConfirmModal" hidden>
    <div class="lv-modal-backdrop" data-dismiss></div>
    <div class="lv-confirm-box" role="dialog" aria-modal="true" aria-labelledby="lvConfirmTitle">
        <div class="lv-confirm-head">
            <h3 id="lvConfirmTitle"><i class="fa-solid fa-circle-question"></i> Xác nhận gửi đơn nghỉ phép</h3>
            <button type="button" class="lv-close" data-dismiss aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <p class="lv-confirm-lead">Bạn có chắc chắn muốn gửi đơn nghỉ phép với thông tin sau tới HR?</p>
        <dl class="lv-summary-list">
            <div><dt>Loại nghỉ</dt><dd id="lvSumType"></dd></div>
            <div><dt>Thời gian</dt><dd id="lvSumRange"></dd></div>
            <div><dt>Số ngày làm việc</dt><dd id="lvSumDays"></dd></div>
            <div><dt>Lý do</dt><dd id="lvSumReason"></dd></div>
        </dl>
        <div class="lv-confirm-actions">
            <button type="button" class="lv-btn lv-btn-ghost" id="lvBack">Quay lại</button>
            <button type="button" class="lv-btn lv-btn-primary" id="lvConfirm"><i class="fa-solid fa-check"></i> Xác nhận gửi</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261008-1"></script>
<script src="${pageContext.request.contextPath}/frontend/js/intern-leave.js?v=20261013-3"></script>
</body>
</html>
