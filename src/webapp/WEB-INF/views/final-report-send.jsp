<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Gửi báo cáo #${snapshot.id} - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/final-report.css?v=20261013-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<c:set var="activeMenu" value="reports"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="anySend" value="false"/>
<c:forEach items="${groups}" var="g"><c:if test="${g.canSend}"><c:set var="anySend" value="true"/></c:if></c:forEach>
<div class="layout">
    <%@ include file="includes/final-report-sidebar.jspf" %>
    <main class="main">
        <header class="header intern-header"><div class="report-context header-context"><i class="fa-solid fa-paper-plane"></i><span>Gửi báo cáo</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content attendance-page">
            <div class="report-heading">
                <div><p class="eyebrow">Bản chụp #${snapshot.id}</p><h1>Gửi ${snapshot.leaderReport ? 'báo cáo cho ban lãnh đạo' : 'báo cáo cho trường'}</h1>
                    <p>Xem trước email và tệp đính kèm, chọn người nhận trong danh bạ rồi xác nhận gửi.</p></div>
                <a class="fr-btn fr-btn-secondary" href="${ctx}/final-reports?id=${snapshot.id}"><i class="fa-solid fa-arrow-left"></i> Quay lại</a>
            </div>

            <c:if test="${not empty param.error}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>
            <c:if test="${!smtpConfigured}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> Hệ thống chưa cấu hình email (SMTP) nên chưa gửi được. Bạn vẫn có thể xuất file.</div></c:if>

            <section class="report-card fr-card">
                <div class="card-heading"><div><h2>Xem trước email</h2><p>Thân email chỉ có thông tin chung. Toàn bộ dữ liệu nằm trong tệp PDF đính kèm.</p></div></div>
                <div class="fr-mail">
                    <p><b>Tiêu đề:</b> <c:out value="${emailSubject}"/></p>
                    <c:forEach items="${emailParagraphs}" var="line"><p><c:out value="${line}"/></p></c:forEach>
                </div>
            </section>

            <form method="post" action="${ctx}/final-reports/send" id="sendForm">
                <input type="hidden" name="action" value="send">
                <input type="hidden" name="id" value="${snapshot.id}">
                <input type="hidden" name="batchToken" value="${batchToken}">

                <c:forEach items="${groups}" var="g">
                    <section class="report-card fr-card">
                        <div class="card-heading"><div><h2><c:out value="${g.label}"/></h2>
                            <c:if test="${g.canSend}"><p><i class="fa-solid fa-paperclip"></i> Tệp đính kèm: <c:out value="${g.fileName}"/> (${g.sizeKb} KB)</p></c:if></div></div>
                        <c:choose>
                            <c:when test="${g.canSend}">
                                <div class="fr-programs">
                                    <c:forEach items="${g.recipients}" var="r">
                                        <label class="fr-choice"><input type="checkbox" name="recipientIds" value="${r.id}" checked>
                                            <c:out value="${r.contactName}"/> &lt;<c:out value="${r.email}"/>&gt;</label>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise><div class="fr-warn"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${g.reason}"/></div></c:otherwise>
                        </c:choose>
                    </section>
                </c:forEach>

                <div class="fr-actions">
                    <button class="fr-btn" type="submit" id="sendButton" ${anySend && smtpConfigured ? '' : 'disabled'}><i class="fa-solid fa-paper-plane"></i> Xác nhận và gửi</button>
                    <a class="fr-btn fr-btn-secondary" href="${ctx}/final-reports?id=${snapshot.id}">Hủy</a>
                </div>
            </form>
        </section>
    </main>
</div>
<script>
    document.getElementById('sendForm').addEventListener('submit', function (e) {
        if (!document.querySelector('input[name=recipientIds]:checked')) {
            e.preventDefault();
            alert('Vui lòng chọn ít nhất một người nhận.');
            return;
        }
        if (!confirm('Xác nhận gửi báo cáo tới các địa chỉ đã chọn? Báo cáo đã gửi không thể thu hồi.')) {
            e.preventDefault();
            return;
        }
        var button = document.getElementById('sendButton'); // chống bấm gửi hai lần
        setTimeout(function () { button.disabled = true; button.textContent = 'Đang gửi...'; }, 0);
    });
</script>
<script src="${ctx}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
