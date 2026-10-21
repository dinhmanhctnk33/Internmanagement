<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Danh bạ người nhận - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/final-report.css?v=20261014-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<c:set var="activeMenu" value="recipients"/>
<div class="layout">
    <%@ include file="includes/final-report-sidebar.jspf" %>
    <main class="main">
        <header class="header intern-header"><div class="report-context header-context"><i class="fa-solid fa-address-book"></i><span>Danh bạ người nhận báo cáo</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content attendance-page">
            <div class="report-heading"><div><p class="eyebrow">Báo cáo cuối kỳ</p><h1>Danh bạ người nhận</h1><p>Chỉ gửi trực tiếp được tới các địa chỉ có trong danh bạ này.</p></div></div>

            <c:if test="${param.success == 'created'}"><div class="fr-success"><i class="fa-solid fa-circle-check"></i> Đã thêm người nhận.</div></c:if>
            <c:if test="${param.success == 'updated'}"><div class="fr-success"><i class="fa-solid fa-circle-check"></i> Đã cập nhật người nhận.</div></c:if>
            <c:if test="${param.success == 'deleted'}"><div class="fr-success"><i class="fa-solid fa-circle-check"></i> Đã xóa người nhận. Lịch sử gửi vẫn được giữ nguyên.</div></c:if>
            <c:if test="${not empty param.error}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>

            <section class="report-card fr-card">
                <div class="card-heading"><div><h2>${empty editing ? 'Thêm người nhận' : 'Sửa người nhận'}</h2><p>Trường được chọn từ tên trường trong hồ sơ thực tập sinh.</p></div></div>
                <form method="post" action="${pageContext.request.contextPath}/report-recipients" class="fr-form fr-recipient-form">
                    <input type="hidden" name="action" value="${empty editing ? 'create' : 'update'}">
                    <c:if test="${not empty editing}"><input type="hidden" name="id" value="${editing.id}"></c:if>
                    <label><span>Loại</span>
                        <select name="recipientType" id="recipientType">
                            <option value="SCHOOL" ${editing.recipientType == 'LEADER' ? '' : 'selected'}>Trường</option>
                            <option value="LEADER" ${editing.recipientType == 'LEADER' ? 'selected' : ''}>Ban lãnh đạo</option>
                        </select></label>
                    <label id="orgField"><span>Trường</span>
                        <select name="organizationName" id="organizationName">
                            <option value="">-- Chọn trường --</option>
                            <c:forEach items="${schoolNames}" var="n"><option value="<c:out value='${n}'/>" ${editing.organizationName == n ? 'selected' : ''}><c:out value="${n}"/></option></c:forEach>
                        </select></label>
                    <label><span>Họ tên liên hệ</span><input name="contactName" maxlength="100" required value="<c:out value='${editing.contactName}'/>"></label>
                    <label><span>Email</span><input type="email" name="email" maxlength="150" required value="<c:out value='${editing.email}'/>"></label>
                    <div class="fr-actions fr-form-actions">
                        <button class="fr-btn" type="submit"><i class="fa-solid fa-floppy-disk"></i> ${empty editing ? 'Thêm' : 'Lưu thay đổi'}</button>
                        <c:if test="${not empty editing}"><a class="fr-btn fr-btn-secondary" href="${pageContext.request.contextPath}/report-recipients">Hủy</a></c:if>
                    </div>
                </form>
                <c:if test="${empty schoolNames}"><p class="fr-hint">Chưa có tên trường nào trong hồ sơ thực tập sinh nên chưa thêm được người nhận loại Trường.</p></c:if>
            </section>

            <form method="get" class="report-filter fr-search fr-filter-bar">
                <label><span>Loại</span><select name="type"><option value="">Tất cả</option><option value="SCHOOL" ${selectedType == 'SCHOOL' ? 'selected' : ''}>Trường</option><option value="LEADER" ${selectedType == 'LEADER' ? 'selected' : ''}>Ban lãnh đạo</option></select></label>
                <label class="search-field"><span>Tìm kiếm</span><div><i class="fa-solid fa-magnifying-glass"></i><input name="q" value="<c:out value='${keyword}'/>" placeholder="Tên trường, họ tên hoặc email"></div></label>
                <button type="submit"><i class="fa-solid fa-filter"></i> Lọc</button>
                <a class="fr-reset-filter" href="${pageContext.request.contextPath}/report-recipients" title="Xóa bộ lọc"><i class="fa-solid fa-rotate-left"></i><span>Đặt lại</span></a>
            </form>

            <section class="report-card">
                <div class="card-heading"><div><h2>Danh sách người nhận</h2></div><span>${recipients.size()} người nhận</span></div>
                <div class="report-table-wrap"><table>
                    <thead><tr><th>LOẠI</th><th>ĐƠN VỊ</th><th>HỌ TÊN LIÊN HỆ</th><th>EMAIL</th><th>THAO TÁC</th></tr></thead>
                    <tbody>
                    <c:forEach items="${recipients}" var="r">
                        <tr>
                            <td><span class="fr-badge ${r.recipientType == 'LEADER' ? 'warn' : 'ok'}">${r.recipientType == 'LEADER' ? 'Lãnh đạo' : 'Trường'}</span></td>
                            <td><c:out value="${r.organizationName}"/></td>
                            <td><strong><c:out value="${r.contactName}"/></strong></td>
                            <td><c:out value="${r.email}"/></td>
                            <td class="fr-row-actions">
                                <a class="fr-btn fr-btn-small fr-btn-secondary" href="${pageContext.request.contextPath}/report-recipients?edit=${r.id}"><i class="fa-solid fa-pen"></i> Sửa</a>
                                <form method="post" action="${pageContext.request.contextPath}/report-recipients" onsubmit="return confirm('Xóa người nhận này? Lịch sử gửi vẫn được giữ nguyên.');">
                                    <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${r.id}">
                                    <button class="fr-btn fr-btn-small fr-btn-danger" type="submit"><i class="fa-solid fa-trash"></i> Xóa</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recipients}"><tr><td colspan="5" class="empty-row"><i class="fa-regular fa-folder-open"></i><br>Chưa có người nhận nào.</td></tr></c:if>
                    </tbody>
                </table></div>
            </section>
        </section>
    </main>
</div>
<script>
    (function () {
        var type = document.getElementById('recipientType');
        var field = document.getElementById('orgField');
        var select = document.getElementById('organizationName');
        function toggle() {
            var leader = type.value === 'LEADER';
            field.style.display = leader ? 'none' : '';
            select.disabled = leader;
        }
        type.addEventListener('change', toggle);
        toggle();
    })();
</script>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>
