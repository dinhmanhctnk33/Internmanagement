# Phân tích nghiệp vụ: HR phân công thực tập sinh cho mentor

**User story:** Là HR, tôi muốn phân công thực tập sinh cho mentor để họ được hướng dẫn.

**Bối cảnh:** Story này nối tiếp các story trước và đưa vào **một vai trò mới: mentor**. Quy trình tổng thể: Tạo chương trình → Đăng ký tài khoản → Nộp hồ sơ → Duyệt hồ sơ → Phỏng vấn/kiểm tra → Nhận hoặc từ chối → Hợp đồng → **Phân công mentor**. Story này dựa vào trạng thái "Được nhận" (kết quả của story phỏng vấn/kiểm tra, hiện mới chừa chỗ nối).

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **HR:** lọc nhóm thực tập sinh, bấm "Chia đều", xem và chỉnh đề xuất, xác nhận; đổi mentor khi cần.
- **Mentor:** đăng nhập và xem các thực tập sinh mình hướng dẫn.
- **Hệ thống:** tạo đề xuất chia đều, áp dụng khi HR xác nhận, ghi nhật ký.
- **Quản trị viên hệ thống:** tạo tài khoản và gán phòng ban cho mentor. *Ngoài phạm vi*, là điều kiện tiên quyết.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Mentor | Người dùng có vai trò mentor, thuộc một phòng ban. Tài khoản do quản trị viên tạo. |
| Phân công | Quan hệ giữa một thực tập sinh và đúng một mentor; một mentor có nhiều thực tập sinh, không giới hạn. |
| Đề xuất chia đều | Kết quả tạm do hệ thống tạo, chưa có hiệu lực cho đến khi HR xác nhận. |
| Nhật ký | Ghi ai phân công hoặc đổi mentor, khi nào, từ mentor nào sang mentor nào. |

### Quyết định thiết kế

- Mentor là người dùng có tài khoản, đăng nhập và xem thực tập sinh mình hướng dẫn.
- Tài khoản mentor do **quản trị viên hệ thống** tạo; story này chỉ chọn từ danh sách mentor có sẵn.
- **Mỗi thực tập sinh một mentor; một mentor nhiều thực tập sinh, không giới hạn.**
- Cách phân công: **hệ thống tự chia đều**, kích hoạt bởi HR, hệ thống **đề xuất**, HR xem/chỉnh từng dòng rồi xác nhận.

### Giả định đã xác nhận

1. Chỉ thực tập sinh ở trạng thái **"Được nhận"** mới được phân công; mentor phải **cùng phòng ban** với chương trình của thực tập sinh. Chia đều chỉ diễn ra giữa các mentor của phòng ban ấy.
2. **Chia đều chỉ áp dụng cho thực tập sinh chưa có mentor**, không tự đổi mentor đã gán. Khi cân bằng, hệ thống tính cả số thực tập sinh mentor đang hướng dẫn, nên mentor có ít người hơn được ưu tiên.
3. Mentor chỉ **xem** (không sửa) thông tin cơ bản và hồ sơ của những thực tập sinh mình hướng dẫn.
4. Thực tập sinh thấy **tên và email mentor** của mình sau khi phân công được xác nhận.

### Phạm vi

Từ lúc có thực tập sinh "Được nhận" đến khi được phân công và mentor xem được thực tập sinh của mình.

## 2. Luồng nghiệp vụ

