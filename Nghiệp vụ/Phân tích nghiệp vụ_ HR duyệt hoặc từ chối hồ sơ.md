# Phân tích nghiệp vụ: HR duyệt hoặc từ chối hồ sơ

**User story:** Là HR, tôi muốn duyệt hoặc từ chối hồ sơ để chọn ứng viên phù hợp.

**Bối cảnh:** Story này nối tiếp story "Đăng ký tài khoản và nộp hồ sơ thực tập". Quy trình tổng thể: Đăng ký tài khoản → Nộp hồ sơ → **Duyệt hồ sơ** → Phỏng vấn/kiểm tra → Nhận hoặc từ chối. Story này bao phủ bước Duyệt hồ sơ.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **HR (Bộ phận phụ trách chương trình):** lọc danh sách, xem hồ sơ, duyệt hoặc từ chối. Chưa phân biệt nhiều vai trò HR.
- **Thực tập sinh:** chỉ nhận email kết quả, không tương tác trong story này.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Danh sách hồ sơ | Màn hình làm việc chính của HR, hiển thị các hồ sơ đã nộp kèm thông tin cần để quyết định. |
| Bộ lọc | Chương trình/phòng ban, trạng thái hồ sơ, trường, chuyên ngành, năm học, GPA, ngày nộp, tìm theo tên/email. |
| Quyết định | Duyệt hoặc Từ chối; ghi lại người quyết định, thời điểm và lý do (với từ chối). |
| Email kết quả | Gửi cho thực tập sinh theo lịch cố định hằng ngày sau khi quyết định được xác nhận. Email từ chối nêu lý do của riêng hồ sơ đó. |

### Quyết định thiết kế

- HR làm việc theo **lọc và duyệt/từ chối hàng loạt** trên danh sách.
- Từ chối **bắt buộc có lý do riêng cho từng hồ sơ** và lý do được gửi trong email cho ứng viên. Duyệt không cần lý do.
- Khi từ chối nhiều hồ sơ, HR có thể nhập một lý do rồi áp dụng cho các dòng đã chọn để điền sẵn, sau đó sửa riêng từng dòng.

### Ràng buộc đối với story trước

Form nộp hồ sơ phải thu **trường, chuyên ngành, năm học, GPA** thành các trường dữ liệu có cấu trúc để lọc được, không chỉ nằm trong file CV.

## 2. Luồng nghiệp vụ

1. HR vào danh sách hồ sơ, chọn chương trình. Mặc định hiển thị các hồ sơ đã nộp (hồ sơ nháp không hiện cho HR).
2. HR lọc và tìm theo các tiêu chí đã chốt, có thể mở chi tiết từng hồ sơ để xem form và giấy tờ đính kèm. Lần mở đầu tiên chuyển hồ sơ sang "Đang duyệt".
3. HR chọn nhiều hồ sơ bằng ô đánh dấu. Nút "chọn tất cả" chỉ áp dụng cho kết quả lọc hiện tại và hiển thị rõ số lượng.
4. HR chọn hành động:
   - **Duyệt:** màn xác nhận nêu số lượng và danh sách hồ sơ. Xác nhận xong, hồ sơ chuyển "Đã duyệt".
   - **Từ chối:** màn hình có một dòng lý do cho mỗi hồ sơ (có nút điền sẵn). Hồ sơ nào thiếu lý do thì chặn xác nhận. Xác nhận xong, hồ sơ chuyển "Bị từ chối".
5. Hệ thống ghi nhận người quyết định, thời điểm, lý do và tạo thông báo chờ gửi; email được gửi cho từng thực tập sinh theo lịch cố định hằng ngày.

### Vòng đời hồ sơ (phần thuộc story này)

`Đã nộp → Đang duyệt → Đã duyệt | Bị từ chối`

"Đã duyệt" nối sang phỏng vấn/kiểm tra ở story sau. "Bị từ chối" chỉ là trạng thái cuối sau khi email đã gửi; trước đó HR còn thu hồi/sửa được.

### Quy tắc nghiệp vụ

