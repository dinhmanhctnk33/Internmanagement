# Phân tích nghiệp vụ: Hệ thống gửi email thông báo kết quả xét duyệt

**User story:** Là hệ thống, tôi muốn gửi email thông báo kết quả xét duyệt để thực tập sinh nhận được thông tin kịp thời.

**Bối cảnh:** Story này nối tiếp "Đăng ký tài khoản và nộp hồ sơ" và "HR duyệt hoặc từ chối hồ sơ". Quy trình tổng thể: Đăng ký tài khoản → Nộp hồ sơ → Duyệt hồ sơ → Phỏng vấn/kiểm tra → Nhận hoặc từ chối. Story này lo phần thông báo kết quả cho cả các bước sau duyệt hồ sơ.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Hệ thống:** tiến trình gửi theo lịch, gom các thông báo chờ gửi và gửi đi.
- **HR:** xem trạng thái thông báo, thu hồi hoặc sửa quyết định trước giờ gửi.
- **Thực tập sinh:** nhận email kết quả.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Sự kiện kết quả | Một hồ sơ chuyển sang trạng thái kết quả. Bốn loại: *Đã duyệt*, *Bị từ chối* (kèm lý do riêng), *kết quả phỏng vấn/kiểm tra*, *quyết định nhận cuối cùng* (Được nhận/Không được nhận). Hai loại sau chỉ chốt hợp đồng giao tiếp ở đây; nội dung chi tiết thiết kế ở story sau. |
| Thông báo chờ gửi | Gắn với một hồ sơ và một loại kết quả. Trạng thái: *Chờ gửi → Đã gửi*, hoặc *Gửi lỗi*, hoặc *Đã hủy*. |
| Lịch gửi | Một giờ cố định mỗi ngày, cấu hình được (ví dụ 9 giờ). |
| Mẫu email | Một mẫu cố định cho mỗi loại kết quả. Quản trị mẫu nằm ngoài phạm vi. |

### Quyết định thiết kế

- Email gửi **theo lịch cố định mỗi ngày**, không gửi tức thì.
- Cho phép HR **thu hồi hoặc sửa quyết định trước giờ gửi** (làm trong story này).
- Dùng **hàng đợi thông báo chờ gửi** để lưu và gửi email.
- Tại một thời điểm, mỗi hồ sơ có **tối đa một thông báo chờ gửi**; thực tập sinh chỉ nhận email theo kết quả cuối cùng, không nhận email trung gian.
- Chỉ có **một giờ gửi mỗi ngày**; kết quả xác nhận sau giờ đó chờ lô của ngày hôm sau.

### Phạm vi

Từ lúc hồ sơ có kết quả đến lúc email được gửi hoặc bị hủy, cộng với việc HR thu hồi/sửa quyết định trong khoảng chờ.

## 2. Luồng nghiệp vụ

1. **Tạo thông báo.** Khi hồ sơ có kết quả (HR xác nhận duyệt/từ chối, hoặc story sau phát sự kiện), hệ thống tạo một thông báo "Chờ gửi" gồm loại kết quả và dữ liệu cần cho email (ví dụ lý do từ chối), dự kiến gửi ở lần chạy lịch kế tiếp.
2. **Khoảng chờ.** HR xem danh sách thông báo đang chờ (hồ sơ, loại kết quả, giờ dự kiến gửi) và có thể:
   - **Thu hồi:** hồ sơ quay về "Đang duyệt", thông báo chuyển "Đã hủy". Làm được hàng loạt.
   - **Sửa:** đổi kết quả hoặc sửa lý do qua màn hình duyệt/từ chối đã có. Thông báo cũ "Đã hủy", thông báo mới "Chờ gửi".
3. **Đến giờ gửi.** Hệ thống chốt lô tại thời điểm bắt đầu chạy và gửi từng email riêng cho từng thực tập sinh. Thành công thì "Đã gửi" (ghi thời điểm). Lỗi tạm thời thì thử lại vài lần trong ngày. Hết số lần thử thì "Gửi lỗi" để HR nhìn thấy.

### Vòng đời thông báo

`Chờ gửi → Đã gửi | Gửi lỗi | Đã hủy`

### Quy tắc nghiệp vụ

