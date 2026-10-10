# Phân tích nghiệp vụ: HR tạo chương trình thực tập theo phòng ban

**User story:** Là HR, tôi muốn tạo chương trình thực tập theo phòng ban để tổ chức kế hoạch.

**Bối cảnh:** Story này định nghĩa thực thể mà các story trước chỉ tham chiếu: "đợt/vị trí tuyển" mà mọi hồ sơ gắn vào. Từ nay thuật ngữ thống nhất là **chương trình thực tập**; mỗi chương trình thuộc một phòng ban và hồ sơ nộp trực tiếp vào đó. Quy trình tổng thể: **Tạo chương trình** → Đăng ký tài khoản → Nộp hồ sơ → Duyệt hồ sơ → Phỏng vấn/kiểm tra → Nhận hoặc từ chối → Hợp đồng.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **HR:** tạo, sửa, đóng sớm chương trình và theo dõi số hồ sơ.
- **Hệ thống:** tự tính trạng thái theo ngày, cảnh báo chỉ tiêu, ghi nhật ký.
- **Thực tập sinh:** không thao tác ở story này, nhưng là người dùng kết quả: họ thấy các chương trình đang nhận hồ sơ và đúng yêu cầu giấy tờ của từng chương trình khi nộp.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Chương trình thực tập | Chính là "đợt/vị trí tuyển" ở các story trước; mỗi chương trình thuộc một phòng ban. Gồm: tên, phòng ban, mô tả, *thời hạn nhận hồ sơ* (ngày mở, ngày đóng), *thời gian thực tập* (ngày bắt đầu, ngày kết thúc), *chỉ tiêu*, *yêu cầu hồ sơ riêng*. |
| Trạng thái | *Chưa mở → Đang nhận hồ sơ → Đã đóng*, tự chuyển theo ngày; HR đóng sớm được. |
| Khóa | Khi đã có hồ sơ **đã nộp**, *yêu cầu hồ sơ riêng* không sửa được nữa; các trường còn lại vẫn sửa được và có nhật ký. |
| Chỉ tiêu | Chỉ **cảnh báo** HR khi duyệt/nhận vượt, không chặn. |

### Quyết định thiết kế

- Chương trình = đợt/vị trí tuyển, một chương trình một phòng ban, hồ sơ nộp trực tiếp vào chương trình.
- Quản lý bốn nhóm thông tin: thời hạn nhận hồ sơ, thời gian thực tập, chỉ tiêu, yêu cầu hồ sơ riêng.
- Chỉ tiêu dùng để **cảnh báo, không chặn**.
- Vòng đời: **trạng thái tự động theo ngày + khóa yêu cầu hồ sơ khi đã có hồ sơ đã nộp**.

### Giả định đã xác nhận

1. **Phòng ban chọn từ một danh mục có sẵn** trong hệ thống (HR không tự gõ tên mới). Việc quản lý danh mục nằm ngoài phạm vi.
2. **"Yêu cầu hồ sơ riêng" chỉ áp dụng cho giấy tờ đính kèm** (bảng điểm, thư giới thiệu, portfolio…): mỗi chương trình đặt giấy tờ nào bắt buộc, giấy tờ nào tùy chọn. Form thông tin và CV cố định cho mọi chương trình.
3. **Chỉ tiêu được đếm theo số hồ sơ "Đã duyệt" và "Được nhận"** của chương trình, vì duyệt đã là bước giữ chỗ. Cảnh báo hiện khi con số này đạt hoặc vượt chỉ tiêu.

### Phạm vi

Từ lúc tạo chương trình đến khi đóng: tạo, sửa, đóng sớm, xem danh sách, theo dõi số hồ sơ so với chỉ tiêu.

## 2. Luồng nghiệp vụ