- Chỉ hồ sơ "Đã nộp" hoặc "Đang duyệt" mới quyết định được.
- Trạng thái được kiểm tra lại **lúc xác nhận**, không dựa vào danh sách đang hiển thị. Hồ sơ đã có quyết định (do HR khác xử lý trước, hoặc danh sách cũ) bị bỏ qua và báo lại cho HR; các hồ sơ còn lại vẫn xử lý.
- Quyết định thu hồi/sửa được cho tới trước giờ chốt lô gửi email (xem tài liệu "Hệ thống gửi email thông báo kết quả"); sau khi email đã gửi thì không hoàn tác.
- Gửi email lỗi không làm mất quyết định: hệ thống thử lại và HR thấy được trạng thái gửi.
- Mọi quyết định được lưu nhật ký (ai, khi nào, lý do).

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Hai HR cùng xử lý một hồ sơ:** người xác nhận sau bị bỏ qua hồ sơ đó và nhận thông báo rõ; các hồ sơ còn lại trong lô vẫn được xử lý.
- **Thiếu lý do khi từ chối:** chặn xác nhận và đánh dấu đúng dòng còn thiếu.
- **Chọn nhầm số lượng lớn:** màn xác nhận luôn nêu số lượng và hành động (ví dụ "Từ chối 42 hồ sơ") để HR kịp nhận ra sai sót trước khi email được gửi.
- **Bộ lọc không có kết quả:** hiển thị danh sách trống kèm gợi ý xóa bộ lọc, không cho thao tác hàng loạt.
- **Gửi email lỗi** (hộp thư đầy, địa chỉ sai): quyết định vẫn được ghi nhận, hệ thống thử lại, HR thấy hồ sơ nào chưa gửi được.
- **Hồ sơ thiếu dữ liệu cấu trúc** (ví dụ chưa có GPA): vẫn hiển thị; khi lọc theo tiêu chí đó thì không lọt vào kết quả, kèm lưu ý để HR không bỏ sót ứng viên.
- **Mất kết nối giữa chừng khi xác nhận:** thao tác an toàn khi bấm lại, không tạo quyết định hay email trùng.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR lọc theo chương trình/phòng ban, trạng thái, trường, chuyên ngành, năm học, GPA, ngày nộp hoặc tìm theo tên/email thì danh sách chỉ hiển thị hồ sơ khớp. |
| 2 | HR chọn nhiều hồ sơ và xác nhận Duyệt thì tất cả chuyển "Đã duyệt" và mỗi thực tập sinh nhận một email. |
| 3 | HR chọn nhiều hồ sơ để Từ chối mà có hồ sơ chưa có lý do thì không xác nhận được và dòng thiếu được đánh dấu. |
| 4 | HR từ chối đủ lý do thì các hồ sơ chuyển "Bị từ chối" và mỗi thực tập sinh nhận email nêu đúng lý do của hồ sơ mình. |
| 5 | Hồ sơ đã có quyết định thì không bị quyết định lại, và HR được báo hồ sơ nào bị bỏ qua. |
| 6 | Mỗi quyết định có nhật ký gồm người thực hiện, thời điểm và lý do. |
| 7 | Hồ sơ nháp không bao giờ xuất hiện trong danh sách của HR. |

### Yêu cầu phi chức năng tối thiểu

- Chỉ tài khoản HR mới truy cập được danh sách và thao tác duyệt/từ chối.
- Danh sách phản hồi tốt khi lọc trên số lượng hồ sơ lớn; xử lý theo lô để không treo khi xác nhận nhiều hồ sơ.
- Email gửi bất đồng bộ, không làm HR phải chờ.

### Ngoài phạm vi (để story sau)

- Yêu cầu bổ sung (chuyển hồ sơ sang "Cần bổ sung").
- Hoàn tác/đổi quyết định **sau khi email đã gửi** (thu hồi/sửa trước giờ gửi đã nằm trong story gửi email).
- Phỏng vấn/kiểm tra.
- Phân quyền nhiều vai trò HR.
- Xuất báo cáo/thống kê.
- Quản trị mẫu email.

## 4. Cập nhật theo story "HR tạo chương trình thực tập"

- **Thuật ngữ:** "đợt/vị trí" thống nhất thành "chương trình"; bộ lọc thêm phòng ban của chương trình.
- **Cảnh báo chỉ tiêu:** nếu thao tác duyệt làm tổng "Đã duyệt + Được nhận" của chương trình đạt hoặc vượt chỉ tiêu (tính cả lô khi làm hàng loạt), hệ thống nêu số liệu và HR xác nhận mới tiếp tục. Chỉ cảnh báo, không chặn.
- **Liên kết:** từ chi tiết chương trình có liên kết mở sẵn danh sách hồ sơ đã lọc theo chương trình đó.