- Mỗi hồ sơ có tối đa một thông báo "Chờ gửi" tại một thời điểm.
- Thu hồi/sửa chỉ có hiệu lực với thông báo còn "Chờ gửi" tại thời điểm chốt lô. Sau khi đã gửi thì không thu hồi được.
- Gửi an toàn khi chạy trùng: mỗi thông báo tạo tối đa một email, kể cả khi tiến trình chạy lại.
- Nội dung email lấy theo kết quả và lý do **mới nhất** tại thời điểm gửi.
- Mọi bước tạo, sửa, hủy, gửi đều có nhật ký (ai, khi nào).
- Trong khoảng chờ, thực tập sinh chưa thấy kết quả trong hệ thống; kết quả chỉ hiển thị cho họ sau khi email đã gửi.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **HR thu hồi/sửa đúng lúc hệ thống đang chốt lô:** thao tác đến trước giờ chốt thì có hiệu lực; đến sau thì HR được báo "thông báo đã được xử lý/gửi, không thu hồi được".
- **Tiến trình gửi bị gián đoạn giữa chừng:** lần chạy tiếp theo gửi nốt các thông báo còn "Chờ gửi" và không gửi trùng các thông báo đã gửi.
- **Gửi lỗi tạm thời** (máy chủ thư bận): thử lại vài lần trong ngày; hết số lần thì "Gửi lỗi" và HR thấy được.
- **Địa chỉ email không nhận được:** đánh dấu "Gửi lỗi" ngay, không thử lại vô ích.
- **Không có thông báo nào chờ gửi trong ngày:** lần chạy kết thúc bình thường, không gửi gì.
- **Số lượng thông báo lớn:** gửi theo từng phần nhỏ, không treo hệ thống; nếu hết giờ chạy chưa xong thì phần còn lại tiếp tục ở lần chạy sau, không bị bỏ sót.
- **Hồ sơ bị sửa quyết định nhiều lần trước giờ gửi:** chỉ giữ thông báo mới nhất, các thông báo cũ đều "Đã hủy".

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR xác nhận một kết quả thì hệ thống tạo một thông báo "Chờ gửi" và hiển thị giờ dự kiến gửi. |
| 2 | Đến giờ gửi, mỗi thông báo "Chờ gửi" tạo đúng một email cho đúng thực tập sinh và chuyển "Đã gửi". |
| 3 | Email từ chối nêu đúng lý do riêng của hồ sơ đó. |
| 4 | HR thu hồi một thông báo trước giờ gửi thì không có email nào được gửi, hồ sơ quay về "Đang duyệt" và thông báo "Đã hủy". |
| 5 | HR sửa quyết định trước giờ gửi thì chỉ email theo kết quả mới được gửi, thông báo cũ "Đã hủy". |
| 6 | Thu hồi/sửa sau khi email đã gửi thì không thực hiện được và HR được báo rõ. |
| 7 | Tiến trình chạy lại hoặc chạy trùng thì không có thực tập sinh nào nhận email trùng. |
| 8 | Gửi lỗi quá số lần thử thì thông báo ở trạng thái "Gửi lỗi" và HR thấy được trong danh sách. |
| 9 | Trong khoảng chờ, thực tập sinh không thấy kết quả trong hệ thống; sau khi email gửi thì thấy. |
| 10 | Mọi thao tác tạo, sửa, hủy, gửi đều có nhật ký. |

### Yêu cầu phi chức năng tối thiểu

- Không làm mất thông báo kể cả khi hệ thống khởi động lại giữa chừng.
- Giờ gửi và số lần thử lại là cấu hình, không cố định trong mã.
- Chỉ HR được xem danh sách thông báo, thu hồi và sửa; nội dung email chỉ gồm thông tin cần thiết của chính thực tập sinh nhận.

### Ngoài phạm vi (để story sau)

- Gửi lại thủ công thông báo bị lỗi.
- Quản trị mẫu email và giờ gửi qua giao diện.
- Nhiều giờ gửi trong ngày.
- Kênh thông báo khác (thông báo trong ứng dụng, SMS).
- Email xác thực/xác nhận nộp/yêu cầu bổ sung/lịch phỏng vấn.
- Báo cáo thống kê tỷ lệ gửi thành công.

## 4. Tác động lên tài liệu "HR duyệt hoặc từ chối hồ sơ"

- Quy tắc "Quyết định không hoàn tác vì email đã gửi đi" đổi thành: quyết định **thu hồi/sửa được cho tới trước giờ chốt lô gửi**, không hoàn tác được sau khi email đã gửi.
- "Bị từ chối" chỉ là trạng thái cuối **sau khi email đã gửi**.
- Email không còn gửi ngay sau khi HR xác nhận mà **gửi theo lịch**; luồng và tiêu chí chấp nhận liên quan cần đọc theo thay đổi này.