1. **Danh sách.** HR xem thực tập sinh "Được nhận": tên, chương trình/phòng ban, mentor hiện tại (hoặc "Chưa có mentor"). Lọc theo chương trình, phòng ban, mentor, tình trạng phân công; tìm theo tên/email.
2. **Chia đều.** HR lọc nhóm cần phân công, chọn các dòng (hoặc "chọn tất cả kết quả lọc", chỉ lấy những người chưa có mentor), bấm "Chia đều". Hệ thống tạo **đề xuất**: mỗi thực tập sinh gán cho một mentor của phòng ban tương ứng.
3. **Xem và chỉnh đề xuất.** Bảng thực tập sinh → mentor đề xuất, kèm số thực tập sinh hiện có và số sau khi gán của từng mentor. HR đổi mentor cho từng dòng (chọn trong mentor cùng phòng ban) hoặc bỏ dòng khỏi lô.
4. **Xác nhận.** Màn xác nhận nêu rõ số lượng ("Phân công 18 thực tập sinh cho 4 mentor"). Xác nhận xong, phân công có hiệu lực, nhật ký ghi lại; mentor thấy thực tập sinh của mình, thực tập sinh thấy mentor của mình.
5. **Đổi mentor.** HR chọn một hoặc nhiều thực tập sinh đã có mentor, chọn mentor mới thủ công, xác nhận. Nhật ký ghi mentor cũ → mới. Chia đều không tự đổi người đã có mentor.
6. **Mentor xem.** Mentor đăng nhập, thấy danh sách thực tập sinh mình hướng dẫn (chỉ xem) cùng chương trình và hồ sơ của họ.

### Vòng đời phân công

`Chưa có mentor → Đã phân công (mentor A) ⇄ Đã phân công (mentor B)`

Đề xuất chia đều chỉ là kết quả tạm. Rời màn hình mà chưa xác nhận thì đề xuất bị bỏ, tạo lại được.

### Quy tắc nghiệp vụ

- Chỉ thực tập sinh "Được nhận" mới được phân công; mentor phải cùng phòng ban với chương trình của thực tập sinh.
- Mỗi thực tập sinh đúng một mentor.
- **Cách chia:** ưu tiên mentor đang có ít thực tập sinh nhất (tính cả người đã phân công trước đó); khi bằng nhau thì xoay vòng theo thứ tự cố định để cùng đầu vào cho cùng kết quả.
- Trạng thái được kiểm tra lại **lúc xác nhận**: thực tập sinh vừa được HR khác gán mentor thì bị bỏ qua và báo lại, các dòng còn lại vẫn áp dụng.
- Phòng ban chưa có mentor nào thì không đề xuất được, báo rõ.
- Mentor chỉ thấy thực tập sinh của mình. Chỉ HR được phân công và đổi mentor.
- Mọi phân công và đổi mentor đều có nhật ký.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Phòng ban chưa có mentor nào:** không đề xuất được; hệ thống báo rõ phòng ban nào thiếu mentor và nhắc liên hệ quản trị viên. Các phòng ban khác trong cùng lô vẫn được đề xuất.
- **Nhóm đã lọc gồm nhiều phòng ban:** hệ thống chia riêng theo từng phòng ban, mỗi thực tập sinh chỉ được gán cho mentor của phòng ban mình.
- **Nhóm đã lọc rỗng, hoặc toàn người đã có mentor:** nút "Chia đều" không tạo đề xuất và giải thích lý do.
- **Hai HR cùng phân công một thực tập sinh:** người xác nhận sau bị bỏ qua dòng đó và nhận thông báo rõ; các dòng còn lại vẫn áp dụng.
- **Mentor bị khóa hoặc xóa tài khoản sau khi đã phân công:** phân công vẫn giữ, nhưng danh sách đánh dấu "mentor không còn hoạt động" để HR đổi sang mentor khác; mentor đó không xuất hiện trong lựa chọn mới.
- **Mentor bị khóa ngay khi HR đang xem đề xuất:** lúc xác nhận, dòng nào dùng mentor không còn hoạt động bị bỏ qua và báo lại.
- **HR chỉnh một dòng sang mentor khác phòng ban:** không cho chọn, danh sách mentor chỉ gồm mentor cùng phòng ban.
- **Xác nhận bị đứt giữa chừng hoặc bấm hai lần:** thao tác an toàn khi thử lại, không tạo phân công trùng, không để lô áp dụng dở dang.
- **Thực tập sinh không còn "Được nhận" sau khi đã phân công:** phân công giữ nguyên, HR tự quyết định có đổi hay không; hệ thống không tự gỡ.
- **Mentor cố xem thực tập sinh của người khác bằng cách đổi đường dẫn:** bị từ chối, không lộ sự tồn tại của thực tập sinh đó.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR lọc nhóm thực tập sinh "Được nhận" chưa có mentor, bấm "Chia đều" thì hệ thống tạo đề xuất, mỗi người gán cho một mentor cùng phòng ban và chưa có phân công nào được lưu. |
| 2 | Hai mentor cùng phòng ban có số thực tập sinh hiện có khác nhau thì mentor ít hơn được ưu tiên; khi bằng nhau thì kết quả theo thứ tự cố định và lặp lại được với cùng đầu vào. |
| 3 | Chia đều không đổi mentor của thực tập sinh đã có mentor. |
| 4 | HR đổi mentor ở một dòng đề xuất hoặc bỏ dòng khỏi lô thì đề xuất cập nhật số lượng của từng mentor ngay. |
| 5 | HR xác nhận thì phân công có hiệu lực, nhật ký ghi ai, khi nào, thực tập sinh nào, mentor nào. |
| 6 | Sau khi xác nhận, mentor thấy đúng các thực tập sinh được giao ở chế độ chỉ xem, và thực tập sinh thấy tên, email mentor của mình. |
| 7 | HR đổi mentor cho một hoặc nhiều thực tập sinh đã có mentor thì nhật ký ghi mentor cũ và mới. |
| 8 | Thực tập sinh vừa được HR khác phân công khi đang xác nhận thì bị bỏ qua và báo lại, các dòng còn lại áp dụng bình thường. |
| 9 | Phòng ban không có mentor thì không đề xuất được và có thông báo rõ. |
| 10 | Chỉ mentor cùng phòng ban với chương trình được chọn cho thực tập sinh. |
| 11 | Mentor không xem được thực tập sinh không thuộc mình; người dùng không phải HR không phân công được. |

