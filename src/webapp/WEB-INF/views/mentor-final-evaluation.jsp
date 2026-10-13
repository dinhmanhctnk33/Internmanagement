<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Đánh giá tổng kết - IMS Portal</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/intern-header.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-workspace.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/mentor-final-evaluation.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
<aside class="sidebar mentor-sidebar">
 <div class="logo"><div class="logo-icon"><i class="fa-solid fa-chalkboard-user"></i></div><div><h2>IMS Portal</h2><span>Không gian Mentor</span></div></div>
 <nav class="menu"><p class="menu-title">HƯỚNG DẪN THỰC TẬP</p>
 <a class="menu-item" href="${pageContext.request.contextPath}/mentor/dashboard"><i class="fa-solid fa-chart-pie"></i><span>Tổng quan</span></a>
 <a class="menu-item" href="${pageContext.request.contextPath}/mentor/reports"><i class="fa-solid fa-file-circle-check"></i><span>Báo cáo tuần</span></a>
 <a class="menu-item active" href="${pageContext.request.contextPath}/mentor/final-evaluation"><i class="fa-solid fa-star-half-stroke"></i><span>Đánh giá tổng kết</span></a>
 <a class="menu-item" href="${pageContext.request.contextPath}/meetings"><i class="fa-solid fa-video"></i><span>Lịch họp</span></a></nav>
</aside>
<main class="main">
<header class="header intern-header"><div class="mentor-context header-context"><i class="fa-solid fa-star-half-stroke"></i><span>Đánh giá tổng kết</span></div><%@ include file="includes/hr-header-actions.jspf" %></header>
<section class="content mentor-content evaluation-content">
<div class="report-page-heading"><div><p class="eyebrow">SPRINT 3 · SCRUM-136</p><h1>Đánh giá tổng kết</h1><p>Đánh giá kỹ năng và thái độ của thực tập sinh vào cuối kỳ thực tập.</p></div><a href="${pageContext.request.contextPath}/mentor/dashboard"><i class="fa-solid fa-arrow-left"></i> Về tổng quan</a></div>

<c:if test="${not empty successMessage}"><div class="evaluation-feedback success" role="status"><i class="fa-solid fa-circle-check"></i><c:out value="${successMessage}"/></div></c:if>
<c:if test="${not empty errorMessage}"><div class="evaluation-feedback error" role="alert"><i class="fa-solid fa-triangle-exclamation"></i><c:out value="${errorMessage}"/></div></c:if>

<form class="evaluation-filters" method="get" action="${pageContext.request.contextPath}/mentor/final-evaluation">
 <label for="internId">Thực tập sinh</label>
 <select id="internId" name="internId">
  <c:choose>
   <c:when test="${empty interns}"><option value="">Chưa có thực tập sinh trong phạm vi hướng dẫn</option></c:when>
   <c:otherwise><c:forEach var="intern" items="${interns}">
    <option value="${intern.id}" ${intern.id == selectedInternId ? 'selected' : ''} ${intern.eligible ? '' : 'disabled'}>
     <c:out value="${intern.fullName}"/> · <c:out value="${intern.internCode}"/>
     <c:choose><c:when test="${intern.eligible}"> · Đủ điều kiện</c:when><c:otherwise> · Chưa đến giữa kỳ</c:otherwise></c:choose>
    </option>
   </c:forEach></c:otherwise>
  </c:choose>
 </select>
 <label for="status">Trạng thái</label>
 <select id="status" name="status">
  <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
  <option value="NOT_STARTED" ${statusFilter == 'NOT_STARTED' ? 'selected' : ''}>Chưa có</option>
  <option value="DRAFT" ${statusFilter == 'DRAFT' ? 'selected' : ''}>Nháp</option>
  <option value="FINALIZED" ${statusFilter == 'FINALIZED' ? 'selected' : ''}>Đã chốt</option>
  <option value="FINALIZED_AGAIN" ${statusFilter == 'FINALIZED_AGAIN' ? 'selected' : ''}>Đã chốt lại</option>
 </select>
 <label class="eligible-filter"><input type="checkbox" name="eligibleOnly" value="true" ${eligibleOnly ? 'checked' : ''}> Chỉ người đủ thời điểm</label>
 <button type="submit" class="filter-button"><i class="fa-solid fa-filter"></i> Lọc</button>
