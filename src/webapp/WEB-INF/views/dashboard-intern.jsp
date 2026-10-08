<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng Thực tập sinh - Upload CV & Đơn xin thực tập</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .account-menu { position:relative; }
        .account-trigger { display:flex; align-items:center; gap:12px; border:0; background:transparent; color:inherit; cursor:pointer; padding:5px 8px; border-radius:9px; text-align:left; }
        .account-trigger:hover, .account-trigger[aria-expanded="true"] { background:rgba(255,255,255,.09); }
        .account-trigger .user-info { min-width:0; }
        .account-dropdown { position:absolute; z-index:100; top:calc(100% + 10px); right:0; min-width:210px; padding:7px; border:1px solid #dce5f0; border-radius:10px; background:#fff; box-shadow:0 14px 30px rgba(17,45,78,.18); }
        .account-dropdown[hidden] { display:none; }
        .account-dropdown a { display:flex; align-items:center; gap:10px; padding:10px 11px; border-radius:7px; color:#234a7d; text-decoration:none; font-size:14px; font-weight:600; }
        .account-dropdown a:hover { background:#edf3fb; }
        .account-dropdown .logout-link { color:#bd3f3f; }
        .notification-wrap { position:relative; }.notification-menu { position:absolute; z-index:120; top:calc(100% + 10px); right:0; width:350px; max-height:420px; overflow:auto; background:#fff; border:1px solid #dce5f0; border-radius:11px; box-shadow:0 16px 34px rgba(17,45,78,.2); }.notification-menu[hidden] { display:none; }.notification-menu h3{margin:0;padding:15px 16px;color:#163a69;border-bottom:1px solid #e6edf5;font-size:15px}.notification-item{display:block;padding:12px 16px;border-bottom:1px solid #edf2f7;color:inherit;text-decoration:none}.notification-item:hover{background:#f2f7fd}.notification-item.unread{background:#ebf4ff}.notification-item strong{display:block;color:#173d70;font-size:13px}.notification-item span{display:block;color:#6b7e95;font-size:12px;margin-top:4px;line-height:1.35}.notification-empty{padding:26px 16px;color:#718096;text-align:center;font-size:13px}
    </style>
</head>

<body>

<div class="layout">

    <!-- ================= SIDEBAR INTERN ================= -->
    <aside class="sidebar">

        <div class="logo">
            <div class="logo-icon">
                <i class="fa-solid fa-user-graduate"></i>
            </div>
            <div>
                <h2>IMS Portal</h2>
                <span>Dành cho Thực tập sinh</span>
            </div>
        </div>

        <nav class="menu">
            <p class="menu-title">TRANG CÁ NHÂN</p>

            <a href="${pageContext.request.contextPath}/home" class="menu-item active">
                <i class="fa-solid fa-cloud-arrow-up"></i>
                <span>Nộp hồ sơ & CV</span>
            </a>

            <a href="${pageContext.request.contextPath}/interns/profile" class="menu-item">
                <i class="fa-solid fa-id-card"></i>
                <span>Hồ sơ của tôi</span>
            </a>

            <a href="${pageContext.request.contextPath}/my-contract" class="menu-item"><i class="fa-solid fa-file-contract"></i><span>Hợp đồng của tôi</span></a>
            <a href="${pageContext.request.contextPath}/my-calendar" class="menu-item"><i class="fa-solid fa-calendar"></i><span>Lịch thực tập</span></a>
            <a href="${pageContext.request.contextPath}/attendance" class="menu-item"><i class="fa-solid fa-clock"></i><span>Check-in / Check-out</span></a>

            <p class="menu-title">TIẾN ĐỘ THỰC TẬP</p>

            <a href="${pageContext.request.contextPath}/tasks" class="menu-item">
                <i class="fa-solid fa-list-check"></i>
                <span>Nhiệm vụ</span>
            </a>

            <a href="${pageContext.request.contextPath}/weekly-reports" class="menu-item">
                <i class="fa-solid fa-file-lines"></i>
                <span>Báo cáo tuần</span>
            </a>
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
                <input type="text" placeholder="Tìm kiếm tài liệu, thông báo...">
            </div>

            <div class="header-right">
                <div class="notification-wrap">
                    <button class="notification" id="notificationButton" type="button" aria-label="Thông báo" aria-expanded="false"><i class="fa-regular fa-bell"></i><c:if test="${unreadNotificationCount > 0}"><span id="notificationBadge">${unreadNotificationCount}</span></c:if></button>
                    <div class="notification-menu" id="notificationMenu" hidden><h3><i class="fa-solid fa-bell"></i> Thông báo</h3><c:choose><c:when test="${empty recentNotifications}"><div class="notification-empty">Chưa có thông báo mới.</div></c:when><c:otherwise><c:forEach var="item" items="${recentNotifications}"><a class="notification-item ${item.read ? '' : 'unread'}" href="${pageContext.request.contextPath}${not empty item.targetUrl ? item.targetUrl : '/home'}"><strong>${item.title}</strong><span>${item.message}</span></a></c:forEach></c:otherwise></c:choose><a class="notification-item" style="text-align:center;color:var(--action-accent);font-weight:700" href="${pageContext.request.contextPath}/notifications">Xem tất cả thông báo <i class="fa-solid fa-arrow-right"></i></a></div>
                </div>

                <div class="account-menu">
                    <div class="account-trigger user" id="accountMenuButton" role="button" tabindex="0" aria-expanded="false" aria-controls="accountMenu">
                    <div class="avatar">
                        ${currentUser.shortName != null ? currentUser.shortName : 'TTS'}
                    </div>

                    <div class="user-info">
                        <strong>${currentUser.fullName != null ? currentUser.fullName : 'Thực tập sinh'}</strong>
                        <small>Thực tập sinh (INTERN)</small>
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
                    <h1>Hồ sơ & Tài liệu của tôi</h1>
                    <p>Upload CV, đơn xin thực tập và xem trạng thái xét duyệt từ Phòng Nhân sự (HR).</p>
                </div>
            </div>

            <c:if test="${param.uploaded == '1'}">
                <div style="background: var(--success-bg); color: var(--success-badge); border: 1px solid var(--success-border); padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    <i class="fa-solid fa-circle-check"></i> Tải lên tài liệu thành công! HR sẽ tiến hành xét duyệt hồ sơ của bạn.
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div style="background: var(--danger-bg); color: var(--danger-badge); border: 1px solid var(--danger-border); padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600;">
                    <i class="fa-solid fa-triangle-exclamation"></i> ${error}
                </div>
            </c:if>


            <!-- GRID: FORM UPLOAD & DANH SÁCH TÀI LIỆU -->
            <div class="dashboard-grid">

                <!-- FORM UPLOAD CV & ĐƠN XIN THỰC TẬP -->
                <div class="card">
                    <div class="card-header">
                        <div>
                            <h3>Upload Tài liệu mới</h3>
                            <p>Tải lên CV (PDF) hoặc Đơn xin thực tập để hoàn thiện hồ sơ</p>
                        </div>
                        <i class="fa-solid fa-file-pdf card-header-icon"></i>
                    </div>

                    <form action="${pageContext.request.contextPath}/documents/upload" method="post" enctype="multipart/form-data" style="display: flex; flex-direction: column; gap: 16px;">
                        
                        <c:if test="${not empty internProfile}">
                            <input type="hidden" name="internId" value="${internProfile.id}">
                        </c:if>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 6px;">
                                Loại tài liệu <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <select name="documentType" required style="width: 100%; padding: 10px 12px; border: 1px solid var(--border-color); border-radius: 8px; font-size: 14px; background: #FFFFFF;">
                                <option value="CV">CV / Sơ yếu lý lịch (PDF)</option>
                                <option value="APPLICATION_LETTER">Đơn xin thực tập (PDF)</option>
                                <option value="RECOMMENDATION_LETTER">Giấy giới thiệu nhà trường (PDF)</option>
                                <option value="WEEKLY_REPORT">Báo cáo thực tập (PDF)</option>
                            </select>
                        </div>

                        <div>
                            <label style="display: block; font-size: 13px; font-weight: 600; color: var(--primary-dark); margin-bottom: 6px;">
                                Chọn tệp PDF <span style="color: var(--danger-badge)">*</span>
                            </label>
                            <input type="file" name="file" accept=".pdf" required style="width: 100%; padding: 10px; border: 1px dashed var(--action-accent); border-radius: 8px; background: #F7FAFC; cursor: pointer;">
                            <small style="color: var(--text-muted); font-size: 11px; display: block; margin-top: 4px;">
                                Chỉ chấp nhận tệp định dạng PDF, tối đa 10 MB.
                            </small>
                        </div>

                        <button type="submit" class="primary-btn" style="width: 100%; justify-content: center; margin-top: 8px;">
                            <i class="fa-solid fa-upload"></i> TẢI LÊN TÀI LIỆU
                        </button>
                    </form>
                </div>


                <!-- TIẾN ĐỘ HỒ SƠ CÁ NHÂN -->
                <div class="card">
                    <div class="card-header">
                        <div>
                            <h3>Tiến độ Hồ sơ</h3>
                            <p>Tình trạng hoàn thiện thủ tục thực tập</p>
                        </div>
                        <i class="fa-solid fa-list-check card-header-icon"></i>
                    </div>

                    <div class="report-status">
                        <div>
                            <span class="dot ${hasCv ? 'green-dot' : 'orange-dot'}"></span>
                            CV & Sơ yếu lý lịch
                            <strong>${hasCv ? 'Đã nộp' : 'Chưa nộp'}</strong>
                        </div>

                        <div>
                            <span class="dot ${hasLetter ? 'green-dot' : 'orange-dot'}"></span>
                            Đơn xin thực tập
                            <strong>${hasLetter ? 'Đã nộp' : 'Chưa nộp'}</strong>
                        </div>

                        <div>
                            <span class="dot ${hasRecommendation ? 'green-dot' : 'orange-dot'}"></span>
                            Giấy giới thiệu nhà trường
                            <strong>${hasRecommendation ? 'Đã nộp' : 'Chưa nộp'}</strong>
                        </div>

                        <div>
                            <span class="dot ${hasWeeklyReport ? 'green-dot' : 'orange-dot'}"></span>
                            Báo cáo thực tập
                            <strong>${hasWeeklyReport ? 'Đã nộp' : 'Chưa nộp'}</strong>
                        </div>

                        <div>
                            <span class="dot green-dot"></span>
                            Trạng thái tiếp nhận
                            <strong style="color: var(--success-badge)">ĐANG THỰC TẬP</strong>
                        </div>
                    </div>
                </div>


                <!-- BẢNG DANH SÁCH TÀI LIỆU ĐÃ UPLOAD -->
                <div class="card large-card" style="grid-column: span 2;">
                    <div class="card-header">
                        <div>
                            <h3>Danh sách tài liệu đã tải lên</h3>
                            <p>Theo dõi phản hồi và trạng thái phê duyệt từ HR</p>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Loại tài liệu</th>
                                    <th>Tên tệp PDF</th>
                                    <th>Ngày tải lên</th>
                                    <th>Trạng thái duyệt</th>
                                    <th>Ghi chú từ HR</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty documents}">
                                        <c:forEach var="doc" items="${documents}">
                                            <tr>
                                                <td><strong>${doc.documentType}</strong></td>
                                                <td><i class="fa-regular fa-file-pdf" style="color: var(--danger-badge); margin-right: 6px;"></i> ${doc.fileName}</td>
                                                <td>${doc.uploadedAt}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${doc.approvalStatus == 'APPROVED'}">
                                                            <span class="badge active"><i class="fa-solid fa-check"></i> Đã duyệt</span>
                                                        </c:when>
                                                        <c:when test="${doc.approvalStatus == 'REJECTED'}">
                                                            <span class="badge inactive"><i class="fa-solid fa-xmark"></i> Từ chối</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge pending"><i class="fa-solid fa-clock"></i> Chờ duyệt</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${doc.rejectionNote != null ? doc.rejectionNote : '—'}</td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}${doc.fileUrl}" target="_blank" style="color: var(--action-accent); font-weight: 600;">
                                                        <i class="fa-solid fa-eye"></i> Xem file
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${false}">
                                        <tr>
                                            <td><strong>CV Thực tập</strong></td>
                                            <td><i class="fa-regular fa-file-pdf" style="color: var(--danger-badge); margin-right: 6px;"></i> CV_NguyenVanA.pdf</td>
                                            <td>01/10/2026</td>
                                            <td><span class="badge active"><i class="fa-solid fa-check"></i> Đã duyệt</span></td>
                                            <td>Hồ sơ hợp lệ</td>
                                            <td><a href="#" style="color: var(--action-accent); font-weight: 600;"><i class="fa-solid fa-eye"></i> Xem file</a></td>
                                        </tr>
                                        <tr>
                                            <td><strong>Đơn xin thực tập</strong></td>
                                            <td><i class="fa-regular fa-file-pdf" style="color: var(--danger-badge); margin-right: 6px;"></i> DonXinThucTap.pdf</td>
                                            <td>01/10/2026</td>
                                            <td><span class="badge pending"><i class="fa-solid fa-clock"></i> Chờ duyệt</span></td>
                                            <td>Đang chờ HR xác thực</td>
                                            <td><a href="#" style="color: var(--action-accent); font-weight: 600;"><i class="fa-solid fa-eye"></i> Xem file</a></td>
                                        </tr>
                                        </c:if>
                                        <tr>
                                            <td colspan="6" style="padding:34px 18px;text-align:center;color:var(--text-muted);">
                                                <i class="fa-regular fa-folder-open" style="font-size:26px;display:block;margin-bottom:10px;color:var(--action-accent);"></i>
                                                Bạn chưa tải lên tài liệu nào. Hãy sử dụng biểu mẫu phía trên để nộp CV hoặc đơn xin thực tập.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                </div>

            </div>

        </section>

    </main>

</div>

<script>
    (() => {
        const button = document.getElementById('accountMenuButton');
        const menu = document.getElementById('accountMenu');
        if (!button || !menu) return;
        const closeMenu = () => { menu.hidden = true; button.setAttribute('aria-expanded', 'false'); };
        button.addEventListener('click', () => {
            const open = menu.hidden;
            menu.hidden = !open;
            button.setAttribute('aria-expanded', String(open));
        });
        document.addEventListener('click', event => { if (!event.target.closest('.account-menu')) closeMenu(); });
        document.addEventListener('keydown', event => { if (event.key === 'Escape') closeMenu(); });
    })();
    // Trang hồ sơ hiện theo dõi hai tài liệu bắt buộc: CV và đơn xin thực tập.
    // Loại bỏ các lựa chọn chưa có tiến độ tương ứng để tránh gây nhầm lẫn.
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
