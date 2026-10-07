<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Xác nhận import - IMS</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css">

</head>

<body>

<main class="main" style="max-width:1000px;margin:40px auto;padding:24px;">

    <section class="content">

        <div class="page-title">

            <div><h1>Xác nhận import thực tập sinh</h1><p>Kiểm tra số dòng hợp lệ và lý do các dòng bị bỏ qua trước khi lưu.</p></div>

        </div>

        <p>Hồ sơ hợp lệ: <strong>${pendingImport.profiles.size()}</strong> · Bỏ qua: <strong>${pendingImport.errors.size()}</strong></p>

        <c:if test="${not empty pendingImport.errors}">

            <h2>Lý do bỏ qua theo dòng</h2>

            <ul><c:forEach var="detail" items="${pendingImport.errors}"><li><c:out value="${detail}"/></li></c:forEach></ul>

        </c:if>

        <c:if test="${not empty pendingImport.profiles}">

            <h2>Dòng sẽ được lưu</h2>

            <div style="overflow-x:auto;"><table><thead><tr><th>Mã TTS</th><th>Họ tên</th><th>Email</th><th>Trạng thái</th></tr></thead><tbody>

                <c:forEach var="profile" items="${pendingImport.profiles}">

                    <tr><td><c:out value="${profile.internCode}"/></td><td><c:out value="${profile.fullName}"/></td><td><c:out value="${profile.email}"/></td><td><c:out value="${profile.internshipStatus}"/></td></tr>

                </c:forEach>

            </tbody></table></div>

        </c:if>

        <div style="display:flex;gap:12px;margin-top:24px;">

            <a class="secondary-btn" href="${pageContext.request.contextPath}/interns">Hủy</a>

            <c:if test="${not empty pendingImport.profiles}">

                <form method="post" action="${pageContext.request.contextPath}/interns/import">

                    <input type="hidden" name="action" value="confirm">

                    <button type="submit" class="primary-btn">Lưu ${pendingImport.profiles.size()} hồ sơ hợp lệ</button>

                </form>

            </c:if>

        </div>

    </section>

</main>

</body>

</html>