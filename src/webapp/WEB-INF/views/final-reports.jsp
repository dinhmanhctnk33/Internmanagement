<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Báo cáo cuối kỳ - IMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261012-3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/attendance-report.css?v=20261010-1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css?v=20261010-2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/final-report.css?v=20261021-1">
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
                    <div class="fr-builder-grid">
                    <div class="fr-field-group"><span class="fr-label">Loại báo cáo</span>
                        <div class="fr-option-grid">
                            <label class="fr-option-card"><input type="radio" name="reportType" value="LEADER" ${selectedType == 'LEADER' ? 'checked' : ''} required><span><i class="fa-solid fa-chart-pie"></i><b>Tổng hợp cho lãnh đạo</b><small>Chỉ số tổng quan, không hiển thị thông tin cá nhân.</small></span></label>
                            <label class="fr-option-card"><input type="radio" name="reportType" value="SCHOOL" ${selectedType == 'SCHOOL' ? 'checked' : ''}><span><i class="fa-solid fa-school"></i><b>Chi tiết cho trường</b><small>Thông tin thực tập sinh, mỗi trường một tệp riêng.</small></span></label>
                            <label class="fr-option-card"><input type="radio" name="reportType" value="BOTH" ${selectedType == 'BOTH' ? 'checked' : ''}><span><i class="fa-solid fa-layer-group"></i><b>Cả hai loại</b><small>Tạo đồng thời báo cáo lãnh đạo và báo cáo trường.</small></span></label>
                        </div>
                    </div>
                    <div class="fr-field-group"><span class="fr-label">Chương trình thực tập</span>
                        <div class="fr-programs">
                            <c:forEach items="${programs}" var="p">
                                <label class="fr-program-card"><input type="checkbox" name="programIds" value="${p.id}" ${selectedProgramIds.contains(p.id) ? 'checked' : ''}><span><b><c:out value="${p.programName}"/></b><small><c:out value="${p.programCode}"/></small></span></label>
                            </c:forEach>
                            <c:if test="${empty programs}"><p class="fr-hint">Chưa có chương trình thực tập nào.</p></c:if>
                        </div>
                    </div>
                    </div>
                    <div class="fr-actions fr-actions-end"><button class="fr-btn" type="submit"><i class="fa-solid fa-eye"></i> Xem trước báo cáo</button></div>
                </form>
            </section>

            <%-- 2. Xem trước và xác nhận --%>
            <c:if test="${not empty previews}">
                <section class="report-card fr-card">
                    <div class="card-heading"><div><h2>2. Xem trước</h2><p>Kiểm tra kỹ rồi mới xác nhận. Chưa có gì được lưu cho tới khi bạn bấm "Xác nhận tạo".</p></div></div>
                    <c:forEach items="${previews}" var="pv">
                        <div class="fr-preview">
                            <div class="fr-preview-header"><div class="fr-preview-icon"><i class="fa-solid ${pv.reportType == 'LEADER' ? 'fa-chart-pie' : 'fa-school'}"></i></div><div><span class="fr-preview-type">${pv.reportType == 'LEADER' ? 'BÁO CÁO LÃNH ĐẠO' : 'BÁO CÁO NHÀ TRƯỜNG'}</span><h3>${pv.reportType == 'LEADER' ? 'Báo cáo tổng hợp cuối kỳ' : 'Báo cáo chi tiết cuối kỳ'}</h3><p><c:out value="${pv.programNames}"/></p></div></div>
                            <div class="fr-preview-stats"><div><strong>${pv.internCount}</strong><span>Thực tập sinh</span></div><div class="is-complete"><strong>${pv.finalizedCount}</strong><span>Đã chốt đánh giá</span></div><div class="is-pending"><strong>${pv.internCount - pv.finalizedCount}</strong><span>Chưa đánh giá</span></div></div>

                            <c:forEach items="${pv.emptyPrograms}" var="e"><div class="fr-warn"><i class="fa-solid fa-triangle-exclamation"></i> <c:out value="${e}"/>: báo cáo sẽ hiển thị "chưa có dữ liệu".</div></c:forEach>
                            <c:if test="${not empty pv.notFinalized}">
                                <div class="fr-warn fr-incomplete"><div class="fr-warn-title"><i class="fa-solid fa-circle-info"></i><span><b>${pv.notFinalized.size()} thực tập sinh</b> chưa có đánh giá đã chốt và sẽ không được tính vào điểm trung bình.</span></div>
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

                            <div class="fr-content-preview">
                                <div class="fr-content-heading">
                                    <div><span>NỘI DUNG SẼ ĐƯỢC XUẤT</span><h4>${pv.reportType == 'LEADER' ? 'Số liệu báo cáo lãnh đạo' : 'Chi tiết báo cáo gửi trường'}</h4></div>
                                    <span class="fr-preview-note"><i class="fa-solid fa-eye"></i> Bản xem trước trực tiếp</span>
                                </div>

                                <c:if test="${pv.reportType == 'LEADER'}">
                                    <c:set var="ranges" value="${['1,0–1,9', '2,0–2,9', '3,0–3,9', '4,0–5,0']}"/>
                                    <c:forEach items="${pv.content.programs}" var="p">
                                        <section class="fr-content-section">
                                            <div class="fr-content-title"><div><h5><c:out value="${p.programName}"/></h5><p><fmt:formatNumber value="${p.finalizedCount}" pattern="0"/>/<fmt:formatNumber value="${p.internCount}" pattern="0"/> thực tập sinh có đánh giá đã chốt</p></div><span><fmt:formatNumber value="${p.internCount}" pattern="0"/> TTS</span></div>
                                            <div class="fr-score-grid">
                                                <div><span>TB kỹ năng</span><strong>${empty p.avgTechnical ? '—' : p.avgTechnical}</strong></div>
                                                <div><span>TB thái độ</span><strong>${empty p.avgAttitude ? '—' : p.avgAttitude}</strong></div>
                                                <div><span>TB chung</span><strong>${empty p.avgFinal ? '—' : p.avgFinal}</strong></div>
                                                <div><span>Chuyên cần</span><strong>${empty p.avgAttendanceRate ? '—' : p.avgAttendanceRate}${empty p.avgAttendanceRate ? '' : '%'}</strong></div>
                                                <div><span>Báo cáo tuần</span><strong><fmt:formatNumber value="${p.weeklySubmitted}" pattern="0"/></strong></div>
                                            </div>
                                            <c:choose>
                                                <c:when test="${empty p.distribution}"><p class="fr-no-data"><i class="fa-regular fa-circle-info"></i> Chưa có đánh giá đã chốt để tính điểm và phân bố.</p></c:when>
                                                <c:otherwise><div class="fr-distribution"><c:forEach items="${ranges}" var="range" varStatus="st"><div><span>${range}</span><b><fmt:formatNumber value="${p.distribution[st.index]}" pattern="0"/></b></div></c:forEach></div></c:otherwise>
                                            </c:choose>
                                            <c:if test="${not empty p.departments}">
                                                <div class="report-table-wrap fr-content-table"><table><thead><tr><th>PHÒNG BAN</th><th>SỐ TTS</th><th>ĐÃ CHỐT</th><th>TB KỸ NĂNG</th><th>TB THÁI ĐỘ</th><th>TB CHUNG</th></tr></thead><tbody>
                                                <c:forEach items="${p.departments}" var="d"><tr><td><strong><c:out value="${d.departmentName}"/></strong></td><td><fmt:formatNumber value="${d.internCount}" pattern="0"/></td><td><fmt:formatNumber value="${d.finalizedCount}" pattern="0"/></td><td>${empty d.avgTechnical ? '—' : d.avgTechnical}</td><td>${empty d.avgAttitude ? '—' : d.avgAttitude}</td><td>${empty d.avgFinal ? '—' : d.avgFinal}</td></tr></c:forEach>
                                                </tbody></table></div>
                                            </c:if>
                                        </section>
                                    </c:forEach>
                                </c:if>

                                <c:if test="${pv.reportType == 'SCHOOL'}">
                                    <c:forEach items="${pv.content}" var="school">
                                        <section class="fr-content-section">
                                            <div class="fr-content-title"><div><h5><c:out value="${school.schoolName}"/></h5><p>${school.interns.size()} thực tập sinh trong tệp báo cáo này</p></div><span>${school.interns.size()} TTS</span></div>
                                            <div class="report-table-wrap fr-content-table"><table><thead><tr><th>HỌ TÊN</th><th>CHƯƠNG TRÌNH / PHÒNG BAN</th><th>KỲ THỰC TẬP</th><th>ĐI LÀM</th><th>KỸ NĂNG</th><th>THÁI ĐỘ</th><th>ĐIỂM CHUNG</th><th>NHẬN XÉT</th></tr></thead><tbody>
                                            <c:forEach items="${school.interns}" var="intern"><tr>
                                                <td><strong><c:out value="${intern.fullName}"/></strong></td>
                                                <td><c:out value="${intern.programName}"/><small><c:out value="${intern.departmentName}"/></small></td>
                                                <td>${empty intern.startDate ? '—' : intern.startDate}<br>${empty intern.endDate ? '—' : intern.endDate}</td>
                                                <td><fmt:formatNumber value="${intern.attendedDays}" pattern="0"/>/<c:choose><c:when test="${empty intern.expectedDays}">—</c:when><c:otherwise><fmt:formatNumber value="${intern.expectedDays}" pattern="0"/></c:otherwise></c:choose></td>
                                                <c:choose><c:when test="${intern.hasEvaluation}"><td>${intern.technicalScore}</td><td>${intern.attitudeScore}</td><td><strong>${intern.finalScore}</strong></td><td><c:out value="${intern.comments}" default="—"/></td></c:when><c:otherwise><td colspan="4" class="fr-muted-cell">Chưa có đánh giá đã chốt</td></c:otherwise></c:choose>
                                            </tr></c:forEach>
                                            </tbody></table></div>
                                        </section>
                                    </c:forEach>
                                    <c:if test="${empty pv.content}"><p class="fr-no-data">Chưa có thực tập sinh để xem trước nội dung.</p></c:if>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>

                    <form method="post" action="${pageContext.request.contextPath}/final-reports" class="fr-actions fr-confirm-actions">
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