### Yêu cầu phi chức năng tối thiểu

- Mentor chỉ truy cập được dữ liệu của thực tập sinh mình hướng dẫn; quyền kiểm tra ở phía máy chủ, không dựa vào giao diện.
- Đề xuất chia đều phản hồi nhanh với nhóm lớn, và áp dụng theo lô để không treo.
- Nhật ký không sửa được từ giao diện.

### Ngoài phạm vi (để story sau)

- Tạo tài khoản mentor và gán phòng ban (quản trị viên).
- Nhận xét/đánh giá của mentor.
- Lịch làm việc.
- Email thông báo phân công cho mentor và thực tập sinh.
- Giới hạn tải mentor.
- Nhiều mentor cho một thực tập sinh.
- Gỡ phân công, và tự gỡ khi thực tập sinh không còn "Được nhận".
- Trọng số hoặc chuyên môn của mentor khi chia.

## 4. Tác động lên các tài liệu trước

- **Chương trình thực tập:** phòng ban cần có mentor (do quản trị viên gán) thì thực tập sinh của chương trình mới phân công được.
- **Hợp đồng:** danh sách thực tập sinh "Được nhận" của HR có thêm cột mentor để tra cứu.
- **Duyệt hồ sơ:** không thay đổi, vì story này chỉ bắt đầu sau khi thực tập sinh đã "Được nhận".

## 5. Cập nhật theo story "Thực tập sinh xem lịch thực tập cá nhân"

- **Đổi mentor ảnh hưởng lịch:** đổi mentor làm đổi nhóm sự kiện hiển thị trên lịch của thực tập sinh. Sự kiện nhóm theo mentor cũ (kể cả đã diễn ra) không còn trong lịch; sự kiện theo mentor mới xuất hiện ở lần xem sau.

## 6. Cập nhật theo story "Thực tập sinh check-in/check-out"

- **Quyền của mentor:** ngoài thông tin cơ bản và hồ sơ, mentor còn xem được bản ghi chấm công của các thực tập sinh mình hướng dẫn (chỉ xem).
- **Đổi mentor:** mentor mới thấy toàn bộ lịch sử chấm công của thực tập sinh; mentor cũ không còn thấy.