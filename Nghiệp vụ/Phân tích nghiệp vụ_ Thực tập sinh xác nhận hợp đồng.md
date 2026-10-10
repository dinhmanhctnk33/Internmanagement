# Phân tích nghiệp vụ: Thực tập sinh xác nhận hợp đồng trên hệ thống

**User story:** Là thực tập sinh, tôi muốn xác nhận hợp đồng trên hệ thống để hoàn tất thủ tục.

**Bối cảnh:** Story này nối tiếp "HR tải lên hợp đồng thực tập". Quy trình tổng thể: … → Nhận hoặc từ chối → Quản lý hợp đồng → **Thực tập sinh xác nhận hợp đồng**. Story này **thay đổi một số quyết định ở tài liệu hợp đồng** (thực tập sinh nay có truy cập; nhãn trạng thái ký đổi thành trạng thái xác nhận); xem mục 4.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Thực tập sinh:** xem (và tải) hợp đồng của chính mình, xác nhận, hoặc gửi yêu cầu chỉnh kèm lý do.
- **HR:** xử lý yêu cầu chỉnh bằng cách xóa rồi tải bản mới (hoặc sửa thông tin nếu chỉ sai ngày); vẫn đặt được "Đã xác nhận" thủ công cho hợp đồng ký giấy.
- **Hệ thống:** ghi nhận xác nhận, quản lý trạng thái, ghi nhật ký.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Trạng thái hợp đồng | *Chưa xác nhận*, *Đã xác nhận*, *Yêu cầu chỉnh*. Nhãn cũ "Chưa ký/Đã ký" đổi thành "Chưa xác nhận/Đã xác nhận". |
| Bản ghi xác nhận | Ai, khi nào, nguồn (thực tập sinh hoặc HR) và gắn với đúng file hợp đồng. Đây là **ghi nhận sự đồng ý, không phải chữ ký có giá trị pháp lý**. |
| Yêu cầu chỉnh | Lý do do thực tập sinh nhập, kèm thời điểm gửi. HR thấy số hợp đồng đang chờ xử lý ở đầu danh sách và lọc được theo trạng thái. |

### Quyết định thiết kế

- "Xác nhận" là **bấm đồng ý nội dung hợp đồng** (ghi nhận sự đồng ý, không phải chữ ký).
- Xác nhận của thực tập sinh **tự chuyển hợp đồng sang "Đã xác nhận"**; nhãn đổi từ "Đã ký" cho khớp bản chất.
- Có nút **"Không đồng ý/yêu cầu chỉnh"** kèm lý do bắt buộc, gửi cho HR.
- Luồng yêu cầu chỉnh theo hướng **trạng thái "Yêu cầu chỉnh" + dấu hiệu trong danh sách của HR** (không thông báo chủ động cho HR, không trao đổi nhiều lượt).

### Giả định đã xác nhận

1. Thực tập sinh phải **mở xem nội dung hợp đồng** thì nút "Xác nhận" mới dùng được.
2. Xác nhận gắn với **đúng file đã hiển thị**. Nếu HR xóa hợp đồng rồi tải lại, xác nhận cũ mất cùng file và hợp đồng mới bắt đầu ở "Chưa xác nhận".
3. **"Hoàn tất thủ tục" nghĩa là hợp đồng ở "Đã xác nhận"**; story này không thêm bước nào khác.

### Phạm vi

Từ lúc HR tải hợp đồng lên đến khi hợp đồng "Đã xác nhận", gồm cả vòng yêu cầu chỉnh và xử lý.

## 2. Luồng nghiệp vụ

