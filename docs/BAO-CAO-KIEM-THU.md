# Báo cáo kiểm thử hệ thống IMS

Ngày chạy: 21/10/2026  
Môi trường: Windows, Java 21, Maven, MySQL, Tomcat tại `localhost:8080/Internmanagement`.

## Tổng quan

| Hạng mục | Kết quả |
|---|---:|
| JUnit nghiệp vụ/DAO/kiến trúc/bảo mật | 31/31 đạt |
| Route HTTP công khai, theo vai trò và xác thực | 47/47 đạt |
| Kiểm tra sai vai trò | 3/3 trả HTTP 403 |
| Đóng gói | WAR được tạo thành công |
| Hiệu năng trang GET | P95 814 ms / ngưỡng 2.000 ms |
| UI Mentor | Không tràn ngang, không trùng ID, không input thiếu nhãn, không lỗi console |

## Lỗi phát hiện và đã sửa

1. `/interns/create` forward tới JSP không tồn tại: chuyển route cũ về `/interns/new`.
2. Import Excel forward tới `intern-import-confirm.jsp` không tồn tại: bổ sung màn hình xem trước và xác nhận.
3. `UserDAO.resetPassword` có thể lưu mật khẩu thô: bắt buộc băm BCrypt và thêm test hồi quy.
4. Đăng nhập tái sử dụng session cũ: hủy session cũ và tạo session mới để chống session fixation.
5. Thiếu header trình duyệt cơ bản: thêm `nosniff`, chống iframe, referrer policy và permissions policy.

## Điểm chưa đạt / rủi ro còn lại

### Cao — Chưa có CSRF token

38 trang JSP có form nhưng mã nguồn chưa có cơ chế CSRF chung. Kẻ tấn công có thể lợi dụng
phiên đã đăng nhập để gửi yêu cầu thay đổi dữ liệu ngoài ý muốn. Cần triển khai token theo
session, thêm hidden field cho toàn bộ POST form và header cho các yêu cầu JavaScript, sau đó
kiểm thử cả token thiếu/sai/hết hạn.

### Trung bình — Chưa có Content-Security-Policy

Ứng dụng dùng nhiều style/script inline và tài nguyên CDN nên chưa thể bật CSP nghiêm ngặt ngay.
Cần tách inline script/style, dùng nonce hoặc hash và chỉ cho phép các nguồn CDN cần thiết.

### Trung bình — Kiểm thử ghi dữ liệu chưa phủ mọi module

Smoke-test HTTP hiện ưu tiên GET an toàn; các POST có tác động như gửi email, xóa, chốt đánh giá
và tạo báo cáo chưa chạy tự động trên CSDL tách biệt. Nên bổ sung môi trường test riêng và fixture
rollback để kiểm thử E2E đầy đủ mà không thay đổi dữ liệu demo.

## Lệnh tái hiện

```powershell
.\setup-database.ps1
.\setup-mentor-demo.ps1
.\tools\test-system.ps1
```
