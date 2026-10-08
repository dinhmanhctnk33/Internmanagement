# Intern Management

Hệ thống quản lý thực tập xây dựng bằng Java Servlet/JSP, MySQL và Maven.

## Yêu cầu

- JDK 21
- Maven 3.9+
- MySQL 8
- Tomcat 10.1+

## Cấu hình database

Khuyến nghị khai báo ba biến môi trường:

```text
IMS_DB_URL=jdbc:mysql://localhost:3306/internship_management?useUnicode=true&characterEncoding=UTF-8
IMS_DB_USER=root
IMS_DB_PASSWORD=<mat-khau-mysql>
```

Khi chạy local, có thể sao chép `src/main/resources/db-config.example.json` thành
`src/main/resources/db-config.json` và thay thông tin tương ứng. File `db-config.json`
đã bị Git bỏ qua vì có chứa thông tin bí mật.

## Khởi tạo dữ liệu

1. Tạo/kết nối MySQL bằng tài khoản đã cấu hình.
2. Chạy lần lượt:
   - `src/main/resources/schema.sql`
   - `src/main/resources/migration-001-rbac.sql`
   - `src/main/resources/demo-data.sql`

## Build và chạy

```bash
mvn clean test
mvn package
```

Sau khi build, deploy `target/Internmanagement.war` lên Tomcat 10.1+, sau đó truy cập:

```text
http://localhost:8080/Internmanagement/login
```

## Cấu hình email kết quả xét duyệt

Khai báo các biến môi trường trước khi khởi động Tomcat:

```text
IMS_SMTP_HOST=smtp.gmail.com
IMS_SMTP_PORT=587
IMS_SMTP_USER=<email-gui>
IMS_SMTP_PASSWORD=<app-password>
IMS_RESULT_EMAIL_MAX_ATTEMPTS=3
IMS_RESULT_EMAIL_RETRY_MINUTES=30
```

Khi HR xác nhận ứng viên **Đỗ** hoặc **Trượt** kèm lý do tại `/applications`, hệ thống
tự tạo email theo mẫu và gửi ngay. Nếu SMTP tạm thời gặp lỗi, email được giữ trong
`candidate_result_email_queue`; tiến trình nền kiểm tra mỗi phút và tự thử lại. HR
theo dõi trạng thái tại `/review-emails`. Việc duyệt tài liệu/CV không phát sinh email
kết quả tuyển dụng.

Không commit `target/`, `tmp/`, `node_modules/`, file cookie hoặc file cấu hình
chứa mật khẩu.
