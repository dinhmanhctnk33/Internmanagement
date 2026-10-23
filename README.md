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

Sau khi cấu hình MySQL, mở PowerShell tại thư mục dự án và chạy một lệnh:

```powershell
.\setup-database.ps1
```

Script tự đọc các biến `IMS_DB_*` hoặc `src/main/resources/db-config.json`, sau đó
nạp file `src/main/resources/database.sql` bằng MySQL Client. Nếu PowerShell chặn
script cục bộ, chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-database.ps1
```

File này đã bao gồm toàn bộ cấu trúc bảng, phần mở rộng nghiệp vụ và dữ liệu mẫu cần thiết cho môi trường local.

### Chuẩn bị dữ liệu demo theo vai trò

Sau khi chạy `setup-database.ps1`, chọn script tương ứng với vai trò cần
trình diễn:

| Vai trò | Script | Tài khoản | Mật khẩu |
|---|---|---|---|
| Mentor | `.\setup-mentor-demo.ps1` | `mentor_demo` | `123456` |
| HR | `.\setup-hr-demo.ps1` | `hr_demo` | `123456` |
| Intern | `.\setup-intern-demo.ps1` | `intern_demo` | `123456` |
| Admin | `.\setup-admin-demo.ps1` | `admin_demo` | `123456` |

Ví dụ, để chuẩn bị toàn bộ dữ liệu cho cả bốn vai trò:

```powershell
.\setup-admin-demo.ps1
```

Các script được xếp theo chuỗi phụ thuộc: Admin chuẩn bị Intern, Intern chuẩn bị
HR, và HR chuẩn bị Mentor. Vì vậy chạy `setup-admin-demo.ps1` là đủ cho môi
trường demo đầy đủ; khi chỉ cần một luồng nhỏ hơn, có thể chạy script của
vai trò tương ứng.

Mỗi bộ dữ liệu có 10 bản ghi mẫu cho các màn hình nghiệp vụ chính,
bao gồm nhiều trạng thái để thuận tiện trình diễn và kiểm thử. Các script có
thể chạy lại; những bản ghi demo chính sẽ được bỏ qua hoặc cập nhật thay vì
tạo trùng.

Nếu PowerShell chặn script cục bộ, có thể chạy, ví dụ với bộ Admin:

```powershell
powershell -ExecutionPolicy Bypass -File .\setup-admin-demo.ps1
```

## Build và chạy

```bash
mvn clean test
mvn package
```

Sau khi build, deploy `target/Internmanagement.war` lên Tomcat 10.1+, sau đó truy cập:

```text
http://localhost:8080/Internmanagement/login
```

## Kiểm thử toàn hệ thống

Sau khi deploy ứng dụng local, chạy bộ kiểm thử chức năng, phi chức năng, kiến trúc
và hộp xám:

```powershell
.\tools\test-system.ps1
```

Nếu chưa chạy Tomcat, có thể kiểm tra mã nguồn, nghiệp vụ và khả năng đóng gói bằng
`.\tools\test-system.ps1 -SkipHttp`. Ma trận chi tiết nằm tại
`docs/KE-HOACH-KIEM-THU.md` và kết quả lần chạy gần nhất nằm tại
`docs/BAO-CAO-KIEM-THU.md`.

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

## Lịch họp và email tự động

HR và mentor truy cập `/meetings` để tạo, cập nhật hoặc hủy lịch họp. Mentor chỉ
chọn được thực tập sinh đang được mình hướng dẫn. Email mời/hủy được gửi ngay;
các cập nhật quan trọng trong vòng 5 phút được gộp trước khi gửi. Trạng thái gửi
theo từng người nhận được hiển thị trong chi tiết lịch họp.