1. **Vào hợp đồng.** Thực tập sinh đăng nhập, vào mục "Hợp đồng của tôi". Nếu chưa có thì hiển thị "Chưa có hợp đồng"; nếu có thì hiển thị trạng thái và nút xem.
2. **Xem.** Thực tập sinh mở xem nội dung hợp đồng và tải về được.
3. **Xác nhận.** Nút "Xác nhận" chỉ bật sau khi đã mở xem. Bấm vào, hệ thống hiện một bước chốt ngắn ("Tôi đã đọc và đồng ý nội dung hợp đồng này"). Xác nhận xong, hợp đồng sang "Đã xác nhận", ghi nhận ai, khi nào, nguồn là thực tập sinh, và file nào. Màn hình báo đã hoàn tất thủ tục.
4. **Không đồng ý.** Thực tập sinh nhập lý do (bắt buộc), hợp đồng sang "Yêu cầu chỉnh". Họ thấy "Đang chờ HR xử lý" kèm lý do của mình và chưa xác nhận được.
5. **HR xử lý.** HR thấy dấu hiệu ở đầu danh sách, đọc lý do rồi chọn: xóa và tải bản mới, sửa thông tin, hoặc **đóng yêu cầu** nếu không cần chỉnh (đã trao đổi ngoài hệ thống). Cả ba cách đều đưa hợp đồng về "Chưa xác nhận" để thực tập sinh xác nhận lại.

### Vòng đời hợp đồng (cập nhật)

`Chưa có hợp đồng → Chưa xác nhận → Đã xác nhận` `Chưa xác nhận → Yêu cầu chỉnh → Chưa xác nhận`

HR vẫn đặt thủ công được giữa "Chưa xác nhận" và "Đã xác nhận" (cho hợp đồng ký giấy). Xóa hợp đồng đưa về "Chưa có hợp đồng".

### Quy tắc nghiệp vụ

- Chỉ xác nhận được khi hợp đồng ở "Chưa xác nhận" và thực tập sinh đã mở xem.
- Thực tập sinh chỉ thấy hợp đồng của chính mình.
- Lý do yêu cầu chỉnh là bắt buộc.
- Thực tập sinh không tự hủy xác nhận. Muốn thay đổi thì liên hệ HR, và HR chuyển lại được.
- Bấm xác nhận hai lần hoặc mở hai tab chỉ ghi một xác nhận.
- Nhật ký ghi mọi bước: xác nhận (kèm nguồn), yêu cầu chỉnh, đóng yêu cầu. Khi HR xóa hợp đồng, nhật ký giữ lại thông tin xác nhận đã có.
- HR sửa ngày bắt đầu/kết thúc **sau khi** thực tập sinh đã xác nhận: giữ nguyên trạng thái, HR được cảnh báo, nhật ký ghi giá trị cũ và mới.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **HR xóa hoặc thay hợp đồng khi thực tập sinh đang xem:** lần thao tác kế tiếp (xác nhận hoặc từ chối) báo "hợp đồng đã thay đổi, vui lòng tải lại và xem bản mới"; không ghi nhận xác nhận cho file đã không còn.
- **Thực tập sinh bấm xác nhận hai lần hoặc mở hai tab:** chỉ ghi một xác nhận, lần còn lại báo hợp đồng đã được xác nhận.
- **HR đặt "Đã xác nhận" thủ công đúng lúc thực tập sinh đang bấm xác nhận:** chỉ một xác nhận được ghi, lần đến sau bị bỏ qua và người bấm được báo hợp đồng đã xác nhận.
- **Gửi yêu cầu chỉnh khi lý do trống hoặc chỉ có khoảng trắng:** chặn gửi, đánh dấu ô lý do.
- **Gửi yêu cầu chỉnh hai lần liên tiếp:** chỉ ghi một yêu cầu, lần sau báo "đang chờ HR xử lý".
- **Mất kết nối giữa chừng khi xác nhận:** thao tác an toàn khi bấm lại, không tạo xác nhận trùng và không để hợp đồng ở trạng thái dở dang.
- **Thực tập sinh chưa có hợp đồng, hoặc không còn "Được nhận":** thấy "Chưa có hợp đồng" thay vì lỗi; nếu đã có hợp đồng từ trước thì vẫn thấy hợp đồng của mình.
- **Người dùng cố mở hợp đồng của người khác bằng cách đổi đường dẫn:** bị từ chối, không lộ sự tồn tại của hợp đồng đó.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Thực tập sinh có hợp đồng "Chưa xác nhận" thì thấy hợp đồng của mình, xem và tải được; chưa mở xem thì nút "Xác nhận" chưa dùng được. |
| 2 | Thực tập sinh đã mở xem, bấm xác nhận và chốt thì hợp đồng sang "Đã xác nhận", và nhật ký ghi ai, khi nào, nguồn là thực tập sinh, và file nào. |
| 3 | Thực tập sinh gửi yêu cầu chỉnh kèm lý do thì hợp đồng sang "Yêu cầu chỉnh" và thực tập sinh thấy "Đang chờ HR xử lý" cùng lý do của mình. |
| 4 | Yêu cầu chỉnh thiếu lý do thì không gửi được. |
| 5 | Hợp đồng ở "Yêu cầu chỉnh" thì thực tập sinh không xác nhận được và không gửi thêm yêu cầu. |
| 6 | HR thấy số hợp đồng "Yêu cầu chỉnh" ở đầu danh sách và lọc được theo trạng thái; mở ra đọc được lý do. |
| 7 | HR xóa và tải bản mới, hoặc sửa thông tin, hoặc đóng yêu cầu thì hợp đồng về "Chưa xác nhận" và thực tập sinh xác nhận lại được. |
| 8 | HR xóa hợp đồng đã xác nhận thì xác nhận mất cùng file, hợp đồng mới bắt đầu ở "Chưa xác nhận", nhật ký vẫn giữ thông tin xác nhận trước đó. |
| 9 | HR sửa ngày sau khi đã xác nhận thì trạng thái giữ nguyên, HR được cảnh báo và nhật ký ghi giá trị cũ, giá trị mới. |
| 10 | HR đặt "Đã xác nhận" thủ công thì nhật ký ghi nguồn là HR. |
| 11 | Thực tập sinh không xem được và không xác nhận được hợp đồng của người khác. |
| 12 | Mọi bước xác nhận, yêu cầu chỉnh, đóng yêu cầu đều có nhật ký. |

