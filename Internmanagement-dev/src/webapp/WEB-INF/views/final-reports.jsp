<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Báo cáo cuối kỳ - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/final-report.css?v=20261013-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<c:set var="activeMenu" value="reports"/>
<div class="layout">
    <%@ include file="includes/final-report-sidebar.jspf" %>
    <main class="main">
        <header class="header intern-header"><div class="report-context header-context"><i class="fa-solid fa-file-lines"></i><span>Báo cáo cuối kỳ</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content attendance-page">
            <div class="report-heading"><div><p class="eyebrow">Báo cáo nhân sự</p><h1>Báo cáo cuối kỳ</h1><p>Tổng hợp đánh giá đã chốt thành báo cáo gửi trường hoặc ban lãnh đạo.</p></div></div>

            <c:if test="${param.success == 'created'}"><div class="fr-success"><i class="fa-solid fa-circle-check"></i> Đã tạo bản chụp báo cáo.</div></c:if>
            <c:if test="${not empty param.error}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>
            <c:if test="${not empty errorMessage}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${errorMessage}"/></div></c:if>

            <%-- 1. Chọn loại báo cáo và chương trình --%>
            <section class="report-card fr-card">
                <div class="card-heading"><div><h2>1. Chọn loại báo cáo và chương trình</h2><p>Chỉ thực tập sinh có đánh giá "Đã chốt" được đưa vào nội dung báo cáo.</p></div></div>
                <form method="get" action="${pageContext.request.contextPath}/final-reports" class="fr-form fr-form-wide">
                    <input type="hidden" name="action" value="preview">
                    <div class="fr-field-group"><span class="fr-label">Loại báo cáo</span>
                        <label class="fr-choice"><input type="radio" name="reportType" value="LEADER" ${selectedType == 'LEADER' ? 'checked' : ''} required> Tổng hợp cho lãnh đạo (chỉ thống kê, không có thông tin cá nhân)</label>
                        <label class="fr-choice"><input type="radio" name="reportType" value="SCHOOL" ${selectedType == 'SCHOOL' ? 'checked' : ''}> Chi tiết cho trường (mỗi trường một file)</label>
                        <label class="fr-choice"><input type="radio" name="reportType" value="BOTH" ${selectedType == 'BOTH' ? 'checked' : ''}> Cả hai loại</label>
                    </div>
                    <div class="fr-field-group"><span class="fr-label">Chương trình thực tập</span>
                        <div class="fr-programs">
                            <c:forEach items="${programs}" var="p">
                                <label class="fr-choice"><input type="checkbox" name="programIds" value="${p.id}" ${selectedProgramIds.contains(p.id) ? 'checked' : ''}> <c:out value="${p.programName}"/> <small>(<c:out value="${p.programCode}"/>)</small></label>
                            </c:forEach>
                            <c:if test="${empty programs}"><p class="fr-hint">Chưa có chương trình thực tập nào.</p></c:if>
                        </div>
                    </div>
                    <div class="fr-actions"><button class="fr-btn" type="submit"><i class="fa-solid fa-eye"></i> Xem trước</button></div>
                </form>
            </section>

            <%-- 2. Xem trước và xác nhận --%>
            <c:if test="${not empty previews}">
                <section class="report-card fr-card">
                    <div class="card-heading"><div><h2>2. Xem trước</h2><p>Kiểm tra kỹ rồi mới xác nhận. Chưa có gì được lưu cho tới khi bạn bấm "Xác nhận tạo".</p></div></div>
                    <c:forEach items="${previews}" var="pv">
                        <div class="fr-preview">
                            <h3>${pv.reportType == 'LEADER' ? 'Báo cáo tổng hợp cho lãnh đạo' : 'Báo cáo chi tiết cho trường'}</h3>
                            <p>Chương trình: <b><c:out value="${pv.programNames}"/></b></p>
                            <p><b>${pv.internCount}</b> thực tập sinh, trong đó <b>${pv.finalizedCount}</b> có đánh giá đã chốt.</p>

                            <c:forEach items="${pv.emptyPrograms}" var="e"><div class="fr-warn"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${e}"/>: báo cáo sẽ hiển thị "chưa có dữ liệu".</div></c:forEach>
                            <c:if test="${not empty pv.notFinalized}">
                                <div class="fr-warn"><i class="fa-solid fa-triangle-exclamation"></i> ${pv.notFinalized.size()} thực tập sinh chưa có đánh giá đã chốt (không bị tính điểm 0):
                                    <ul><c:forEach items="${pv.notFinalized}" var="n"><li><c:out value="${n}"/></li></c:forEach></ul>
                                </div>
                            </c:if>

                            <c:if test="${pv.reportType == 'SCHOOL'}">
                                <div class="report-table-wrap"><table>
                                    <thead><tr><th>TRƯỜNG</th><th>SỐ THỰC TẬP SINH</th><th>NGƯỜI NHẬN TRONG DANH BẠ</th></tr></thead>
                                    <tbody>
                                    <c:forEach items="${pv.schools}" var="s">
                                        <tr>
                                            <td><strong><c:out value="${s.schoolName}"/></strong></td>
                                            <td>${s.internCount}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${s.noSchoolInfo}"><span class="fr-badge err">Chưa có thông tin trường: chỉ xuất file</span></c:when>
                                                    <c:when test="${s.inDirectory}"><span class="fr-badge ok">Có ${s.emailCount} email</span></c:when>
                                                    <c:otherwise><span class="fr-badge err">Chưa có người nhận: chỉ xuất file</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty pv.schools}"><tr><td colspan="3" class="empty-row">Chưa có thực tập sinh nào trong các chương trình đã chọn.</td></tr></c:if>
                                    </tbody>
                                </table></div>
                                <p class="fr-hint">Tên trường viết khác nhau (viết tắt, sai chính tả) được coi là các trường khác nhau. Nếu thấy hai dòng cùng một trường, hãy sửa lại hồ sơ trước khi gửi.</p>
                            </c:if>
                        </div>
                    </c:forEach>

                    <form method="post" action="${pageContext.request.contextPath}/final-reports" class="fr-actions">
                        <input type="hidden" name="action" value="create">
                        <input type="hidden" name="confirmed" value="true">
                        <input type="hidden" name="reportType" value="<c:out value='${selectedType}'/>">
                        <c:forEach items="${selectedProgramIds}" var="pid"><input type="hidden" name="programIds" value="${pid}"></c:forEach>
                        <button class="fr-btn" type="submit"><i class="fa-solid fa-camera"></i> Xác nhận tạo bản chụp báo cáo</button>
                    </form>
                </section>
            </c:if>

            <%-- 3. Lịch sử --%>
            <section class="report-card">
                <div class="card-heading"><div><h2>Lịch sử báo cáo</h2><p>Bản chụp là cố định, không sửa hay xóa. Muốn dữ liệu mới thì tạo bản mới.</p></div><span>${snapshots.size()} bản chụp</span></div>
                <div class="report-table-wrap"><table>
                    <thead><tr><th>#</th><th>LOẠI</th><th>CHƯƠNG TRÌNH</th><th>TẠO LÚC</th><th>NGƯỜI TẠO</th><th>TÌNH TRẠNG GỬI</th><th>THAO TÁC</th></tr></thead>
                    <tbody>
                    <c:forEach items="${snapshots}" var="s">
                        <c:set var="sm" value="${sendSummaries[s.id]}"/>
                        <tr>
                            <td><b>${s.id}</b></td>
                            <td>${s.leaderReport ? 'Lãnh đạo' : 'Trường'}</td>
                            <td><c:out value="${s.programNames}"/>
                                <c:if test="${s.newerExists}"><br><span class="fr-badge warn">Đã có bản mới hơn</span></c:if></td>
                            <td>${s.createdAt}</td>
                            <td><c:out value="${s.createdByName}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${empty sm}"><span class="fr-badge muted">Chưa gửi</span></c:when>
                                    <c:otherwise>
                                        <span class="fr-badge ok">${sm.sent}/${sm.total} đã gửi</span>
                                        <c:if test="${sm.failed > 0}"><span class="fr-badge err">${sm.failed} gửi lỗi</span></c:if>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="fr-row-actions">
                                <a class="fr-btn fr-btn-small fr-btn-secondary" href="${pageContext.request.contextPath}/final-reports?id=${s.id}"><i class="fa-solid fa-folder-open"></i> Mở</a>
                                <c:if test="${s.leaderReport}">
                                    <a class="fr-btn fr-btn-small fr-btn-secondary" href="${pageContext.request.contextPath}/final-reports/download?id=${s.id}&format=pdf">PDF</a>
                                    <a class="fr-btn fr-btn-small fr-btn-secondary" href="${pageContext.request.contextPath}/final-reports/download?id=${s.id}&format=xlsx">Excel</a>
                                </c:if>
                                <a class="fr-btn fr-btn-small" href="${pageContext.request.contextPath}/final-reports/send?id=${s.id}"><i class="fa-solid fa-paper-plane"></i> Gửi</a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty snapshots}"><tr><td colspan="7" class="empty-row"><i class="fa-regular fa-folder-open"></i><br>Chưa có báo cáo nào được tạo.</td></tr></c:if>
                    </tbody>
                </table></div>
            </section>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>