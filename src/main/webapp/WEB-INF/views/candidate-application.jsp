<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Hồ sơ ứng tuyển - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/business.css">
</head>
<body class="auth-page">
<main class="auth-shell">
    <section class="auth-intro">
        <div class="auth-logo">IMS Portal</div>
        <h1>Hồ sơ ứng tuyển thực tập</h1>
        <p>Hoàn thiện thông tin cá nhân và gửi CV trực tuyến. Bạn có thể cập nhật hồ sơ trước khi được xét duyệt.</p>
        <ul>
            <li>CV định dạng PDF, dung lượng tối đa 10 MB</li>
            <li>Theo dõi trạng thái xét duyệt trực tuyến</li>
            <li>Nhận phản hồi từ Phòng Nhân sự</li>
        </ul>
    </section>
    <section class="auth-card">
        <div class="auth-heading">
            <p>Ứng viên</p>
            <h2>Nộp hồ sơ trực tuyến</h2>
            <span>${currentUser.fullName} · ${currentUser.email}</span>
        </div>
        <c:if test="${not empty errorMessage}">
            <div class="alert error"><c:out value="${errorMessage}"/></div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert success"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty application}">
            <div class="alert">
                Trạng thái hồ sơ: <strong><c:out value="${application.applicationStatus}"/></strong>
                <c:if test="${not empty application.hrNotes}">
                    <br>Phản hồi: <c:out value="${application.hrNotes}"/>
                </c:if>
                <c:if test="${not empty application.cvFileUrl}">
                    <br><a href="${pageContext.request.contextPath}${application.cvFileUrl}">Tải CV đã nộp</a>
                </c:if>
            </div>
        </c:if>
        <form method="post" enctype="multipart/form-data" class="auth-form">
            <label>Số điện thoại *
                <input name="phone" type="tel" required maxlength="20" pattern="\+?[0-9]{8,15}"
                       value="<c:out value='${application.phone}'/>" placeholder="0912345678">
            </label>
            <label>Trường đại học *
                <input name="universityName" required maxlength="150"
                       value="<c:out value='${application.universityName}'/>">
            </label>
            <label>Chuyên ngành *
                <input name="majorName" required maxlength="100"
                       value="<c:out value='${application.majorName}'/>">
            </label>
            <label>Vị trí ứng tuyển *
                <input name="desiredPosition" required maxlength="100"
                       value="<c:out value='${application.desiredPosition}'/>">
            </label>
            <label>CV (PDF) *
                <input name="cv" type="file" accept="application/pdf,.pdf"
                       ${empty application.cvFileUrl ? 'required' : ''}>
                <span class="field-help">Tối đa 10 MB. Nếu đã nộp CV, có thể để trống khi cập nhật thông tin.</span>
            </label>
            <div class="auth-actions">
                <a href="${pageContext.request.contextPath}/logout" class="cancel-link">Đăng xuất</a>
                <button type="submit">Nộp hồ sơ</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