</form>

<c:choose>
 <c:when test="${empty selectedIntern}">
  <div class="evaluation-empty"><i class="fa-regular fa-clipboard"></i><h2>Không có thực tập sinh phù hợp</h2><p>Thử thay đổi bộ lọc hoặc kiểm tra danh sách thực tập sinh đã được phân công cho bạn.</p></div>
 </c:when>
 <c:otherwise>
  <div class="evaluation-notice ${canEvaluate ? '' : 'warning'}">
   <i class="fa-solid fa-circle-info"></i>
   <span><c:choose>
    <c:when test="${canEvaluate}">Bản nháp và đánh giá đã chốt được lưu trên hệ thống. Chỉ mentor đang được phân công mới có thể cập nhật đánh giá.</c:when>
    <c:otherwise>Chưa thể đánh giá: chỉ mở đánh giá từ giữa kỳ thực tập và khi hồ sơ có đủ ngày bắt đầu, ngày kết thúc.</c:otherwise>
   </c:choose></span>
  </div>
  <form id="evaluationForm" class="evaluation-card" method="post" action="${pageContext.request.contextPath}/mentor/final-evaluation" novalidate>
  <input type="hidden" name="internId" value="${selectedIntern.id}">
  <input type="hidden" name="revisionNo" value="${evaluation.revisionNo != null ? evaluation.revisionNo : 0}">
  <div class="evaluation-top">
   <div class="selected-intern"><span class="field-label">Thực tập sinh đang đánh giá</span><strong><c:out value="${selectedIntern.fullName}"/></strong><small><c:out value="${selectedIntern.internCode}"/> · <c:out value="${selectedIntern.majorName}"/></small></div>
   <div class="intern-meta"><span><i class="fa-regular fa-calendar"></i> Kết thúc kỳ</span><strong><fmt:formatDate value="${selectedIntern.endDate}" pattern="dd/MM/yyyy"/></strong></div>
   <div class="intern-meta"><span><i class="fa-regular fa-calendar-check"></i> Được đánh giá từ</span><strong><fmt:formatDate value="${selectedIntern.eligibleAt}" pattern="dd/MM/yyyy"/></strong></div>
   <div><span class="field-label">Trạng thái</span>
    <c:choose>
     <c:when test="${empty evaluation.status}"><span class="evaluation-status empty">CHƯA ĐÁNH GIÁ</span></c:when>
     <c:when test="${evaluation.status == 'DRAFT'}"><span class="evaluation-status draft">NHÁP</span></c:when>
     <c:when test="${evaluation.status == 'FINALIZED_AGAIN'}"><span class="evaluation-status finalized">ĐÃ CHỐT LẠI</span></c:when>
     <c:otherwise><span class="evaluation-status finalized">ĐÃ CHỐT</span></c:otherwise>
    </c:choose>
   </div>
  </div>
  <div class="evaluation-context"><i class="fa-regular fa-calendar"></i> Thời gian thực tập:
   <fmt:formatDate value="${selectedIntern.startDate}" pattern="dd/MM/yyyy"/> – <fmt:formatDate value="${selectedIntern.endDate}" pattern="dd/MM/yyyy"/>
   <c:if test="${evaluation.finalizationCount > 0}"> · Đã chốt <strong><c:out value="${evaluation.finalizationCount}"/></strong> lần</c:if>
   <c:if test="${not empty evaluation.finalizedAt}"> · Lần chốt gần nhất: <fmt:formatDate value="${evaluation.finalizedAt}" pattern="dd/MM/yyyy HH:mm"/></c:if>
  </div>
  <div class="criteria-heading"><div><h2>Tiêu chí đánh giá</h2><p>Chấm từng tiêu chí từ 1 (cần cải thiện) đến 5 (xuất sắc).</p></div><span class="scale-label">THANG ĐIỂM 1–5</span></div>
  <div class="criteria-list">
   <div class="criterion"><div class="criterion-copy"><strong>1. Kiến thức chuyên môn</strong><small>Nắm vững kiến thức và áp dụng vào công việc.</small></div><div class="rating" role="radiogroup" aria-label="Kiến thức chuyên môn"><c:forEach begin="1" end="5" var="score"><label><input type="radio" name="knowledge" value="${score}" ${evaluation.knowledgeScore == score ? 'checked' : ''} ${canEvaluate ? '' : 'disabled'}><span>${score}</span></label></c:forEach></div></div>
   <div class="criterion"><div class="criterion-copy"><strong>2. Giải quyết vấn đề</strong><small>Phân tích tình huống và đề xuất hướng xử lý.</small></div><div class="rating" role="radiogroup" aria-label="Giải quyết vấn đề"><c:forEach begin="1" end="5" var="score"><label><input type="radio" name="problemSolving" value="${score}" ${evaluation.problemSolvingScore == score ? 'checked' : ''} ${canEvaluate ? '' : 'disabled'}><span>${score}</span></label></c:forEach></div></div>
   <div class="criterion"><div class="criterion-copy"><strong>3. Chủ động và trách nhiệm</strong><small>Chủ động nhận việc, đúng hạn và chịu trách nhiệm.</small></div><div class="rating" role="radiogroup" aria-label="Chủ động và trách nhiệm"><c:forEach begin="1" end="5" var="score"><label><input type="radio" name="initiative" value="${score}" ${evaluation.initiativeScore == score ? 'checked' : ''} ${canEvaluate ? '' : 'disabled'}><span>${score}</span></label></c:forEach></div></div>
   <div class="criterion"><div class="criterion-copy"><strong>4. Hợp tác và kỷ luật</strong><small>Phối hợp nhóm, giao tiếp và tuân thủ quy định.</small></div><div class="rating" role="radiogroup" aria-label="Hợp tác và kỷ luật"><c:forEach begin="1" end="5" var="score"><label><input type="radio" name="teamwork" value="${score}" ${evaluation.teamworkScore == score ? 'checked' : ''} ${canEvaluate ? '' : 'disabled'}><span>${score}</span></label></c:forEach></div></div>
  </div>
  <div class="score-summary">
   <article><span>ĐIỂM KỸ NĂNG</span><strong id="skillScore"><c:out value="${evaluation.skillScore != null ? evaluation.skillScore : '—'}"/></strong><small>Trung bình tiêu chí 1–2</small></article>
   <article><span>ĐIỂM THÁI ĐỘ</span><strong id="attitudeScore"><c:out value="${evaluation.attitudeScore != null ? evaluation.attitudeScore : '—'}"/></strong><small>Trung bình tiêu chí 3–4</small></article>
   <article><span>ĐIỂM CHUNG</span><strong id="overallScore"><c:out value="${evaluation.overallScore != null ? evaluation.overallScore : '—'}"/></strong><small>Trung bình 4 tiêu chí</small></article>
  </div>
  <div class="comment-area"><label for="generalComment">Nhận xét chung <b>* khi chốt</b></label><textarea id="generalComment" name="generalComment" maxlength="2000" rows="4" ${canEvaluate ? '' : 'disabled'} placeholder="Nhập nhận xét về điểm mạnh, điểm cần cải thiện và định hướng phát triển..."><c:out value="${evaluation.overallComments}"/></textarea><div class="comment-hint"><i class="fa-solid fa-lock"></i> Đánh giá đã chốt được lưu lịch sử nếu cần chỉnh sửa và chốt lại.</div></div>
  <div id="formMessage" class="form-message" role="status" aria-live="polite"></div>
  <div class="evaluation-actions"><button type="submit" class="btn-secondary" name="action" value="saveDraft" ${canEvaluate ? '' : 'disabled'}><i class="fa-regular fa-floppy-disk"></i> Lưu nháp</button><button type="submit" class="btn-primary" name="action" value="finalize" ${canEvaluate ? '' : 'disabled'}><i class="fa-solid fa-check"></i> <c:choose><c:when test="${evaluation.finalizationCount > 0}">Chốt lại đánh giá</c:when><c:otherwise>Chốt đánh giá</c:otherwise></c:choose></button></div>
  </form>
 </c:otherwise>
</c:choose>
</section></main></div>
<script src="${pageContext.request.contextPath}/frontend/js/mentor-final-evaluation.js"></script>
</body></html>
