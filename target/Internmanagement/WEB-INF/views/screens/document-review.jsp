<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Duyệt tài liệu CV & Đơn xin thực tập - HR Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261004-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        .doc-card { background:#fff; border:1px solid var(--border-color); border-radius:10px; padding:18px; margin-bottom:14px; }
        .doc-card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
        .doc-meta { font-size:12px; color:var(--text-muted); margin-bottom:8px; }
        .doc-actions { display:flex; gap:10px; align-items:center; flex-wrap:wrap; margin-top:12px; }
        .btn-approve { background:var(--success-badge); color:#fff; border:none; padding:7px 16px; border-radius:7px; cursor:pointer; font-size:13px; font-weight:600; display:inline-flex; align-items:center; gap:6px; }
        .btn-reject  { background:var(--danger-badge);  color:#fff; border:none; padding:7px 16px; border-radius:7px; cursor:pointer; font-size:13px; font-weight:600; display:inline-flex; align-items:center; gap:6px; }
        .btn-view    { color:var(--action-accent); font-weight:600; text-decoration:none; font-size:13px; display:inline-flex; align-items:center; gap:5px; }
        textarea.note-input { width:100%; padding:8px; border:1px solid var(--border-color); border-radius:6px; font-size:12px; box-sizing:border-box; }
    </style>
</head>
<body>
<div class="layout">
    <!-- SIDEBAR HR -->
    <aside class="sidebar">
        <div class="logo"><div class="logo-icon"><i class="fa-solid fa-user-tie"></i></div><div><h2>IMS Portal</h2><span>Phòng Nhân sự (HR)</span></div></div>
        <nav class="menu">
            <p class="menu-title">NGHIỆP VỤ HR</p>
            <a href="${pageContext.request.contextPath}/home" class="menu-item"><i class="fa-solid fa-chart-line"></i><span>Dashboard HR</span></a>
            <a href="${pageContext.request.contextPath}/interns" class="menu-item"><i class="fa-solid fa-users"></i><span>Thực tập sinh & Lọc</span></a>
            <a href="${pageContext.request.contextPath}/interns/new" class="menu-item"><i class="fa-solid fa-user-plus"></i><span>Thêm hồ sơ TTS</span></a>
            <a href="${pageContext.request.contextPath}/documents/review" class="menu-item active"><i class="fa-solid fa-file-circle-check"></i><span>Duyệt tài liệu & CV</span></a>
        </nav>
        <div class="sidebar-bottom">
            <a href="${pageContext.request.contextPath}/logout" class="menu-item logout"><i class="fa-solid fa-right-from-bracket"></i><span>Đăng xuất</span></a>
        </div>
    </aside>

    <!-- MAIN -->
    <main class="main">
        <header class="header">
            <div class="search-box"><i class="fa-solid fa-magnifying-glass"></i><input type="text" placeholder="Tìm kiếm tài liệu..."></div>
            <div class="header-right">
                <div class="user">
                    <div class="avatar">${currentUser.shortName}</div>
                    <div class="user-info"><strong>${currentUser.fullName}</strong><small>HR Manager</small></div>
                    <i class="fa-solid fa-chevron-down"></i>
                </div>
            </div>
        </header>

        <section class="content">
            <div class="page-title">
                <div>
                    <h1><i class="fa-solid fa-file-circle-check" style="color:var(--action-accent);margin-right:10px;"></i>Duyệt tài liệu CV & Đơn xin thực tập</h1>
                    <p>Xem xét và phê duyệt hoặc từ chối tài liệu do thực tập sinh nộp lên</p>
                </div>
                <span class="badge pending" style="font-size:14px;padding:8px 16px;">
                    <i class="fa-solid fa-clock"></i> 
                    ${not empty pendingDocuments ? pendingDocuments.size() : 0} chờ duyệt
                </span>
            </div>

            <c:if test="${param.reviewed == '1'}">
                <div style="background:#F0FFF4;color:var(--success-badge);border:1px solid #C6F6D5;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    <i class="fa-solid fa-circle-check"></i> Đã xử lý tài liệu thành công!
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div style="background:#FFF5F5;color:var(--danger-badge);border:1px solid #FED7D7;padding:12px 16px;border-radius:8px;margin-bottom:20px;font-weight:600;">
                    <i class="fa-solid fa-triangle-exclamation"></i> ${error}
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty pendingDocuments}">
                    <c:forEach var="doc" items="${pendingDocuments}">
                        <div class="doc-card">
                            <div class="doc-card-header">
                                <div>
                                    <strong style="font-size:15px;color:var(--primary-dark);">
                                        <i class="fa-regular fa-file-pdf" style="color:var(--danger-badge);margin-right:6px;"></i>
                                        ${doc.fileName}
                                    </strong>
                                    <div class="doc-meta">
                                        Loại: <strong>${doc.documentType}</strong> &nbsp;|&nbsp;
                                        TTS: <strong>${doc.internFullName}</strong> (${doc.internCode}) &nbsp;|&nbsp;
                                        Ngày nộp: ${doc.uploadedAt}
                                    </div>
                                </div>
                                <span class="badge pending"><i class="fa-solid fa-clock"></i> Chờ duyệt</span>
                            </div>

                            <div class="doc-actions">
                                <a href="${pageContext.request.contextPath}${doc.fileUrl}" target="_blank" class="btn-view">
                                    <i class="fa-solid fa-eye"></i> Xem tệp PDF
                                </a>
                                <a href="${pageContext.request.contextPath}/interns/profile?id=${doc.internProfileId}" class="btn-view" style="color:var(--warning-badge);">
                                    <i class="fa-solid fa-id-card"></i> Xem hồ sơ TTS
                                </a>
                            </div>

                            <!-- Form duyệt nhanh -->
                            <form action="${pageContext.request.contextPath}/documents/review" method="post" style="display:flex;gap:12px;align-items:flex-end;margin-top:14px;border-top:1px solid var(--border-color);padding-top:14px;flex-wrap:wrap;">
                                <input type="hidden" name="documentId" value="${doc.id}">
                                <input type="hidden" name="internId" value="${doc.internProfileId}">
                                <div style="flex:1;min-width:200px;">
                                    <label style="font-size:12px;font-weight:600;color:var(--primary-dark);display:block;margin-bottom:4px;">Ghi chú phản hồi cho TTS</label>
                                    <textarea name="reviewNote" class="note-input" rows="2" placeholder="Nhập nhận xét hoặc lý do từ chối..."></textarea>
                                </div>
                                <div style="display:flex;gap:8px;">
                                    <button type="submit" name="status" value="APPROVED" class="btn-approve">
                                        <i class="fa-solid fa-check-double"></i> Phê duyệt
                                    </button>
                                    <button type="submit" name="status" value="REJECTED" class="btn-reject">
                                        <i class="fa-solid fa-xmark"></i> Từ chối
                                    </button>
                                </div>
                            </form>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="card" style="text-align:center;padding:60px 20px;">
                        <i class="fa-solid fa-clipboard-check" style="font-size:3rem;color:var(--success-badge);opacity:0.4;margin-bottom:16px;display:block;"></i>
                        <h3 style="color:var(--text-muted);">Không có tài liệu nào đang chờ duyệt</h3>
                        <p style="color:var(--text-muted);">Tất cả hồ sơ đã được xử lý hoặc chưa có thực tập sinh nào nộp tài liệu.</p>
                        <a href="${pageContext.request.contextPath}/interns" class="primary-btn" style="display:inline-flex;margin-top:16px;">
                            <i class="fa-solid fa-users"></i> Xem danh sách TTS
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

        </section>
    </main>
</div>
</body>
</html>
