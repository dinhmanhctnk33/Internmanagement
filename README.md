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
   - `src/main/resources/migration-002-review-email-queue.sql`
   - `src/main/resources/migration-003-candidate-result-email.sql`
   - `src/main/resources/migration-004-meeting-email.sql`
   - `src/main/resources/migration-005-mentor-final-evaluation.sql`
   - `src/main/resources/demo-data.sql`

Với database đã có, chạy các migration chưa áp dụng theo thứ tự; không chạy lại
`schema.sql` trên dữ liệu đang sử dụng.

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


## Sprint 3 - SCRUM-136: Đánh giá tổng kết

- URL: `/mentor/final-evaluation` (yêu cầu đăng nhập bằng tài khoản Mentor).
- Chỉ hiển thị thực tập sinh được phân công cho Mentor hiện tại; có thể lọc theo trạng thái đánh giá và thời điểm đủ điều kiện.
- Đánh giá mở từ giữa kỳ thực tập (ngày bắt đầu cộng một nửa số ngày đến ngày kết thúc); hồ sơ thiếu ngày hoặc chưa đến giữa kỳ không thể lưu/chốt.
- Bốn tiêu chí được chấm từ 1–5; điểm kỹ năng, thái độ và điểm chung được tự tính. Bản nháp có thể lưu từng phần; chốt yêu cầu đủ điểm và nhận xét ít nhất 5 ký tự.
- Đánh giá được lưu trong `mentor_final_evaluations`; mỗi lần cập nhật lưu phiên bản trước trong `mentor_final_evaluation_history` và ghi lại thao tác vào `audit_logs`.
- Khi cần sửa bản đã chốt, lưu thành nháp trước rồi chốt lại; lịch sử phiên bản trước được giữ lại. Điều kiện và phân công được kiểm tra lại ở phía máy chủ trong transaction.
