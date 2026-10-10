# Phân tích nghiệp vụ: Đăng ký tài khoản và nộp hồ sơ thực tập trực tuyến

**User story:** Là thực tập sinh, tôi muốn đăng ký tài khoản và nộp hồ sơ trực tuyến để tham gia chương trình thực tập.

**Bối cảnh:** Hệ thống quản lý thực tập sinh do một đơn vị tổ chức chương trình vận hành trực tiếp. Quy trình tổng thể: Đăng ký tài khoản → Nộp hồ sơ → Duyệt hồ sơ → Phỏng vấn/kiểm tra → Nhận hoặc từ chối. Story này bao phủ hai bước đầu.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Thực tập sinh:** đăng ký tài khoản, xem các chương trình đang mở, tạo và nộp hồ sơ cho từng chương trình.
- **Bộ phận phụ trách chương trình:** tạo chương trình (xem tài liệu "HR tạo chương trình thực tập"), duyệt hồ sơ. Nằm ngoài story này nhưng là điều kiện tiên quyết.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Tài khoản | Gắn với email đã xác thực, mỗi email chỉ có một tài khoản. |
| Chương trình thực tập | (trước đây gọi là chương trình) Thuộc một phòng ban, có thời hạn nhận hồ sơ và yêu cầu giấy tờ riêng (giấy tờ nào bắt buộc, giấy tờ nào tùy chọn). |
| Hồ sơ | Thuộc đúng một tài khoản và đúng một chương trình. Gồm form thông tin, CV và giấy tờ đính kèm (bảng điểm, thư giới thiệu, portfolio). |

### Quyết định thiết kế

- Hồ sơ **gắn với từng chương trình** (hướng C).
- Hồ sơ có **lưu nháp** và **bổ sung theo yêu cầu**.
- Mỗi thực tập sinh nộp **tối đa một hồ sơ cho mỗi chương trình**, nhưng được nộp cho nhiều chương trình khác nhau.
- Đăng ký bằng **email + mật khẩu, bắt buộc xác thực email**.

## 2. Luồng nghiệp vụ

### Luồng đăng ký tài khoản

1. Thực tập sinh nhập họ tên, email, mật khẩu.
2. Hệ thống kiểm tra email chưa tồn tại, mật khẩu đủ mạnh; tạo tài khoản ở trạng thái "Chưa xác thực" và gửi email chứa liên kết xác thực.
3. Bấm liên kết còn hạn thì tài khoản chuyển sang "Đã xác thực" và đăng nhập được.
4. Liên kết hết hạn hoặc không nhận được email thì cho phép gửi lại.

### Luồng nộp hồ sơ

1. Thực tập sinh đã xác thực xem danh sách chương trình đang mở và chọn một.
2. Hệ thống tạo hồ sơ nháp; người nộp điền form, tải CV và giấy tờ lên, lưu nháp tùy ý.
3. Khi bấm nộp, hệ thống kiểm tra đủ trường và giấy tờ bắt buộc theo yêu cầu của chương trình đó, và chương trình còn nhận hồ sơ.
4. Nếu đạt, hồ sơ chuyển sang "Đã nộp", khóa chỉnh sửa và gửi email xác nhận.

### Vòng đời hồ sơ

`Nháp → Đã nộp → (Cần bổ sung ⇄ Đã nộp) → Đang duyệt → …`

Sau "Đang duyệt" là các trạng thái của bước phỏng vấn/kiểm tra và kết quả, thiết kế ở story sau. Chỉ trạng thái "Nháp" và "Cần bổ sung" cho phép thực tập sinh sửa hồ sơ.

### Quy tắc nghiệp vụ

- Chưa xác thực email thì không được nộp hồ sơ.
- Mỗi tài khoản chỉ có một hồ sơ cho mỗi chương trình.
- Hết hạn nhận hồ sơ của chương trình thì không nộp được; hồ sơ nháp còn dở không còn nộp được nữa.
- Giới hạn định dạng và dung lượng file (ví dụ PDF/Word cho CV, PDF/ảnh cho giấy tờ).

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Email đã đăng ký:** báo rõ và gợi ý đăng nhập hoặc quên mật khẩu.
- **Liên kết xác thực hết hạn hoặc đã dùng:** cho gửi lại liên kết mới, liên kết cũ vô hiệu.
- **Chương trình đóng khi đang soạn nháp:** cho xem nội dung nháp nhưng không cho nộp, thông báo rõ lý do.
- **Tải file lỗi** (sai định dạng, quá dung lượng, đứt kết nối): chỉ file đó báo lỗi, phần form đã nhập không bị mất.
- **Nhấn nộp hai lần hoặc mở hai tab:** chỉ tạo một hồ sơ cho mỗi chương trình.
- **Thiếu trường hoặc giấy tờ bắt buộc:** chặn nộp và chỉ ra chính xác mục còn thiếu.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Email hợp lệ, chưa tồn tại, đăng ký xong thì nhận email xác thực và chưa nộp được hồ sơ. |
| 2 | Tài khoản đã xác thực chọn chương trình đang nhận hồ sơ thì có hồ sơ nháp; thoát ra vào lại vẫn còn dữ liệu. |
| 3 | Hồ sơ đủ thông tin, bấm nộp thì chuyển "Đã nộp", khóa sửa và có email xác nhận. |
| 4 | Hồ sơ thiếu mục bắt buộc thì không nộp được và báo đúng mục thiếu. |
| 5 | Chương trình đã đóng thì không tạo mới và không nộp được hồ sơ. |
| 6 | Cùng tài khoản, cùng chương trình thì chỉ có một hồ sơ. |

### Yêu cầu phi chức năng tối thiểu

- Mật khẩu lưu dạng băm, truyền qua HTTPS.
- Thực tập sinh chỉ xem được hồ sơ của chính mình (giấy tờ như bảng điểm là dữ liệu cá nhân).
- Giới hạn số lần gửi lại email xác thực để tránh lạm dụng.

### Ngoài phạm vi (để story sau)

- Quên/đổi mật khẩu (nên làm sớm vì rất gần với đăng ký).
- Phía Bộ phận phụ trách: tạo chương trình, duyệt hồ sơ.
- Phỏng vấn/kiểm tra và thông báo kết quả.

## 4. Cập nhật theo story "HR tạo chương trình thực tập"

- **Thuật ngữ:** "đợt/vị trí tuyển" thống nhất thành "chương trình"; mỗi chương trình thuộc một phòng ban.
- **Form nộp hồ sơ:** danh sách giấy tờ bắt buộc/tùy chọn hiển thị theo từng chương trình (form thông tin và CV cố định cho mọi chương trình).
- **Kiểm tra khi nộp:** theo yêu cầu **hiện hành** của chương trình; yêu cầu giấy tờ bị khóa khi chương trình đã có hồ sơ đã nộp.
- **Trạng thái chương trình:** thực tập sinh chỉ nộp được vào chương trình "Đang nhận hồ sơ", trạng thái tự tính theo ngày mở/đóng.