# Phân tích nghiệp vụ: HR tải lên hợp đồng thực tập để quản lý giấy tờ

**User story:** Là HR, tôi muốn tải lên hợp đồng thực tập để quản lý giấy tờ.

**Bối cảnh:** Story này nối tiếp các story trước. Quy trình tổng thể: Đăng ký tài khoản → Nộp hồ sơ → Duyệt hồ sơ → Phỏng vấn/kiểm tra → Nhận hoặc từ chối → **Quản lý hợp đồng** → Thực tập sinh xác nhận hợp đồng. Hợp đồng chỉ có ý nghĩa với thực tập sinh đã ở trạng thái "Được nhận" (kết quả từ story phỏng vấn/kiểm tra, hiện mới chừa chỗ nối).

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **HR:** tải lên, xem, tải về, cập nhật thông tin và xóa hợp đồng.
- **Hệ thống:** lưu file, kiểm tra định dạng và dung lượng, ghi nhật ký.
- **Thực tập sinh:** xem, tải và xác nhận hợp đồng của chính mình (xem tài liệu "Thực tập sinh xác nhận hợp đồng").

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Hợp đồng thực tập | Mỗi thực tập sinh có tối đa một hợp đồng, gồm một file kèm thông tin có cấu trúc: *ngày bắt đầu*, *ngày kết thúc*, *trạng thái xác nhận* (Chưa xác nhận / Đã xác nhận / Yêu cầu chỉnh). HR đặt thủ công cho hợp đồng ký giấy; thực tập sinh tự xác nhận trên hệ thống. |
| Nhật ký | Ghi ai, khi nào, làm gì (tải lên, sửa thông tin, đổi trạng thái xác nhận, xóa). Với thao tác xóa còn ghi tên file, vì đây là dấu vết duy nhất còn lại. |

### Quyết định thiết kế

- Mức quản lý: **lưu trữ file + thông tin hợp đồng** (ngày bắt đầu/kết thúc, trạng thái xác nhận).
- **Một hợp đồng duy nhất** cho mỗi thực tập sinh.
- **Không thay file**: khi tải nhầm hoặc có bản đã ký thay bản chưa ký thì xóa rồi tải lại.
- **Xóa cứng + nhật ký.** Rủi ro đã chấp nhận: xóa nhầm bản đã ký sẽ không lấy lại được từ hệ thống.

### Giả định đã xác nhận

1. Chỉ tải lên hợp đồng cho thực tập sinh có hồ sơ ở trạng thái **"Được nhận"**; HR chọn thực tập sinh từ danh sách đã nhận.
2. File hợp đồng là **PDF**, dung lượng tối đa cấu hình được (ví dụ 10 MB).
3. **Thông tin hợp đồng sửa được độc lập với file**: HR đổi ngày hoặc trạng thái xác nhận mà không cần xóa file.

### Phạm vi

Từ lúc thực tập sinh được nhận đến khi hợp đồng được lưu trữ, tra cứu, cập nhật thông tin hoặc xóa.

## 2. Luồng nghiệp vụ

1. **Danh sách.** HR xem danh sách thực tập sinh đã được nhận. Mỗi dòng cho biết tình trạng hợp đồng: *Chưa có hợp đồng*, *Chưa xác nhận* hoặc *Đã xác nhận* (kèm ngày kết thúc). HR tìm theo tên/email và lọc theo tình trạng hợp đồng.
2. **Tải lên.** HR chọn thực tập sinh chưa có hợp đồng, chọn file PDF, nhập ngày bắt đầu, ngày kết thúc (gợi ý sẵn từ thời gian thực tập của chương trình thực tập sinh đã tham gia, sửa được) và trạng thái xác nhận (mặc định "Chưa xác nhận"). Hệ thống kiểm tra, lưu và ghi nhật ký.
3. **Xem/tải về.** HR mở hoặc tải hợp đồng của thực tập sinh.
4. **Cập nhật thông tin.** HR sửa ngày hoặc đổi trạng thái xác nhận, không đụng đến file. Nhật ký ghi giá trị cũ và mới.
5. **Xóa.** HR bấm xóa, xác nhận một lần (màn xác nhận nêu tên thực tập sinh và tên file), file bị xóa vĩnh viễn. Thực tập sinh quay về "Chưa có hợp đồng" và có thể tải lại. Nhật ký ghi tên file cùng ngày và trạng thái xác nhận tại thời điểm xóa.

