<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Bản chụp báo cáo #${snapshot.id} - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/final-report.css?v=20261013-1">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<c:set var="activeMenu" value="reports"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="layout">
    <%@ include file="includes/final-report-sidebar.jspf" %>
    <main class="main">
        <header class="header intern-header"><div class="report-context header-context"><i class="fa-solid fa-file-lines"></i><span>Báo cáo cuối kỳ</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
        <section class="content attendance-page">
            <div class="report-heading">
                <div><p class="eyebrow">Bản chụp #${snapshot.id}</p>
                    <h1>${snapshot.leaderReport ? 'Báo cáo tổng hợp cho lãnh đạo' : 'Báo cáo chi tiết cho trường'}</h1>
                    <p>Chương trình: <c:out value="${snapshot.programNames}"/> · Tạo lúc ${snapshot.createdAt} bởi <c:out value="${snapshot.createdByName}"/></p></div>
                <a class="fr-btn fr-btn-secondary" href="${ctx}/final-reports"><i class="fa-solid fa-arrow-left"></i> Danh sách</a>
            </div>

            <c:if test="${param.success == 'created'}"><div class="fr-success"><i class="fa-solid fa-circle-check"></i> Đã tạo bản chụp báo cáo.</div></c:if>
            <c:if test="${not empty param.sent}">
                <div class="${param.failed > 0 ? 'report-alert' : 'fr-success'}"><i class="fa-solid fa-paper-plane"></i> Đã gửi thành công <c:out value="${param.sent}"/> địa chỉ<c:if test="${param.failed > 0}">, <c:out value="${param.failed}"/> địa chỉ gửi lỗi (xem lịch sử gửi bên dưới)</c:if>.</div>
            </c:if>
            <c:if test="${not empty param.error}"><div class="report-alert"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${param.error}"/></div></c:if>

            <div class="fr-actions">
                <c:if test="${snapshot.leaderReport}">
                    <a class="fr-btn fr-btn-secondary" href="${ctx}/final-reports/download?id=${snapshot.id}&format=pdf"><i class="fa-solid fa-file-pdf"></i> Tải PDF</a>
                    <a class="fr-btn fr-btn-secondary" href="${ctx}/final-reports/download?id=${snapshot.id}&format=xlsx"><i class="fa-solid fa-file-excel"></i> Tải Excel</a>
                </c:if>
                <a class="fr-btn" href="${ctx}/final-reports/send?id=${snapshot.id}"><i class="fa-solid fa-paper-plane"></i> Gửi trực tiếp</a>
            </div>

            <%-- ================= BÁO CÁO CHO LÃNH ĐẠO ================= --%>
            <c:if test="${snapshot.leaderReport}">
                <c:set var="ranges" value="${['1,0 đến 1,9', '2,0 đến 2,9', '3,0 đến 3,9', '4,0 đến 5,0']}"/>
                <c:forEach items="${payload.leader.programs}" var="p">
                    <c:set var="noScore" value="${p.finalizedCount == 0 ? 'chưa có dữ liệu' : 'ẩn vì nhóm quá nhỏ'}"/>
                    <section class="report-card fr-card">
                        <div class="card-heading"><div><h2><c:out value="${p.programName}"/></h2></div><span>${p.internCount} thực tập sinh</span></div>
                        <table class="fr-kv"><tbody>
                            <tr><th>Số thực tập sinh</th><td>${p.internCount}</td></tr>
                            <tr><th>Số có đánh giá đã chốt</th><td>${p.finalizedCount}</td></tr>
                            <tr><th>Số chưa có đánh giá đã chốt</th><td>${p.notFinalizedCount}</td></tr>
                            <tr><th>Điểm trung bình nhóm kỹ năng</th><td>${empty p.avgTechnical ? noScore : p.avgTechnical}</td></tr>
                            <tr><th>Điểm trung bình nhóm thái độ</th><td>${empty p.avgAttitude ? noScore : p.avgAttitude}</td></tr>
                            <tr><th>Điểm trung bình chung</th><td>${empty p.avgFinal ? noScore : p.avgFinal}</td></tr>
                            <c:forEach items="${ranges}" var="rg" varStatus="st">
                                <tr><th>Phân bố điểm chung: ${rg} (số thực tập sinh)</th><td>${empty p.distribution ? noScore : p.distribution[st.index]}</td></tr>
                            </c:forEach>
                            <tr><th>Tỷ lệ chuyên cần trung bình</th><td>
                                <c:choose>
                                    <c:when test="${not empty p.avgAttendanceRate}">${p.avgAttendanceRate}%</c:when>
                                    <c:when test="${p.internCount > 0 && p.internCount < 3}">ẩn vì nhóm quá nhỏ</c:when>
                                    <c:otherwise>chưa có dữ liệu</c:otherwise>
                                </c:choose></td></tr>
                            <tr><th>Báo cáo tuần đã nộp</th><td>${p.weeklySubmitted}</td></tr>
                            <tr><th>Báo cáo tuần nộp muộn</th><td>${empty p.weeklyLate ? 'chưa có dữ liệu' : p.weeklyLate}</td></tr>
                        </tbody></table>
                        <c:if test="${not empty p.departments}">
                            <div class="report-table-wrap"><table>
                                <thead><tr><th>PHÒNG BAN</th><th>SỐ TTS</th><th>ĐÃ CHỐT</th><th>TB KỸ NĂNG</th><th>TB THÁI ĐỘ</th><th>TB CHUNG</th></tr></thead>
                                <tbody>
                                <c:forEach items="${p.departments}" var="d">
                                    <c:set var="dNo" value="${d.finalizedCount == 0 ? 'chưa có dữ liệu' : 'ẩn vì nhóm quá nhỏ'}"/>
                                    <tr><td><c:out value="${d.departmentName}"/></td><td>${d.internCount}</td><td>${d.finalizedCount}</td>
                                        <td>${empty d.avgTechnical ? dNo : d.avgTechnical}</td><td>${empty d.avgAttitude ? dNo : d.avgAttitude}</td><td>${empty d.avgFinal ? dNo : d.avgFinal}</td></tr>
                                </c:forEach>
                                </tbody>
                            </table></div>
                        </c:if>
                    </section>
                </c:forEach>
            </c:if>

            <%-- ================= BÁO CÁO CHO TRƯỜNG ================= --%>
            <c:if test="${!snapshot.leaderReport}">
                <c:forEach items="${payload.schools}" var="s">
                    <c:url var="pdfUrl" value="/final-reports/download"><c:param name="id" value="${snapshot.id}"/><c:param name="format" value="pdf"/><c:param name="school" value="${s.schoolName}"/></c:url>
                    <c:url var="xlsxUrl" value="/final-reports/download"><c:param name="id" value="${snapshot.id}"/><c:param name="format" value="xlsx"/><c:param name="school" value="${s.schoolName}"/></c:url>
                    <section class="report-card fr-card">
                        <div class="card-heading"><div><h2><c:out value="${s.schoolName}"/></h2><p>${s.interns.size()} thực tập sinh</p></div>
                            <div class="fr-row-actions"><a class="fr-btn fr-btn-small fr-btn-secondary" href="${pdfUrl}"><i class="fa-solid fa-file-pdf"></i> PDF</a><a class="fr-btn fr-btn-small fr-btn-secondary" href="${xlsxUrl}"><i class="fa-solid fa-file-excel"></i> Excel</a></div></div>
                        <div class="report-table-wrap"><table>
                            <thead><tr><th>HỌ TÊN</th><th>CHƯƠNG TRÌNH / PHÒNG BAN</th><th>KỲ THỰC TẬP</th><th>NGÀY ĐI LÀM</th><th>KỸ NĂNG</th><th>THÁI ĐỘ</th><th>CHUYÊN CẦN</th><th>CHUNG</th><th>NHẬN XÉT CHUNG</th></tr></thead>
                            <tbody>
                            <c:forEach items="${s.interns}" var="i">
                                <tr>
                                    <td><strong><c:out value="${i.fullName}"/></strong></td>
                                    <td><c:out value="${i.programName}"/><small><c:out value="${i.departmentName}"/></small></td>
                                    <td>${empty i.startDate ? '-' : i.startDate} - ${empty i.endDate ? '-' : i.endDate}</td>
                                    <td>${i.attendedDays}/${empty i.expectedDays ? 'chưa có dữ liệu' : i.expectedDays}</td>
                                    <c:choose>
                                        <c:when test="${i.hasEvaluation}">
                                            <td>${i.technicalScore}</td><td>${i.attitudeScore}</td><td>${i.punctualityScore}</td><td><b>${i.finalScore}</b></td>
                                            <td><c:out value="${i.comments}" default="-"/></td>
                                        </c:when>
                                        <c:otherwise><td colspan="5" class="fr-muted-cell">Chưa có đánh giá</td></c:otherwise>
                                    </c:choose>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table></div>
                    </section>
                </c:forEach>
            </c:if>

            <%-- ================= LỊCH SỬ GỬI ================= --%>
            <section class="report-card">
                <div class="card-heading"><div><h2>Lịch sử gửi</h2><p>Ghi từng địa chỉ: thành công hoặc lỗi.</p></div><span>${sendLogs.size()} lượt</span></div>
                <div class="report-table-wrap"><table>
                    <thead><tr><th>THỜI ĐIỂM</th><th>TRƯỜNG</th><th>ĐỊA CHỈ</th><th>TRẠNG THÁI</th><th>SỐ LẦN THỬ</th><th>NGƯỜI GỬI</th><th>GHI CHÚ</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${sendLogs}" var="l">
                        <tr>
                            <td>${empty l.sentAt ? l.createdAt : l.sentAt}</td>
                            <td><c:out value="${l.schoolName}" default="Ban lãnh đạo"/></td>
                            <td><c:out value="${l.recipientEmail}"/></td>
                            <td><span class="fr-badge ${l.status == 'SENT' ? 'ok' : l.status == 'FAILED' ? 'err' : 'muted'}">${l.status == 'SENT' ? 'Đã gửi' : l.status == 'FAILED' ? 'Gửi lỗi' : 'Đang gửi'}</span></td>
                            <td>${l.attempts}</td>
                            <td><c:out value="${l.sentByName}"/></td>
                            <td><c:out value="${l.errorMessage}"/></td>
                            <td>
                                <c:if test="${l.status == 'FAILED'}">
                                    <form method="post" action="${ctx}/final-reports/send">
                                        <input type="hidden" name="action" value="resend"><input type="hidden" name="id" value="${snapshot.id}"><input type="hidden" name="logId" value="${l.id}">
                                        <button class="fr-btn fr-btn-small" type="submit"><i class="fa-solid fa-rotate-right"></i> Gửi lại</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty sendLogs}"><tr><td colspan="8" class="empty-row">Bản chụp này chưa được gửi.</td></tr></c:if>
                    </tbody>
                </table></div>
            </section>
        </section>
    </main>
</div>
<script src="${ctx}/frontend/js/intern-header.js?v=20261010-2"></script>
</body>
</html>