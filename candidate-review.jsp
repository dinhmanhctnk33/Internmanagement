<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Duyệt hồ sơ ứng viên - IMS</title><link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/dashboard.css?v=20261007-4"><link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/candidate-review.css?v=20261007-1"><link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="logo">
            <div class="logo-icon"><i class="fa-solid fa-user-tie"></i></div>
            <div><h2>IMS Portal</h2><span>Phòng Nhân sự (HR)</span></div>
        </div>
        <nav class="menu">
            <p class="menu-title">NGHIỆP VỤ HR</p>
            <a href="${pageContext.request.contextPath}/home" class="menu-item">
                <i class="fa-solid fa-chart-line"></i><span>Dashboard HR</span>
            </a>
            <a href="${pageContext.request.contextPath}/applications" class="menu-item active">
                <i class="fa-solid fa-user-check"></i><span>Duyệt hồ sơ</span>
            </a>
            <a href="${pageContext.request.contextPath}/review-emails" class="menu-item">
                <i class="fa-solid fa-envelope-circle-check"></i><span>Email kết quả</span>
            </a>
        </nav>
        <div class="sidebar-bottom">
            <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">
                <i class="fa-solid fa-right-from-bracket"></i><span>Đăng xuất</span>
            </a>
        </div>
    </aside>

    <main class="main">
        <header class="header"></header>
        <section class="content review-page">
            <div class="review-heading">
                <div>
                    <span class="eyebrow">QUẢN LÝ TUYỂN DỤNG</span>
                    <h1>Duyệt hồ sơ ứng viên</h1>
                    <p>Kiểm tra thông tin, ghi rõ lý do và xác nhận kết quả cho từng hồ sơ.</p>
                </div>
                <div class="review-summary" aria-label="Tổng số hồ sơ trong danh sách">
                    <span class="summary-icon"><i class="fa-regular fa-folder-open"></i></span>
                    <span><strong>${candidates.size()}</strong><small>Hồ sơ hiển thị</small></span>
                </div>
            </div>

            <c:if test="${param.result=='sent'}">
                <div class="result-alert" role="status">
                    <i class="fa-solid fa-circle-check"></i>
                    <div><strong>Duyệt hồ sơ thành công</strong><span>Kết quả đã được lưu và email đã gửi tới ứng viên.</span></div>
                </div>
            </c:if>
            <c:if test="${param.result=='queued'}">
                <div class="result-alert warning" role="status">
                    <i class="fa-solid fa-clock"></i>
                    <div><strong>Đã lưu kết quả</strong><span>Email đang trong hàng đợi và hệ thống sẽ tự gửi lại.</span></div>
                </div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="result-alert error" role="alert">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <div><strong>Chưa thể cập nhật hồ sơ</strong><span><c:out value="${param.error}"/></span></div>
                </div>
            </c:if>

            <form class="review-tools" method="get">
                <div class="filter-title">
                    <i class="fa-solid fa-filter"></i>
                    <span><strong>Bộ lọc hồ sơ</strong><small>Tìm nhanh ứng viên cần xử lý</small></span>
                </div>
                <label class="search-field">
                    <span>Tìm ứng viên</span>
                    <span class="input-with-icon">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input name="q" value="<c:out value='${keyword}'/>" placeholder="Nhập tên hoặc email">
                    </span>
                </label>
                <label>
                    <span>Trạng thái</span>
                    <select name="status">
                        <option value="">Tất cả trạng thái</option>
                        <option value="NEW" ${selectedStatus=='NEW'?'selected':''}>Chờ xét</option>
                        <option value="INTERVIEWING" ${selectedStatus=='INTERVIEWING'?'selected':''}>Đang phỏng vấn</option>
                        <option value="ACCEPTED" ${selectedStatus=='ACCEPTED'?'selected':''}>Đã duyệt</option>
                        <option value="REJECTED" ${selectedStatus=='REJECTED'?'selected':''}>Đã từ chối</option>
                    </select>
                </label>
                <button class="filter-btn" type="submit"><i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm</button>
                <c:if test="${not empty selectedStatus or not empty keyword}">
                    <a class="clear-filter" href="${pageContext.request.contextPath}/applications">Xóa lọc</a>
                </c:if>
            </form>

            <div class="review-card">
                <div class="card-heading">
                    <div>
                        <h2>Danh sách hồ sơ</h2>
                        <p>Lý do là bắt buộc và cần có ít nhất 5 ký tự trước khi xác nhận.</p>
                    </div>
                    <span class="record-count">${candidates.size()} hồ sơ</span>
                </div>

                <div class="table-container review-table-wrap">
                    <table class="review-table">
                        <thead>
                        <tr>
                            <th scope="col">Ứng viên</th>
                            <th scope="col">Học vấn</th>
                            <th scope="col">Vị trí ứng tuyển</th>
                            <th scope="col">Trạng thái</th>
                            <th scope="col" class="decision-column">Quyết định của HR</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${candidates}" var="a">
                            <tr>
                                <td>
                                    <div class="candidate-cell">
                                        <span class="candidate-avatar" aria-hidden="true"><i class="fa-solid fa-user"></i></span>
                                        <span class="candidate-meta">
                                            <strong><c:out value="${a.full_name}"/></strong>
                                            <small><i class="fa-regular fa-envelope"></i><c:out value="${a.email}"/></small>
                                            <small><i class="fa-solid fa-phone"></i><c:out value="${empty a.phone ? 'Chưa cập nhật' : a.phone}"/></small>
                                        </span>
                                    </div>
                                </td>
                                <td class="education-cell">
                                    <strong><c:out value="${a.university_name}"/></strong>
                                    <small><c:out value="${a.major_name}"/></small>
                                </td>
                                <td><span class="position-chip"><c:out value="${a.desired_position}"/></span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${a.application_status=='ACCEPTED'}">
                                            <span class="review-status approved"><i class="fa-solid fa-check"></i> Đã duyệt</span>
                                        </c:when>
                                        <c:when test="${a.application_status=='REJECTED'}">
                                            <span class="review-status rejected"><i class="fa-solid fa-xmark"></i> Đã từ chối</span>
                                        </c:when>
                                        <c:when test="${a.application_status=='INTERVIEWING'}">
                                            <span class="review-status interviewing"><i class="fa-solid fa-comments"></i> Đang phỏng vấn</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="review-status pending"><i class="fa-regular fa-clock"></i> Chờ xét</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="decision-cell">
                                    <c:choose>
                                        <c:when test="${a.application_status=='NEW'||a.application_status=='INTERVIEWING'}">
                                            <form method="post" class="decision-form">
                                                <input type="hidden" name="candidateId" value="${a.id}">
                                                <label for="reason-${a.id}">Lý do xác nhận <span aria-hidden="true">*</span></label>
                                                <textarea id="reason-${a.id}" name="reason" required minlength="5" maxlength="1000"
                                                          aria-describedby="reason-error-${a.id}"
                                                          placeholder="Nhập lý do duyệt hoặc từ chối hồ sơ"></textarea>
                                                <span id="reason-error-${a.id}" class="field-error">
                                                    <i class="fa-solid fa-circle-exclamation"></i>
                                                    Vui lòng nhập lý do ít nhất 5 ký tự.
                                                </span>
                                                <div class="decision-actions">
                                                    <button class="pass-btn" name="decision" value="PASSED" type="submit">
                                                        <i class="fa-solid fa-check"></i> Duyệt hồ sơ
                                                    </button>
                                                    <button class="fail-btn" name="decision" value="FAILED" type="submit">
                                                        <i class="fa-solid fa-xmark"></i> Từ chối
                                                    </button>
                                                </div>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="status-final">
                                                <span class="final-label"><i class="fa-solid fa-lock"></i> Đã xác nhận kết quả</span>
                                                <p><c:out value="${a.hr_notes}"/></p>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty candidates}">
                            <tr>
                                <td colspan="5" class="empty-review-state">
                                    <i class="fa-regular fa-folder-open"></i>
                                    <strong>Không tìm thấy hồ sơ phù hợp</strong>
                                    <span>Hãy thử thay đổi từ khóa hoặc trạng thái trong bộ lọc.</span>
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    </main>
</div>
</body>
</html>