### Vòng đời hợp đồng

`Chưa có hợp đồng → Chưa xác nhận ⇄ Đã xác nhận → (xóa) → Chưa có hợp đồng` `Chưa xác nhận → Yêu cầu chỉnh → Chưa xác nhận` (vòng yêu cầu chỉnh của thực tập sinh, xem tài liệu xác nhận)

Trạng thái xác nhận chuyển được cả hai chiều để HR sửa khi nhập nhầm; mọi lần đổi đều có nhật ký.

### Quy tắc nghiệp vụ

- Chỉ thực tập sinh ở trạng thái "Được nhận" mới tải lên được hợp đồng.
- Mỗi thực tập sinh tối đa một hợp đồng. Khi đã có, hệ thống chặn tải thêm và báo phải xóa hợp đồng cũ trước.
- Ngày kết thúc không được trước ngày bắt đầu.
- File phải là PDF và trong giới hạn dung lượng.
- Xóa là vĩnh viễn, không khôi phục (rủi ro đã chấp nhận). Nếu hợp đồng đã xác nhận thì xác nhận mất cùng file; nhật ký giữ lại thông tin xác nhận đã có.
- Nhật ký không sửa hay xóa được từ giao diện.
- Chỉ HR và thực tập sinh sở hữu hợp đồng truy cập được; file không có đường dẫn công khai.

### Điểm để ngoài phạm vi, cần lưu ý

Nếu thực tập sinh sau này không còn ở trạng thái "Được nhận" (ví dụ rút lui), hợp đồng đã tải lên vẫn giữ nguyên và HR tự quyết định có xóa hay không. Hệ thống không tự xóa.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **File sai định dạng hoặc quá dung lượng:** từ chối, nêu rõ lý do và giới hạn cho phép; thông tin đã nhập trên form không bị mất.
- **File PDF hỏng hoặc không mở được:** báo lỗi và không tạo hợp đồng.
- **Tải lên bị đứt giữa chừng:** không để lại hợp đồng dở dang; thực tập sinh vẫn ở "Chưa có hợp đồng".
- **Bấm tải lên hai lần hoặc hai HR cùng tải cho một thực tập sinh:** chỉ một hợp đồng được tạo, lần còn lại báo "thực tập sinh đã có hợp đồng".
- **Ngày kết thúc trước ngày bắt đầu, hoặc thiếu ngày:** chặn lưu và đánh dấu đúng trường sai.
- **Xóa trong lúc HR khác đang xem hoặc tải:** thao tác xóa vẫn thành công; người đang xem nhận thông báo "hợp đồng không còn tồn tại" ở lần thao tác kế tiếp.
- **Xóa nhầm bản đã ký:** không khôi phục được. Màn xác nhận luôn nêu rõ tên thực tập sinh, tên file và trạng thái xác nhận hiện tại để giảm khả năng nhầm.
- **Danh sách trống hoặc bộ lọc không có kết quả:** hiển thị trống kèm gợi ý xóa bộ lọc.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR chọn thực tập sinh "Được nhận" chưa có hợp đồng, tải PDF hợp lệ kèm ngày hợp lệ thì hợp đồng được lưu và hiển thị đúng trạng thái xác nhận đã chọn. |
| 2 | Thực tập sinh chưa "Được nhận" thì không tải lên được hợp đồng. |
| 3 | Thực tập sinh đã có hợp đồng thì hệ thống chặn tải thêm và báo phải xóa hợp đồng cũ trước. |
| 4 | File không phải PDF hoặc vượt dung lượng thì bị từ chối kèm lý do, và form giữ nguyên dữ liệu đã nhập. |
| 5 | Ngày kết thúc trước ngày bắt đầu thì không lưu được. |
| 6 | HR đổi ngày hoặc trạng thái xác nhận thì file giữ nguyên, thông tin mới hiển thị ngay và nhật ký ghi giá trị cũ, giá trị mới. |
| 7 | HR xác nhận xóa thì file bị xóa vĩnh viễn, thực tập sinh quay về "Chưa có hợp đồng" và tải lại được. |
| 8 | Nhật ký của thao tác xóa có tên file, ngày và trạng thái xác nhận tại thời điểm xóa, người xóa và thời điểm xóa. |
| 9 | Chỉ HR và thực tập sinh sở hữu hợp đồng xem và tải được; người dùng khác hoặc truy cập không đăng nhập đều bị từ chối. |
| 10 | HR tìm theo tên/email và lọc theo tình trạng hợp đồng thì danh sách chỉ hiển thị thực tập sinh khớp. |

