# Kế hoạch kiểm thử toàn hệ thống IMS

## Phạm vi và môi trường

- Ứng dụng Java 21, Servlet/JSP, MySQL 8, Tomcat 10.1.
- Toàn bộ controller, DAO, quy tắc nghiệp vụ, schema hợp nhất, JSP và JavaScript trong repository.
- Tài khoản thử nghiệm: `admin_demo`, `hr_demo`, `mentor_demo`, `intern_demo` (mật khẩu dữ liệu mẫu `123456`).

## 1. Kiểm thử chức năng

| Nhóm | Kịch bản chính | Kết quả mong đợi |
|---|---|---|
| Xác thực | Đăng nhập đúng/sai; đăng xuất; đổi và đặt lại mật khẩu | Đúng vai trò; mật khẩu được băm; token không tái sử dụng |
| Hồ sơ | Tạo, sửa, lọc, nhập Excel; duyệt tài liệu | Dữ liệu hợp lệ được lưu, dữ liệu lỗi bị từ chối, có thông báo |
| Chương trình | Quản lý chương trình, mentor, phân công | Không vượt tải; chỉ HR/Admin thao tác |
| Mentor | Giao nhiệm vụ, xem tiến độ, phản hồi báo cáo, đánh giá cuối kỳ | Chỉ tác động TTS được phân công; đúng trạng thái |
| Thực tập sinh | Cập nhật tiến độ, báo cáo tuần, nghỉ phép, chấm công, hợp đồng | Kiểm tra biên và quyền sở hữu; mentor/HR nhận thông báo |
| Lịch/Báo cáo | Lịch họp, lịch làm việc, báo cáo cuối kỳ, tải/gửi | Dữ liệu tổng hợp đúng; file sinh được; lịch sử không mất |
| Thông báo | Danh sách, đánh dấu đã đọc, điều hướng | Đúng người nhận, URL hợp lệ |

## 2. Kiểm thử phi chức năng

- Hiệu năng smoke: đo P95 của toàn bộ trang GET sau đăng nhập, ngưỡng mặc định 2 giây trên máy local.
- Khả năng đóng gói: `mvn package` phải tạo được WAR deploy lên Tomcat 10.1.
- Tương thích: kiểm tra cú pháp toàn bộ JavaScript bằng Node; trang dùng UTF-8 và viewport responsive.
- Tin cậy: các thao tác DAO nhiều bước phải rollback khi lỗi; script CSDL và demo chạy lặp lại được.
- Bảo mật cơ bản: mật khẩu BCrypt, kiểm tra truy cập chưa đăng nhập/sai vai trò, truy vấn tham số hóa.

## 3. Kiểm thử kiến trúc

- Route servlet không trùng nhau.
- Mọi JSP được controller forward tới phải tồn tại và nằm dưới `WEB-INF`.
- Model/DAO không phụ thuộc controller, Servlet hoặc view.
- Model không tự mở kết nối CSDL.
- Tất cả khóa ngoại trong `database.sql` phải trỏ tới bảng được khai báo.
- Schema hợp nhất phải chứa đủ các aggregate nghiệp vụ cốt lõi và dữ liệu demo.

## 4. Kiểm thử hộp xám

- Kiểm tra quy tắc ngày nghỉ, chồng lấn đơn và chuyển trạng thái bằng H2 tại lớp DAO.
- Kiểm tra upload–duyệt tài liệu–thông báo xuyên nhiều bảng.
- Kiểm tra tính điểm, điều kiện mở/chốt đánh giá cuối kỳ.
- Kiểm tra mật khẩu cũ được nâng cấp sang BCrypt và hash lỗi không làm sập hệ thống.
- Kiểm tra route bảo vệ dựa trên cấu hình filter và phiên đăng nhập thật.

## Cách chạy

```powershell
# Đã deploy ứng dụng local
.\tools\test-system.ps1

# Chỉ chạy test mã nguồn/kiến trúc khi chưa có Tomcat
.\tools\test-system.ps1 -SkipHttp
```

Script trả exit code `1` nếu có bất kỳ ca nào không đạt để có thể dùng trong CI.
