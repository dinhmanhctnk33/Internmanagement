<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quản lý nhiệm vụ | IMS PORTAL</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
  <link rel="stylesheet" href="mentor-tasks.css?v=15">
  <script src="mentor-tasks.js?v=15" defer></script>
</head>
<body>
<div class="app-shell">
  <aside class="sidebar" aria-label="Điều hướng">
    <a class="global-brand" href="#" aria-label="IMS Portal - Mentor">
      <span class="global-brand-icon" aria-hidden="true"><i class="fa-solid fa-user-graduate"></i></span>
      <span><strong>IMS Portal</strong><small>Dành cho Mentor</small></span>
    </a>
    <div class="sidebar-label">MENTOR WORKSPACE</div>
    <nav class="nav">
      <a href="#" data-demo-nav="Tổng quan"><i class="fa-solid fa-chart-pie" aria-hidden="true"></i><span>Tổng quan</span></a>
      <a href="#" class="active" aria-current="page"><i class="fa-solid fa-list-check" aria-hidden="true"></i><span>Công việc</span></a>
      <a href="#" data-demo-nav="Báo cáo"><i class="fa-solid fa-file-lines" aria-hidden="true"></i><span>Báo cáo</span></a>
      <a href="#" data-demo-nav="Lịch"><i class="fa-solid fa-calendar-days" aria-hidden="true"></i><span>Lịch</span></a>
      <a href="#" data-demo-nav="Thông báo"><i class="fa-regular fa-bell" aria-hidden="true"></i><span>Thông báo</span></a>
      <a href="#" data-demo-nav="Tài khoản"><i class="fa-solid fa-circle-user" aria-hidden="true"></i><span>Tài khoản</span></a>
    </nav>
  </aside>
  <main class="main-content">
    <header class="global-header">
      <div class="global-header-tools">
        <label class="global-search">
          <i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i>
          <input id="globalSearchInput" type="search" placeholder="Tìm kiếm nhiệm vụ..." aria-label="Tìm kiếm nhiệm vụ">
        </label>
        <span class="header-notification" aria-hidden="true"><i class="fa-regular fa-bell"></i></span>
        <span class="header-avatar" aria-hidden="true">M</span>
        <span class="header-user"><strong>Mentor Demo</strong><small>Mentor</small></span>
        <i class="header-chevron fa-solid fa-chevron-down" aria-hidden="true"></i>
      </div>
    </header>
    <div class="page-content">
    <header class="topbar"><div><div class="eyebrow">MENTOR / CÔNG VIỆC</div><h1>Quản lý nhiệm vụ</h1><p>Giao việc rõ ràng, theo dõi tiến độ của từng thực tập sinh.</p></div></header>
    <section class="stats" aria-label="Thống kê nhiệm vụ">
      <article class="stat"><span>Tổng nhiệm vụ</span><strong id="statTotal">0</strong><small>Trong danh sách mẫu</small></article>
      <article class="stat"><span>Đang hiệu lực</span><strong id="statActive">0</strong><small>Đang triển khai</small></article>
      <article class="stat"><span>Quá hạn</span><strong id="statOverdue">0</strong><small>Cần theo dõi</small></article>
      <article class="stat"><span>Tiến độ trung bình</span><strong id="statProgress">0%</strong><small>Trên các nhiệm vụ</small></article>
    </section>
    <section class="panel" aria-labelledby="taskListTitle">
      <div class="panel-heading"><div><h2 id="taskListTitle">Danh sách nhiệm vụ</h2></div><button class="btn btn-primary" id="openCreate" type="button">＋ Tạo nhiệm vụ</button></div>
      <div class="filters"><label class="field search"><span>Tìm nhiệm vụ</span><input id="searchInput" type="search" placeholder="Nhập tên hoặc mô tả nhiệm vụ..."></label><label class="field"><span>Người nhận</span><select id="assigneeFilter"><option value="all">Tất cả thực tập sinh</option></select></label><label class="field"><span>Trạng thái</span><select id="statusFilter"><option value="all">Tất cả trạng thái</option><option value="active">Đang hiệu lực</option><option value="edited">Đã chỉnh sửa</option><option value="overdue">Quá hạn</option><option value="cancelled">Đã hủy</option><option value="reopened">Đã mở lại</option></select></label></div>
      <div id="loadingState" class="state-message" hidden>Đang tải nhiệm vụ...</div>
      <div id="errorState" class="state-message error-state" hidden>Không thể tải danh sách. <button type="button" class="btn btn-light" id="retryButton">Thử lại</button></div>
      <div class="table-wrap" id="tableWrap"><table><thead><tr><th>Nhiệm vụ</th><th>Hạn</th><th>Hoàn thành</th><th>Tiến độ TB</th><th>Người nhận</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="taskRows"></tbody></table></div>
      <div id="emptyState" class="state-message" hidden><strong>Chưa có nhiệm vụ phù hợp</strong><p>Hãy thay đổi bộ lọc hoặc tạo nhiệm vụ mới.</p></div>
      <div class="table-footer"><span id="resultCount">0 nhiệm vụ</span><span class="demo-tag">DỮ LIỆU MẪU · CHƯA KẾT NỐI BACKEND</span></div>
    </section>
    <section class="panel form-panel" id="taskFormPanel" aria-labelledby="formTitle">
      <div class="panel-heading"><div><h2 id="formTitle">Tạo nhiệm vụ mới</h2><p>Điền thông tin và chọn các thực tập sinh trong nhóm</p></div></div>
      <form id="taskForm" novalidate>
        <input type="hidden" id="editingId">
        <div class="form-grid"><label class="field full"><span>Tiêu đề nhiệm vụ <em>*</em></span><input id="taskTitle" maxlength="120" required placeholder="Ví dụ: Hoàn thiện API báo cáo"></label>
        <label class="field full"><span>Mô tả / đường dẫn tài liệu <em>*</em></span><textarea id="taskDescription" rows="4" maxlength="1000" required placeholder="Mô tả yêu cầu, kết quả mong đợi, đường dẫn tài liệu..."></textarea></label>
        <label class="field"><span>Hạn hoàn thành <em>*</em></span><input id="taskDeadline" type="date" required></label>
        <div class="field"><span>Người nhận <em>*</em> <small>(chọn nhiều)</small></span><div id="assigneeChecks" class="checkbox-grid"></div></div></div>
        <p id="formError" class="form-error" role="alert" hidden></p>
        <div class="form-actions"><button type="button" class="btn btn-light" id="cancelForm">Hủy</button><button type="submit" class="btn btn-primary" id="submitTask">Giao nhiệm vụ</button></div>
      </form>
    </section>
    <footer class="page-footer">Bản giao diện demo. Xác thực quyền Mentor và lưu dữ liệu thật cần được thực hiện ở Backend.</footer>
    </div>
  </main>
</div>
<div id="toast" class="toast" role="status" aria-live="polite"></div>
</body>
</html>