1. **Danh sách.** HR xem danh sách chương trình: tên, phòng ban, trạng thái, thời hạn nhận hồ sơ, và số hồ sơ so với chỉ tiêu. Lọc theo phòng ban, trạng thái; tìm theo tên.
2. **Tạo.** HR nhập tên, chọn phòng ban, mô tả, ngày mở/đóng nhận hồ sơ, ngày bắt đầu/kết thúc thực tập, chỉ tiêu, và chọn giấy tờ nào bắt buộc, giấy tờ nào tùy chọn. Hệ thống kiểm tra, lưu, ghi nhật ký.
3. **Sửa.** HR sửa các thông tin, trừ *yêu cầu hồ sơ riêng* bị khóa khi đã có hồ sơ đã nộp. Mọi thay đổi có nhật ký giá trị cũ và mới.
4. **Đóng sớm và gia hạn.** Đóng sớm là đặt ngày đóng thành thời điểm hiện tại. Gia hạn hay mở lại là sửa ngày đóng sang tương lai. Không cần cờ riêng, trạng thái luôn tự tính từ ngày.
5. **Theo dõi.** Xem chi tiết chương trình có số liệu (tổng hồ sơ, đã duyệt, được nhận, chỉ tiêu) và liên kết sang danh sách hồ sơ đã lọc sẵn theo chương trình đó.
6. **Cảnh báo chỉ tiêu.** Ở màn duyệt và màn quyết định nhận: nếu thao tác làm tổng "Đã duyệt + Được nhận" đạt hoặc vượt chỉ tiêu (tính cả lô khi làm hàng loạt), hệ thống nêu số liệu và HR xác nhận mới tiếp tục.
7. **Xóa.** Chỉ xóa được chương trình chưa có hồ sơ nào. Đã có thì chỉ đóng.

### Vòng đời chương trình

`Chưa mở → Đang nhận hồ sơ → Đã đóng`

Tự chuyển theo ngày. Hết ngày đóng (đến cuối ngày) mới chuyển "Đã đóng".

### Quy tắc nghiệp vụ

- Ngày đóng không trước ngày mở; ngày kết thúc thực tập không trước ngày bắt đầu; chỉ tiêu là số nguyên dương.
- Tên chương trình không trùng trong cùng phòng ban.
- Thực tập sinh chỉ nộp được vào chương trình "Đang nhận hồ sơ". Hồ sơ nháp ở chương trình đã đóng vẫn xem được nhưng không nộp được.
- Hồ sơ nháp không làm khóa yêu cầu giấy tờ; khi nộp, hệ thống kiểm tra theo yêu cầu **hiện hành** của chương trình.
- Chỉ HR tạo, sửa, đóng, xóa.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Thiếu hoặc sai thông tin khi tạo/sửa** (thiếu tên, chưa chọn phòng ban, ngày đóng trước ngày mở, ngày kết thúc thực tập trước ngày bắt đầu, chỉ tiêu bằng 0 hoặc âm): chặn lưu và đánh dấu đúng trường sai, dữ liệu đã nhập không bị mất.
- **Tên trùng trong cùng phòng ban:** chặn lưu, báo rõ chương trình trùng.
- **Sửa yêu cầu giấy tờ khi đã có hồ sơ đã nộp:** trường bị khóa, giải thích lý do khóa, gợi ý tạo chương trình mới nếu cần yêu cầu khác.
- **HR sửa đúng lúc có hồ sơ vừa được nộp:** hệ thống kiểm tra lại điều kiện khóa tại lúc lưu; nếu đã có hồ sơ nộp thì thay đổi yêu cầu giấy tờ bị từ chối, các thay đổi khác vẫn lưu được.
- **Rút ngắn ngày đóng về trước hôm nay hoặc đóng sớm:** chương trình chuyển "Đã đóng" ngay; hồ sơ nháp được giữ nhưng không nộp được, thực tập sinh đang soạn dở nhận thông báo rõ lý do khi bấm nộp.
- **Gia hạn chương trình đã đóng:** sửa ngày đóng sang tương lai thì chương trình trở lại "Đang nhận hồ sơ"; nhật ký ghi ngày đóng cũ và mới.
- **Xóa chương trình đã có hồ sơ:** từ chối, gợi ý đóng thay vì xóa.
- **Hai HR cùng sửa một chương trình:** lần lưu sau thấy thông báo "chương trình đã được cập nhật", phải tải lại trước khi lưu, tránh ghi đè lặng lẽ.
- **Chỉ tiêu bị sửa thấp hơn số đã duyệt/nhận:** vẫn cho lưu, hiển thị cảnh báo "đang vượt chỉ tiêu" rõ ràng ở chi tiết chương trình.
- **Danh sách trống hoặc bộ lọc không có kết quả:** hiển thị trống kèm gợi ý tạo chương trình hoặc xóa bộ lọc.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR nhập đủ thông tin hợp lệ thì chương trình được tạo, trạng thái đúng theo ngày mở, và nhật ký ghi lại. |
| 2 | Thông tin thiếu hoặc sai thì không lưu được và đúng trường sai được đánh dấu. |
| 3 | Tên chương trình trùng trong cùng phòng ban thì không lưu được. |
| 4 | Đến ngày mở thì chương trình tự chuyển "Đang nhận hồ sơ"; hết ngày đóng thì tự chuyển "Đã đóng". |
| 5 | Thực tập sinh chỉ thấy và nộp được hồ sơ vào chương trình "Đang nhận hồ sơ", và form hiển thị đúng giấy tờ bắt buộc/tùy chọn của chương trình đó. |
| 6 | Chương trình đã có hồ sơ đã nộp thì yêu cầu giấy tờ không sửa được, các thông tin khác vẫn sửa được, và mỗi thay đổi có nhật ký giá trị cũ, mới. |
| 7 | HR đóng sớm thì chương trình chuyển "Đã đóng" ngay và không nộp thêm hồ sơ được. |
| 8 | HR sửa ngày đóng sang tương lai thì chương trình trở lại "Đang nhận hồ sơ". |
| 9 | Chương trình chưa có hồ sơ nào thì xóa được; đã có thì xóa bị từ chối và gợi ý đóng. |
| 10 | HR duyệt hoặc quyết định nhận khiến tổng "Đã duyệt + Được nhận" đạt hoặc vượt chỉ tiêu thì hệ thống cảnh báo kèm số liệu và vẫn cho tiếp tục sau khi HR xác nhận. |
| 11 | Chi tiết chương trình hiển thị đúng số hồ sơ, số đã duyệt, số được nhận so với chỉ tiêu, và liên kết mở danh sách hồ sơ đã lọc theo chương trình. |
| 12 | Chỉ HR tạo, sửa, đóng, xóa được chương trình; thực tập sinh không thao tác được. |

