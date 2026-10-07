<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Đăng ký tài khoản</title>

        <link rel="stylesheet"
            href="${pageContext.request.contextPath}/frontend/css/internship.css">

</head>

<body>

<div class="page">

    <!-- HEADER -->
    <header class="header">

        <div class="header-container">

            <div class="logo">
                HỆ THỐNG THỰC TẬP
            </div>

            <a href="${pageContext.request.contextPath}/login" class="login-link">
                Đăng nhập
            </a>

        </div>

    </header>


    <!-- MAIN -->
    <main class="main">

        <div class="form-card">

            <div class="form-title">

                <h1>Đăng ký tài khoản</h1>

                <p>
                    Tạo tài khoản để đăng ký chương trình thực tập
                </p>

            </div>


            <form action="${pageContext.request.contextPath}/register" method="post">

                <!-- HỌ TÊN -->
                <div class="form-group">

                    <label for="fullName">
                        Họ và tên
                        <span class="required">*</span>
                    </label>

                    <input
                        type="text"
                        id="fullName"
                        name="fullName"
                        placeholder="Nhập họ và tên"
                        maxlength="100"
                        required>

                </div>


                <!-- EMAIL -->
                <div class="form-group">

                    <label for="email">
                        Email
                        <span class="required">*</span>
                    </label>

                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Nhập email"
                        maxlength="150"
                        required>

                    <small>
                        Email dùng để xác thực tài khoản.
                    </small>

                </div>


                <!-- MẬT KHẨU -->
                <div class="form-group">

                    <label for="password">
                        Mật khẩu
                        <span class="required">*</span>
                    </label>

                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Nhập mật khẩu"
                        minlength="8"
                        required>

                    <small>
                        Mật khẩu tối thiểu 8 ký tự.
                    </small>

                </div>


                <!-- XÁC NHẬN -->
                <div class="form-group">

                    <label for="confirmPassword">
                        Xác nhận mật khẩu
                        <span class="required">*</span>
                    </label>

                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        placeholder="Nhập lại mật khẩu"
                        minlength="8"
                        required>

                </div>


                <button
                    type="submit"
                    class="btn-primary">

                    Đăng ký tài khoản

                </button>

            </form>


            <div class="form-footer">

                <span>Đã có tài khoản?</span>

                <a href="${pageContext.request.contextPath}/login">
                    Đăng nhập
                </a>

            </div>

        </div>

    </main>


    <footer class="footer">

        <p>
            © 2026 Hệ thống quản lý thực tập
        </p>

    </footer>

</div>

</body>

</html>