### Yêu cầu phi chức năng tối thiểu

- Chỉ thực tập sinh sở hữu hợp đồng và HR truy cập được file; không có đường dẫn công khai.
- Xác nhận phải ghi nhận bền vững: không mất khi hệ thống khởi động lại giữa chừng.
- Thông tin ghi nhận (ai, khi nào, file nào) không sửa được từ giao diện.

### Ngoài phạm vi (để story sau)

- Thông báo chủ động cho HR khi có yêu cầu chỉnh.
- Email nhắc thực tập sinh có hợp đồng cần xác nhận (hiện thực tập sinh thấy khi đăng nhập).
- Hạn xác nhận và nhắc quá hạn.
- Ký điện tử có giá trị pháp lý.
- Trao đổi nhiều lượt giữa HR và thực tập sinh.
- Thực tập sinh tự hủy xác nhận.

## 4. Tác động lên tài liệu "HR tải lên hợp đồng thực tập"

- **Nhãn trạng thái:** "Chưa ký/Đã ký" đổi thành "Chưa xác nhận/Đã xác nhận", và thêm trạng thái "Yêu cầu chỉnh".
- **Tác nhân:** thực tập sinh không còn là "không tương tác"; nay xem, tải và xác nhận hợp đồng của chính mình.
- **Phạm vi:** "thực tập sinh tự xem/tải hợp đồng của mình" chuyển từ ngoài phạm vi sang đã làm ở story này.
- **Xóa hợp đồng:** xác nhận mất cùng file; nhật ký giữ lại thông tin xác nhận trước đó.
- **Trạng thái ký thủ công:** HR vẫn đặt thủ công "Đã xác nhận" cho hợp đồng ký giấy; nhật ký ghi nguồn.