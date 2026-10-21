<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác nhận nhập hồ sơ - IMS Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css">
    <style>
        body{background:#f5f8fc;color:#153b68}.import-wrap{max-width:1180px;margin:36px auto;padding:0 20px}.import-card{background:#fff;border:1px solid #d9e4f1;border-radius:16px;box-shadow:0 12px 34px rgba(21,59,104,.08);overflow:hidden}.import-head,.import-actions{padding:24px 28px}.import-head{border-bottom:1px solid #e4ebf4}.import-head h1{margin:0 0 7px;font-size:26px}.import-head p{margin:0;color:#6b7f99}.table-scroll{overflow:auto}.import-table{width:100%;border-collapse:collapse;min-width:920px}.import-table th,.import-table td{padding:13px 16px;border-bottom:1px solid #e8eef6;text-align:left}.import-table th{font-size:12px;text-transform:uppercase;background:#eef4fb}.error-box{margin:20px 28px;padding:16px 18px;border-radius:10px;background:#fff5e6;color:#945800}.error-box ul{margin:8px 0 0;padding-left:20px}.import-actions{display:flex;justify-content:flex-end;gap:12px}.btn{border:0;border-radius:9px;padding:11px 18px;font-weight:700;text-decoration:none;cursor:pointer}.btn.secondary{background:#edf2f7;color:#365775}.btn.primary{background:#356aa5;color:#fff}
    </style>
</head>
<body>
<main class="import-wrap">
    <section class="import-card">
        <header class="import-head">
            <h1>Xác nhận dữ liệu nhập từ Excel</h1>
            <p>Kiểm tra các hồ sơ hợp lệ trước khi ghi vào hệ thống.</p>
        </header>
        <c:if test="${not empty pendingImport.errors}">
            <div class="error-box"><strong>${pendingImport.errors.size()} dòng sẽ bị bỏ qua</strong><ul><c:forEach items="${pendingImport.errors}" var="error"><li><c:out value="${error}"/></li></c:forEach></ul></div>
        </c:if>
        <div class="table-scroll">
            <table class="import-table">
                <thead><tr><th>Mã TTS</th><th>Họ tên</th><th>Email</th><th>Điện thoại</th><th>Trường</th><th>Chuyên ngành</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody><c:forEach items="${pendingImport.profiles}" var="profile"><tr>
                    <td><c:out value="${profile.internCode}"/></td><td><c:out value="${profile.fullName}"/></td><td><c:out value="${profile.email}"/></td>
                    <td><c:out value="${profile.phoneNumber}"/></td><td><c:out value="${profile.universityName}"/></td><td><c:out value="${profile.majorName}"/></td>
                    <td><c:out value="${profile.internshipStatus}"/></td><td>${profile.id == 0 ? 'Thêm mới' : 'Cập nhật'}</td>
                </tr></c:forEach></tbody>
            </table>
        </div>
        <footer class="import-actions">
            <a class="btn secondary" href="${pageContext.request.contextPath}/interns">Hủy</a>
            <form method="post" action="${pageContext.request.contextPath}/interns/import">
                <input type="hidden" name="action" value="confirm">
                <button class="btn primary" type="submit">Xác nhận nhập ${pendingImport.profiles.size()} hồ sơ</button>
            </form>
        </footer>
    </section>
</main>
</body>
</html>
