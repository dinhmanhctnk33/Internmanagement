<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển Nhân sự (HR) - Quản lý Thực tập sinh</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .account-menu { position:relative; }.account-trigger { display:flex; align-items:center; gap:12px; border:0; background:transparent; color:inherit; cursor:pointer; padding:5px 8px; border-radius:9px; text-align:left; }.account-trigger:hover,.account-trigger[aria-expanded="true"] { background:rgba(255,255,255,.09); }.account-dropdown { position:absolute; z-index:100; top:calc(100% + 10px); right:0; min-width:210px; padding:7px; border:1px solid #dce5f0; border-radius:10px; background:#fff; box-shadow:0 14px 30px rgba(17,45,78,.18); }.account-dropdown[hidden] { display:none; }.account-dropdown a { display:flex; align-items:center; gap:10px; padding:10px 11px; border-radius:7px; color:#234a7d; text-decoration:none; font-size:14px; font-weight:600; }.account-dropdown a:hover { background:#edf3fb; }.account-dropdown .logout-link { color:#bd3f3f; }
        .notification-wrap { position:relative; }.notification-menu { position:absolute; z-index:120; top:calc(100% + 10px); right:0; width:350px; max-height:420px; overflow:auto; background:#fff; border:1px solid #dce5f0; border-radius:11px; box-shadow:0 16px 34px rgba(17,45,78,.2); }.notification-menu[hidden] { display:none; }.notification-menu h3{margin:0;padding:15px 16px;color:#163a69;border-bottom:1px solid #e6edf5;font-size:15px}.notification-item{display:block;padding:12px 16px;border-bottom:1px solid #edf2f7;color:inherit;text-decoration:none}.notification-item:hover{background:#f2f7fd}.notification-item.unread{background:#ebf4ff}.notification-item strong{display:block;color:#173d70;font-size:13px}.notification-item span{display:block;color:#6b7e95;font-size:12px;margin-top:4px;line-height:1.35}.notification-empty{padding:26px 16px;color:#718096;text-align:center;font-size:13px}
    </style>
    <style>.statistics .stat-card small { display:none; }</style>
</head>

<body>

<div class="layout">

    <!-- ================= SIDEBAR HR ================= -->
    <aside class="sidebar">

        <div class="logo">
            <div class="logo-icon">
                <i class="fa-solid fa-user-tie"></i>
            </div>
            <div>
                <h2>IMS Portal</h2>
                <span>Phòng Nhân sự (HR)</span>
            </div>
        </div>

        <nav class="menu">
            <p class="menu-title">NGHIỆP VỤ HR</p>

            <a href="${pageContext.request.contextPath}/home" class="menu-item active">
                <i class="fa-solid fa-chart-line"></i>
                <span>Dashboard HR</span>
            </a>

            <a href="${pageContext.request.contextPath}/interns" class="menu-item">
                <i class="fa-solid fa-users"></i>
                <span>Thực tập sinh & Lọc</span>
            </a>

            <a href="${pageContext.request.contextPath}/interns/new" class="menu-item">
                <i class="fa-solid fa-user-plus"></i>
                <span>Thêm hồ sơ TTS</span>
            </a>

            <a href="${pageContext.request.contextPath}/documents/review" class="menu-item">
                <i class="fa-solid fa-file-circle-check"></i>
                <span>Duyệt tài liệu & CV</span>
            </a>

            <div class="menu-group">
                <button class="menu-group-toggle" type="button" aria-expanded="false"><i class="fa-solid fa-briefcase"></i><span>Quy trình thực tập</span><i class="fa-solid fa-chevron-down group-chevron"></i></button>
                <div class="menu-group-content" hidden>
                    <a href="${pageContext.request.contextPath}/programs" class="menu-item"><i class="fa-solid fa-calendar-days"></i><span>Chương trình thực tập</span></a>
                    <a href="${pageContext.request.contextPath}/applications" class="menu-item"><i class="fa-solid fa-file-signature"></i><span>Xét duyệt hồ sơ</span></a>
                    <a href="${pageContext.request.contextPath}/contracts" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng thực tập</span></a>
                    <a href="${pageContext.request.contextPath}/mentor-assignments" class="menu-item"><i class="fa-solid fa-user-group"></i><span>Phân công mentor</span></a>
                    <a href="${pageContext.request.contextPath}/attendance-report" class="menu-item"><i class="fa-solid fa-chart-column"></i><span>Báo cáo chuyên cần</span></a>
                </div>
            </div>

            <div class="menu-group">
                <button class="menu-group-toggle" type="button" aria-expanded="false"><i class="fa-solid fa-sliders"></i><span>Danh mục &amp; báo cáo</span><i class="fa-solid fa-chevron-down group-chevron"></i></button>
                <div class="menu-group-content" hidden>
                    <a href="${pageContext.request.contextPath}/mentors" class="menu-item"><i class="fa-solid fa-user-group"></i><span>Mentor hướng dẫn</span></a>
                    <a href="${pageContext.request.contextPath}/departments" class="menu-item"><i class="fa-solid fa-building"></i><span>Phòng ban</span></a>
                    <a href="${pageContext.request.contextPath}/weekly-reports" class="menu-item"><i class="fa-solid fa-file-lines"></i><span>Báo cáo tuần</span></a>
                </div>
            </div>
        </nav>

        <div class="sidebar-bottom">
            <a href="${pageContext.request.contextPath}/change-password" class="menu-item">
                <i class="fa-solid fa-key"></i><span>Đổi mật khẩu</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">
                <i class="fa-solid fa-right-from-bracket"></i>
                <span>Đăng xuất</span>
            </a>
        </div>

    </aside>


    <!-- ================= MAIN ================= -->
    <main class="main">

        <!-- HEADER -->
        <header class="header">
            <div class="search-box">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input type="text" name="q" placeholder="Tìm kiếm thực tập sinh, trường, ngành...">
            </div>

            <div class="header-right">
                <div class="notification-wrap">
                    <button class="notification" id="notificationButton" type="button" aria-label="Thông báo" aria-expanded="false"><i class="fa-regular fa-bell"></i><c:if test="${unreadNotificationCount > 0}"><span id="notificationBadge">${unreadNotificationCount}</span></c:if></button>
                    <div class="notification-menu" id="notificationMenu" hidden><h3><i class="fa-solid fa-bell"></i> Thông báo</h3><c:choose><c:when test="${empty recentNotifications}"><div class="notification-empty">Chưa có thông báo mới.</div></c:when><c:otherwise><c:forEach var="item" items="${recentNotifications}"><a class="notification-item ${item.read ? '' : 'unread'}" href="${pageContext.request.contextPath}${not empty item.targetUrl ? item.targetUrl : '/home'}"><strong>${item.title}</strong><span>${item.message}</span></a></c:forEach></c:otherwise></c:choose><a class="notification-item" style="text-align:center;color:var(--action-accent);font-weight:700" href="${pageContext.request.contextPath}/notifications">Xem tất cả thông báo <i class="fa-solid fa-arrow-right"></i></a></div>
                </div>

                <div class="account-menu">
                    <div class="account-trigger user" id="accountMenuButton" role="button" tabindex="0" aria-expanded="false" aria-controls="accountMenu">
                    <div class="avatar">
                        ${currentUser.shortName != null ? currentUser.shortName : 'HR'}
                    </div>

                    <div class="user-info">
                        <strong>${currentUser.fullName != null ? currentUser.fullName : 'Phòng Nhân sự'}</strong>
                        <small>Nhân sự (HR Manager)</small>
                    </div>

                    <i class="fa-solid fa-chevron-down"></i>
                    </div>
                    <div class="account-dropdown" id="accountMenu" hidden>
                        <a href="${pageContext.request.contextPath}/change-password"><i class="fa-solid fa-key"></i> Đổi mật khẩu</a>
                        <a class="logout-link" href="${pageContext.request.contextPath}/logout"><i class="fa-solid fa-right-from-bracket"></i> Đăng xuất</a>
                    </div>
                </div>
            </div>
        </header>


        <!-- CONTENT -->
        <section class="content">

            <div class="page-title">
                <div>
                    <h1>Quản lý Thực tập sinh (HR Portal)</h1>
                    <p>Hệ thống hỗ trợ tiếp nhận hồ sơ, tìm kiếm & lọc trường/ngành, và duyệt CV thực tập sinh.</p>
                </div>

                <!-- 1. THÊM MỚI HỒ SƠ THỰC TẬP SINH -->
                <button class="primary-btn" onclick="location.href='${pageContext.request.contextPath}/interns/new'">
                    <i class="fa-solid fa-plus"></i>
                    Thêm mới hồ sơ Thực tập sinh
                </button>
            </div>


            <!-- THỐNG KÊ TỔNG QUAN -->
            <div class="statistics">
                <div class="stat-card">
                    <div class="stat-icon blue">
                        <i class="fa-solid fa-users"></i>
                    </div>
                    <div>
                        <span>Tổng thực tập sinh</span>
                        <h2>${dashboardStats.totalInterns}</h2>
                        <small>Dữ liệu hiện tại</small>
                        <small class="increase"><i class="fa-solid fa-arrow-up"></i> 12% tháng này</small>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon green">
                        <i class="fa-solid fa-user-check"></i>
                    </div>
                    <div>
                        <span>Đang thực tập</span>
                        <h2>${dashboardStats.activeInterns}</h2>
                        <small>Dữ liệu hiện tại</small>
                        <small class="increase"><i class="fa-solid fa-arrow-up"></i> 8% tháng này</small>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon orange">
                        <i class="fa-solid fa-building-columns"></i>
                    </div>
                    <div>
                        <span>Trường ĐH liên kết</span>
                        <h2>${dashboardStats.universityCount}</h2>
                        <small>Dữ liệu từ hồ sơ thực tập sinh</small>
                        <small>Đại học & Cao đẳng</small>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon purple">
                        <i class="fa-solid fa-file-circle-check"></i>
                    </div>
                    <div>
                        <span>CV & Đơn chờ duyệt</span>
                        <h2>${dashboardStats.pendingDocuments}</h2>
                        <small>Dữ liệu hiện tại</small>
                        <small class="warning">Cần HR xử lý</small>
                    </div>
                </div>
            </div>


            <!-- 3. TÌM KIẾM VÀ LỌC THỰC TẬP SINH THEO TRƯỜNG / NGÀNH -->
            <div class="card" style="margin-bottom: 25px;">
                <div class="card-header">
                    <div>
                        <h3><i class="fa-solid fa-filter" style="color: var(--action-accent); margin-right: 8px;"></i> Bộ lọc tìm kiếm Thực tập sinh theo Trường / Ngành</h3>
                        <p>Lọc danh sách thực tập sinh nhanh chóng theo Trường Đại học và Ngành đào tạo</p>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/interns" method="get" style="display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;">
                    <div style="flex: 1; min-width: 200px;">
                        <label style="display: block; font-size: 12px; font-weight: 700; color: var(--primary-dark); margin-bottom: 5px;">
                            Từ khóa (Họ tên / Mã SV / Email)
                        </label>
                        <input type="text" name="q" value="${param.q}" placeholder="Nhập tên hoặc mã TTS..." style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 13px;">
                    </div>

                    <div style="flex: 1; min-width: 200px;">
                        <label style="display: block; font-size: 12px; font-weight: 700; color: var(--primary-dark); margin-bottom: 5px;">
                            Lọc theo Trường Đại học
                        </label>
                        <select name="university" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 13px; background: #FFFFFF;">
                            <c:if test="${false}">
                            <option value="">-- Tất cả các trường --</option>
                            <option value="ĐH Bách Khoa" ${param.university == 'ĐH Bách Khoa' ? 'selected' : ''}>ĐH Bách Khoa</option>
                            <option value="ĐH Công Nghệ" ${param.university == 'ĐH Công Nghệ' ? 'selected' : ''}>ĐH Công Nghệ (VNU)</option>
                            <option value="ĐH Kinh Tế Quốc Dân" ${param.university == 'ĐH Kinh Tế Quốc Dân' ? 'selected' : ''}>ĐH Kinh Tế Quốc Dân</option>
                            <option value="ĐH FPT" ${param.university == 'ĐH FPT' ? 'selected' : ''}>ĐH FPT</option>
                            </c:if>
                            <option value="">-- Tất cả các trường --</option>
                            <c:forEach var="school" items="${universities}">
                                <option value="${school}" ${param.university == school ? 'selected' : ''}>${school}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div style="flex: 1; min-width: 200px;">
                        <label style="display: block; font-size: 12px; font-weight: 700; color: var(--primary-dark); margin-bottom: 5px;">
                            Lọc theo Ngành học
                        </label>
                        <select name="major" style="width: 100%; padding: 9px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 13px; background: #FFFFFF;">
                            <c:if test="${false}">
                            <option value="">-- Tất cả ngành học --</option>
                            <option value="Công nghệ thông tin" ${param.major == 'Công nghệ thông tin' ? 'selected' : ''}>Công nghệ thông tin</option>
                            <option value="Khoa học dữ liệu" ${param.major == 'Khoa học dữ liệu' ? 'selected' : ''}>Khoa học dữ liệu</option>
                            <option value="Kinh tế / HR" ${param.major == 'Kinh tế / HR' ? 'selected' : ''}>Kinh tế / Quản trị nhân sự</option>
                            </c:if>
                            <option value="">-- Tất cả ngành học --</option>
                            <c:forEach var="field" items="${majors}">
                                <option value="${field}" ${param.major == field ? 'selected' : ''}>${field}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <button type="submit" class="primary-btn" style="padding: 9px 20px; height: 38px;">
                        <i class="fa-solid fa-magnifying-glass"></i> LỌC HỒ SƠ
                    </button>
                </form>
            </div>


            <!-- GRID: BANNER CHỨC NĂNG VÀ DUYỆT TÀI LIỆU -->
            <div class="dashboard-grid">

                <!-- 2. & 3. DANH SÁCH THỰC TẬP SINH & NÚT CHỈNH SỬA HỒ SƠ -->
                <div class="card large-card">
                    <div class="card-header">
                        <div>
                            <h3>Danh sách Hồ sơ Thực tập sinh mới nhất</h3>
                            <p>Quản lý thông tin và thực hiện chỉnh sửa hồ sơ</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/interns">Xem tất cả (${interns != null ? interns.size() : 4})</a>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Thực tập sinh</th>
                                    <th>Mã TTS</th>
                                    <th>Trường / Ngành</th>
                                    <th>Mentor hướng dẫn</th>
                                    <th>Trạng thái</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty interns}">
                                        <c:forEach var="item" items="${interns}">
                                            <tr>
                                                <td>
                                                    <div class="person">
                                                        <div class="person-avatar">${item.userFullName != null ? item.userFullName.substring(0, 1) : 'T'}</div>
                                                        <div>
                                                            <strong>${item.userFullName}</strong>
                                                            <small>${item.userEmail}</small>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td><strong>${item.internCode}</strong></td>
                                                <td>${item.universityName} <br><small style="color: var(--text-muted);">${item.majorName}</small></td>
                                                <td>${item.mentorName != null ? item.mentorName : 'Chưa phân công'}</td>
                                                <td><span class="badge active"><i class="fa-solid fa-check"></i> ${item.internshipStatus}</span></td>
                                                <td>
                                                    <!-- 2. CHỈNH SỬA HỒ SƠ THỰC TẬP SINH -->
                                                    <a href="${pageContext.request.contextPath}/interns/edit?id=${item.id}" style="color: var(--action-accent); font-weight: 600; text-decoration: none;">
                                                        <i class="fa-solid fa-pen-to-square"></i> Chỉnh sửa
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td>
                                                <div class="person">
                                                    <div class="person-avatar">NV</div>
                                                    <div>
                                                        <strong>Nguyễn Văn An</strong>
                                                        <small>nguyenvanan@gmail.com</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>TTS001</td>
                                            <td>ĐH Bách Khoa <br><small style="color: var(--text-muted);">CNTT</small></td>
                                            <td>Nguyễn Văn Nam</td>
                                            <td><span class="badge active">Đang thực tập</span></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/interns/edit?id=1" style="color: var(--action-accent); font-weight: 600; text-decoration: none;">
                                                    <i class="fa-solid fa-pen-to-square"></i> Chỉnh sửa
                                                </a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <div class="person">
                                                    <div class="person-avatar">TL</div>
                                                    <div>
                                                        <strong>Trần Thị Lan</strong>
                                                        <small>tranthilan@gmail.com</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>TTS002</td>
                                            <td>ĐH Công Nghệ <br><small style="color: var(--text-muted);">KH Dữ liệu</small></td>
                                            <td>Trần Văn Bình</td>
                                            <td><span class="badge active">Đang thực tập</span></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/interns/edit?id=2" style="color: var(--action-accent); font-weight: 600; text-decoration: none;">
                                                    <i class="fa-solid fa-pen-to-square"></i> Chỉnh sửa
                                                </a>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>


                <!-- 4. XEM VÀ DUYỆT TÀI LIỆU CỦA THỰC TẬP SINH -->
                <div class="card" style="grid-column: span 1;">
                    <div class="card-header">
                        <div>
                            <h3>Duyệt tài liệu TTS (CV & Đơn)</h3>
                            <p>Phê duyệt hoặc Từ chối CV & Đơn xin thực tập</p>
                        </div>
                        <i class="fa-solid fa-file-circle-check card-header-icon"></i>
                    </div>

                    <c:choose>
                        <c:when test="${not empty pendingDocuments}">
                            <c:forEach var="doc" items="${pendingDocuments}">
                                <form action="${pageContext.request.contextPath}/documents/review" method="post" style="display:flex;flex-direction:column;gap:12px;margin-bottom:18px;">
                                    <input type="hidden" name="documentId" value="${doc.id}">
                                    <input type="hidden" name="internId" value="${doc.internProfileId}">
                                    <div style="background:#F7FAFC;padding:12px;border-radius:8px;border:1px solid var(--border-color);">
                                        <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:6px;"><strong style="font-size:13px;color:var(--primary-dark);word-break:break-word;">${doc.fileName}</strong><span class="badge pending">Chờ duyệt</span></div>
                                        <small style="color:var(--text-muted);font-size:11px;display:block;margin-bottom:8px;">Nộp bởi: ${doc.internFullName} (${doc.internCode}) • ${doc.uploadedAt}</small>
                                        <a href="${pageContext.request.contextPath}${doc.fileUrl}" target="_blank" rel="noopener" style="font-size:12px;color:var(--action-accent);font-weight:600;"><i class="fa-solid fa-eye"></i> Xem tệp PDF</a>
                                    </div>
                                    <div><label style="display:block;font-size:12px;font-weight:600;color:var(--primary-dark);margin-bottom:4px;">Trạng thái phê duyệt</label><select name="status" required style="width:100%;padding:8px 10px;border:1px solid var(--border-color);border-radius:6px;font-size:13px;background:#fff;"><option value="APPROVED">Đã duyệt (Hợp lệ)</option><option value="REJECTED">Từ chối (Cần nộp lại)</option></select></div>
                                    <div><label style="display:block;font-size:12px;font-weight:600;color:var(--primary-dark);margin-bottom:4px;">Ghi chú phản hồi cho TTS</label><textarea name="reviewNote" rows="2" placeholder="Nhập lý do hoặc nhận xét..." style="width:100%;padding:8px;box-sizing:border-box;border:1px solid var(--border-color);border-radius:6px;font-size:12px;"></textarea></div>
                                    <button type="submit" class="primary-btn" style="width:100%;justify-content:center;margin-top:4px;"><i class="fa-solid fa-check-double"></i> XÁC NHẬN DUYỆT TÀI LIỆU</button>
                                </form>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div style="padding:28px 12px;text-align:center;color:var(--text-muted);"><i class="fa-regular fa-folder-open" style="font-size:28px;color:var(--action-accent);display:block;margin-bottom:10px;"></i>Hiện không có tài liệu nào đang chờ duyệt.</div>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${false}">
                    <form action="${pageContext.request.contextPath}/documents/review" method="post" style="display: flex; flex-direction: column; gap: 12px;">
                        <input type="hidden" name="documentId" value="1">
                        <input type="hidden" name="internId" value="1">

                        <div style="background: #F7FAFC; padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
                            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;">
                                <strong style="font-size: 13px; color: var(--primary-dark);">CV_NguyenVanAn.pdf</strong>
                                <span class="badge pending">Chờ duyệt</span>
                            </div>
                            <small style="color: var(--text-muted); font-size: 11px; display: block; margin-bottom: 8px;">
                                Nộp bởi: Nguyễn Văn An (TTS001) • 01/10/2026
                            </small>
                            <a href="#" style="font-size: 12px; color: var(--action-accent); font-weight: 600;">
                                <i class="fa-solid fa-eye"></i> Xem trước tệp PDF
                            </a>
                        </div>

                        <div>
                            <label style="display: block; font-size: 12px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Trạng thái phê duyệt
                            </label>
                            <select name="status" required style="width: 100%; padding: 8px 10px; border: 1px solid var(--border-color); border-radius: 6px; font-size: 13px; background: #FFFFFF;">
                                <option value="APPROVED">Đã duyệt (Hợp lệ)</option>
                                <option value="REJECTED">Từ chối (Cần nộp lại)</option>
                            </select>
                        </div>

                        <div>
                            <label style="display: block; font-size: 12px; font-weight: 600; color: var(--primary-dark); margin-bottom: 4px;">
                                Ghi chú phản hồi cho TTS
                            </label>
                            <textarea name="reviewNote" rows="2" placeholder="Nhập lý do hoặc nhận xét..." style="width: 100%; padding: 8px; border: 1px solid var(--border-color); border-radius: 6px; font-size: 12px;"></textarea>
                        </div>

                        <button type="submit" class="primary-btn" style="width: 100%; justify-content: center; margin-top: 4px;">
                            <i class="fa-solid fa-check-double"></i> XÁC NHẬN DUYỆT TÀI LIỆU
                        </button>
                    </form>
                    </c:if>
                </div>

            </div>

        </section>

    </main>

</div>

<script>
    document.querySelectorAll('.menu-group-toggle').forEach(button => {
        button.addEventListener('click', () => {
            const group = button.closest('.menu-group');
            const content = group.querySelector('.menu-group-content');
            const isOpen = button.getAttribute('aria-expanded') === 'true';
            button.setAttribute('aria-expanded', String(!isOpen));
            content.hidden = isOpen;
            group.classList.toggle('is-open', !isOpen);
        });
    });
</script>
<script>
    (() => { const button=document.getElementById('accountMenuButton'), menu=document.getElementById('accountMenu'); if(!button||!menu)return; const close=()=>{menu.hidden=true;button.setAttribute('aria-expanded','false');}; button.addEventListener('click',()=>{const open=menu.hidden;menu.hidden=!open;button.setAttribute('aria-expanded',String(open));}); document.addEventListener('click',event=>{if(!event.target.closest('.account-menu'))close();}); document.addEventListener('keydown',event=>{if(event.key==='Escape')close();}); })();
</script>
<script>
    (() => {
        const button = document.getElementById('notificationButton'); const menu = document.getElementById('notificationMenu');
        if (!button || !menu) return;
        const close = () => { menu.hidden = true; button.setAttribute('aria-expanded', 'false'); };
        button.addEventListener('click', () => { const open = menu.hidden; menu.hidden = !open; button.setAttribute('aria-expanded', String(open)); if (open) { const form = new URLSearchParams({action:'mark-read'}); fetch('${pageContext.request.contextPath}/notifications', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:form}); const badge=document.getElementById('notificationBadge'); if(badge) badge.remove(); menu.querySelectorAll('.unread').forEach(item=>item.classList.remove('unread')); } });
        document.addEventListener('click', event => { if (!event.target.closest('.notification-wrap')) close(); });
    })();
</script>
</body>
</html>
