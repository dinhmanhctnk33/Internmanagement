<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nộp tài liệu thực tập</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/frontend/css/internship.css">
</head>
<body>
<div class="page">
    <header class="header">
        <div class="header-container">
            <a class="logo" href="${pageContext.request.contextPath}/home">IMS Portal</a>
            <a class="login-link" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </div>
    </header>

    <main class="main application-main">
        <section class="application-card">
            <div class="form-title">
                <h1>Nộp tài liệu thực tập</h1>
                <p>Chọn loại tài liệu và tải lên tệp PDF để HR xét duyệt.</p>
            </div>

            <form action="${pageContext.request.contextPath}/documents/upload"
                  method="post"
                  enctype="multipart/form-data">
                <section class="section">
                    <div class="form-group">
                        <label for="documentType">Loại tài liệu <span class="required">*</span></label>
                        <select id="documentType" name="documentType" required>
                            <option value="CV">CV / Sơ yếu lý lịch</option>
                            <option value="APPLICATION_LETTER">Đơn xin thực tập</option>
                            <option value="RECOMMENDATION_LETTER">Giấy giới thiệu nhà trường</option>
                            <option value="WEEKLY_REPORT">Báo cáo thực tập</option>
                        </select>
                    </div>

                    <div class="upload-box">
                        <label for="file">Tệp PDF <span class="required">*</span></label>
                        <input type="file" id="file" name="file" accept=".pdf,application/pdf" required>
                        <p>Chỉ chấp nhận PDF, dung lượng tối đa 10 MB.</p>
                    </div>
                </section>

                <div class="application-actions">
                    <a class="btn-secondary" href="${pageContext.request.contextPath}/home">Quay lại</a>
                    <button type="submit" class="btn-primary">Tải lên tài liệu</button>
                </div>
            </form>
        </section>
    </main>

    <footer class="footer">
        <p>Hệ thống quản lý thực tập</p>
    </footer>
</div>
</body>
</html>