### Yêu cầu phi chức năng tối thiểu

- File hợp đồng được lưu an toàn và chỉ truy cập qua phiên đăng nhập của HR hoặc của thực tập sinh sở hữu hợp đồng, không có liên kết công khai.
- Truyền file qua HTTPS.
- Việc tải lên không làm mất dữ liệu nếu hệ thống khởi động lại giữa chừng.

### Ngoài phạm vi (để story sau)

- Ký điện tử.
- Phụ lục/gia hạn.
- Nhắc hạn hợp đồng sắp hết.
- Tạo hợp đồng từ mẫu.
- Lưu lịch sử bản cũ hoặc khôi phục sau khi xóa.
- Tự xử lý hợp đồng khi thực tập sinh không còn "Được nhận".
- Hỗ trợ định dạng khác ngoài PDF.

## 4. Cập nhật theo story "Thực tập sinh xác nhận hợp đồng"

- **Nhãn trạng thái:** "Chưa ký/Đã ký" đã đổi thành "Chưa xác nhận/Đã xác nhận", và thêm trạng thái "Yêu cầu chỉnh" (vòng xử lý nằm ở tài liệu xác nhận).
- **Tác nhân:** thực tập sinh nay xem, tải và xác nhận hợp đồng của chính mình; HR vẫn đặt "Đã xác nhận" thủ công cho hợp đồng ký giấy, nhật ký ghi nguồn.
- **Phạm vi:** "thực tập sinh tự xem/tải hợp đồng của mình" đã được đưa vào làm; ký điện tử có giá trị pháp lý vẫn ngoài phạm vi.
- **Xóa hợp đồng:** xác nhận mất cùng file; nhật ký giữ lại thông tin xác nhận trước đó.

## 5. Cập nhật theo story "HR tạo chương trình thực tập"

- **Ngày hợp đồng:** ngày bắt đầu/kết thúc gợi ý sẵn từ thời gian thực tập của chương trình mà thực tập sinh được nhận; HR vẫn sửa được.
- **Chỉ tiêu:** việc quyết định nhận có cảnh báo khi vượt chỉ tiêu của chương trình (chi tiết ở tài liệu duyệt hồ sơ và tài liệu chương trình).

## 6. Cập nhật theo story "HR phân công thực tập sinh cho mentor"

- **Danh sách thực tập sinh "Được nhận" của HR** có thêm cột mentor (hoặc "Chưa có mentor") để tra cứu cùng với tình trạng hợp đồng.

## 7. Cập nhật theo story "Thực tập sinh xem lịch thực tập cá nhân"

- **Ngày hợp đồng là nguồn ưu tiên của lịch:** ngày bắt đầu/kết thúc của hợp đồng là nguồn của hai mốc "bắt đầu thực tập" và "kết thúc thực tập" trên lịch của thực tập sinh. HR sửa ngày hợp đồng thì mốc đổi theo ở lần xem sau; xóa hợp đồng thì mốc quay về lấy theo chương trình.

## 8. Cập nhật theo story "Thực tập sinh check-in/check-out"

- **Kỳ được chấm công:** ngày bắt đầu/kết thúc hợp đồng (nguồn ưu tiên, rồi đến chương trình) giới hạn kỳ thực tập sinh được check-in/check-out. HR sửa hoặc xóa hợp đồng thì kỳ được chấm công đổi từ lần chấm sau; các bản ghi đã có không bị xóa.