### Yêu cầu phi chức năng tối thiểu

- Trạng thái chương trình tính nhất quán từ ngày mở/đóng, không lệ thuộc vào việc HR có mở hệ thống hay không.
- Mọi thay đổi chương trình có nhật ký (ai, khi nào, giá trị cũ/mới) và không sửa được từ giao diện.
- Danh sách chương trình phản hồi tốt khi có nhiều chương trình qua nhiều kỳ.

### Ngoài phạm vi (để story sau)

- Quản lý danh mục phòng ban.
- Phân quyền HR theo phòng ban.
- Trưởng phòng ban tham gia duyệt.
- Lịch trình chi tiết trong kỳ thực tập.
- Sao chép chương trình từ kỳ trước.
- Báo cáo/thống kê theo chương trình.
- Tùy biến form thông tin cá nhân theo chương trình.

## 4. Tác động lên các tài liệu trước

- **Nộp hồ sơ:** thuật ngữ "đợt/vị trí tuyển" thống nhất thành "chương trình"; form hiển thị danh sách giấy tờ bắt buộc/tùy chọn theo từng chương trình, và hệ thống kiểm tra theo yêu cầu hiện hành lúc nộp.
- **Duyệt hồ sơ:** màn duyệt và màn quyết định nhận có thêm cảnh báo chỉ tiêu; chi tiết chương trình liên kết sang danh sách hồ sơ đã lọc sẵn theo chương trình.
- **Hợp đồng:** ngày bắt đầu/kết thúc của hợp đồng gợi ý từ thời gian thực tập của chương trình mà thực tập sinh đã tham gia; HR vẫn sửa được.

## 5. Cập nhật theo story "HR phân công thực tập sinh cho mentor"

- **Phòng ban và mentor:** thực tập sinh của một chương trình chỉ phân công được cho mentor cùng phòng ban với chương trình đó (mentor do quản trị viên tạo và gán phòng ban). Phòng ban chưa có mentor thì không chia đều được.

## 6. Cập nhật theo story "HR xem báo cáo đi làm và nghỉ phép"

- **Trường mới "thứ làm việc":** mỗi chương trình có tập các thứ trong tuần làm việc (ít nhất một thứ), mặc định thứ Hai đến thứ Sáu; HR đặt khi tạo hoặc sửa chương trình. Sửa được bất kỳ lúc nào và áp dụng cho cả kỳ, nên đổi thứ làm việc sẽ làm báo cáo chuyên cần tính lại cả phần quá khứ. Mọi thay đổi có nhật ký.