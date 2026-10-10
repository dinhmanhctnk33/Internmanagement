<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ Thực tập sinh - ${intern.userFullName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
        .info-row { display: flex; flex-direction: column; gap: 3px; }
        .info-label { font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
        .info-value { font-size: 14px; color: var(--primary-dark); font-weight: 500; }
    </style>
</head>
<body>
<div class="layout">
    <!-- SIDEBAR -->
    <c:choose>
        <c:when test="${sessionScope.userRole == 'INTERN'}">
        <aside class="sidebar">
            <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-graduate"></i></div><div><h2>IMS Portal</h2><span>Thực tập sinh</span></div></div>
            <nav class="menu">
                <p class="menu-title">TRANG CÁ NHÂN</p>
                <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-cloud-arrow-up"></i><span>Nộp hồ sơ & CV</span></a>
                <a href="${pageContext.request.contextPath}/interns/profile" class="menu-item active"><i class="fa-solid fa-id-card"></i><span>Hồ sơ của tôi</span></a>
            </nav>
            
        </aside>
        </c:when>
        <c:otherwise>
        <aside class="sidebar">
            <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-tie"></i></div><div><h2>IMS Portal</h2><span>Phòng Nhân sự (HR)</span></div></div>
            <nav class="menu">
                <p class="menu-title">NGHIỆP VỤ HR</p>
                <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-chart-line"></i><span>Dashboard HR</span></a>
                <a href="${pageContext.request.contextPath}/interns" class="menu-item active"><i class="fa-solid fa-users"></i><span>Thực tập sinh & Lọc</span></a>
                <a href="${pageContext.request.contextPath}/interns/new" class="menu-item"><i class="fa-solid fa-user-plus"></i><span>Thêm hồ sơ TTS</span></a>
                <a href="${pageContext.request.contextPath}/documents/review" class="menu-item"><i class="fa-solid fa-file-circle-check"></i><span>Duyệt tài liệu & CV</span></a>
            </nav>
            
        </aside>
        </c:otherwise>
    </c:choose>

    <!-- MAIN -->
    <main class="main">
        <header class="header intern-header">
            <div class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input type="text" placeholder="Tìm kiếm..."></div>
            <%@ include file="../includes/hr-header-actions.jspf" %>
        </header>

        <section class="content">

            <c:if test="${param.success == '1'}">
                <div style="background:#F0FFF4;color:var(--success-badge);border:1px solid #C6F6D5;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    <i class="fa-solid fa-circle-check"></i> Cập nhật hồ sơ thành công!
                </div>
            </c:if>
            <c:if test="${param.uploaded == '1'}">
                <div style="background:#F0FFF4;color:var(--success-badge);border:1px solid #C6F6D5;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    <i class="fa-solid fa-circle-check"></i> Tải lên tài liệu thành công! HR sẽ xét duyệt sớm.
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty intern}">

                    <div class="page-title">
                        <div>
                            <h1><i class="fa-solid fa-id-card" style="color:var(--action-accent);margin-right:10px;"></i>Hồ sơ: ${intern.userFullName}</h1>
                            <p>Mã TTS: <strong>${intern.internCode}</strong> &nbsp;|&nbsp; ${intern.universityName} - ${intern.majorName}</p>
                        </div>
                        <c:if test="${sessionScope.userRole != 'INTERN'}">
                        <a href="${pageContext.request.contextPath}/interns/edit?id=${intern.id}" class="primary-btn">
                            <i class="fa-solid fa-pen-to-square"></i> Chỉnh sửa hồ sơ
                        </a>
                        </c:if>
                    </div>

                    <!-- Thông tin hồ sơ -->
                    <div class="dashboard-grid" style="margin-bottom:24px;">
                        <div class="card">
                            <div class="card-header"><div><h3>Thông tin cơ bản</h3><p>Thông tin hồ sơ thực tập sinh</p></div><i class="fa-solid fa-user card-header-icon"></i></div>
                            <div class="profile-grid">
                                <div class="info-row"><span class="info-label">Họ và tên</span><span class="info-value">${intern.userFullName}</span></div>
                                <div class="info-row"><span class="info-label">Email</span><span class="info-value">${intern.userEmail}</span></div>
                                <div class="info-row"><span class="info-label">Mã thực tập sinh</span><span class="info-value">${intern.internCode}</span></div>
                                <div class="info-row"><span class="info-label">Điện thoại</span><span class="info-value">${not empty intern.phoneNumber ? intern.phoneNumber : '—'}</span></div>
                                <div class="info-row"><span class="info-label">Trường đại học</span><span class="info-value">${intern.universityName}</span></div>
                                <div class="info-row"><span class="info-label">Ngành học</span><span class="info-value">${intern.majorName}</span></div>
                                <div class="info-row"><span class="info-label">Mentor hướng dẫn</span><span class="info-value">${not empty intern.mentorName ? intern.mentorName : 'Chưa phân công'}</span></div>
                                <div class="info-row">
                                    <span class="info-label">Trạng thái</span>
                                    <span>
                                        <c:choose>
                                            <c:when test="${intern.internshipStatus == 'ACTIVE'}"><span class="badge active"><i class="fa-solid fa-check"></i> Đang thực tập</span></c:when>
                                            <c:when test="${intern.internshipStatus == 'COMPLETED'}"><span class="badge active">Hoàn thành</span></c:when>
                                            <c:when test="${intern.internshipStatus == 'PAUSED'}"><span class="badge pending">Tạm dừng</span></c:when>
                                            <c:otherwise><span class="badge inactive">${intern.internshipStatus}</span></c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- Upload tài liệu (chỉ cho INTERN) -->
                        <c:if test="${sessionScope.userRole == 'INTERN'}">
                        <div class="card">
                            <div class="card-header"><div><h3>Upload tài liệu</h3><p>Tải lên CV hoặc đơn xin thực tập</p></div><i class="fa-solid fa-file-pdf card-header-icon"></i></div>
                            <form action="${pageContext.request.contextPath}/documents/upload" method="post" enctype="multipart/form-data" style="display:flex;flex-direction:column;gap:14px;">
                                <input type="hidden" name="internId" value="${intern.id}">
                                <div>
                                    <label style="font-size:13px;font-weight:600;color:var(--primary-dark);display:block;margin-bottom:6px;">Loại tài liệu</label>
                                    <select name="documentType" style="width:100%;padding:10px 12px;border:1px solid var(--border-color);border-radius:8px;font-size:14px;background:#fff;">
                                        <option value="CV">CV / Sơ yếu lý lịch</option>
                                        <option value="APPLICATION_LETTER">Đơn xin thực tập</option>
                                        <option value="RECOMMENDATION_LETTER">Giấy giới thiệu</option>
                                        <option value="WEEKLY_REPORT">Báo cáo thực tập</option>
                                    </select>
                                </div>
                                <div>
                                    <label style="font-size:13px;font-weight:600;color:var(--primary-dark);display:block;margin-bottom:6px;">Chọn tệp PDF *</label>
                                    <input type="file" name="file" accept=".pdf" required style="width:100%;padding:10px;border:1px dashed var(--action-accent);border-radius:8px;background:#F7FAFC;box-sizing:border-box;">
                                </div>
                                <button type="submit" class="primary-btn" style="justify-content:center;">
                                    <i class="fa-solid fa-upload"></i> TẢI LÊN
                                </button>
                            </form>
                        </div>
                        </c:if>
                    </div>

                    <!-- Danh sách tài liệu -->
                    <div class="card">
                        <div class="card-header">
                            <div><h3>Tài liệu đã nộp</h3><p>CV, đơn xin thực tập và các tài liệu khác</p></div>
                            <i class="fa-solid fa-file-circle-check card-header-icon"></i>
                        </div>
                        <div class="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Loại tài liệu</th>
                                        <th>Tên tệp</th>
                                        <th>Ngày tải lên</th>
                                        <th>Trạng thái</th>
                                        <th>Ghi chú</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty documents}">
                                            <c:forEach var="doc" items="${documents}">
                                                <tr>
                                                    <td><strong>${doc.documentType}</strong></td>
                                                    <td><i class="fa-regular fa-file-pdf" style="color:var(--danger-badge);margin-right:6px;"></i>${doc.fileName}</td>
                                                    <td>${doc.uploadedAt}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${doc.approvalStatus == 'APPROVED'}"><span class="badge active"><i class="fa-solid fa-check"></i> Đã duyệt</span></c:when>
                                                            <c:when test="${doc.approvalStatus == 'REJECTED'}"><span class="badge inactive"><i class="fa-solid fa-xmark"></i> Từ chối</span></c:when>
                                                            <c:otherwise><span class="badge pending"><i class="fa-solid fa-clock"></i> Chờ duyệt</span></c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>${not empty doc.rejectionNote ? doc.rejectionNote : '—'}</td>
                                                    <td style="display:flex;gap:10px;align-items:center;">
                                                        <a href="${pageContext.request.contextPath}${doc.fileUrl}" target="_blank" style="color:var(--action-accent);font-weight:600;">
                                                            <i class="fa-solid fa-eye"></i> Xem
                                                        </a>
                                                        <!-- HR: Duyệt tài liệu -->
                                                        <c:if test="${sessionScope.userRole != 'INTERN' && doc.approvalStatus == 'PENDING'}">
                                                        <form action="${pageContext.request.contextPath}/documents/review" method="post" style="display:inline;">
                                                            <input type="hidden" name="documentId" value="${doc.id}">
                                                            <input type="hidden" name="internId" value="${intern.id}">
                                                            <input type="hidden" name="status" value="APPROVED">
                                                            <button type="submit" style="background:var(--success-badge);color:#fff;border:none;padding:4px 10px;border-radius:5px;cursor:pointer;font-size:12px;font-weight:600;">
                                                                <i class="fa-solid fa-check"></i> Duyệt
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/documents/review" method="post" style="display:inline;">
                                                            <input type="hidden" name="documentId" value="${doc.id}">
                                                            <input type="hidden" name="internId" value="${intern.id}">
                                                            <input type="hidden" name="status" value="REJECTED">
                                                            <button type="submit" style="background:var(--danger-badge);color:#fff;border:none;padding:4px 10px;border-radius:5px;cursor:pointer;font-size:12px;font-weight:600;">
                                                                <i class="fa-solid fa-xmark"></i> Từ chối
                                                            </button>
                                                        </form>
                                                        </c:if>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="6" style="text-align:center;padding:30px;color:var(--text-muted);">
                                                    <i class="fa-regular fa-folder-open" style="font-size:2rem;display:block;margin-bottom:10px;opacity:0.3;"></i>
                                                    Chưa có tài liệu nào được tải lên.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>

                </c:when>
                <c:otherwise>
                    <div class="card" style="text-align:center;padding:60px 20px;">
                        <i class="fa-solid fa-user-slash" style="font-size:3rem;color:var(--text-muted);opacity:0.3;margin-bottom:16px;display:block;"></i>
                        <h3 style="color:var(--text-muted);">Không tìm thấy hồ sơ thực tập sinh</h3>
                        <p style="color:var(--text-muted);">Hồ sơ chưa được tạo hoặc ID không hợp lệ.</p>
                        <c:if test="${sessionScope.userRole != 'INTERN'}">
                        <a href="${pageContext.request.contextPath}/interns/new" class="primary-btn" style="display:inline-flex;margin-top:16px;">
                            <i class="fa-solid fa-plus"></i> Tạo hồ sơ mới
                        </a>
                        </c:if>
                    </div>
                </c:otherwise>
            </c:choose>

        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
