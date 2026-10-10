# Phân tích nghiệp vụ: Mentor giao nhiệm vụ cho thực tập sinh

**User story:** Là mentor, tôi muốn giao nhiệm vụ cho thực tập sinh để họ có công việc cụ thể.

**Bối cảnh:** Story này nối tiếp phân công mentor và lịch. Nó là lần đầu mentor có một **thao tác ghi** (đến nay mentor chỉ xem), và đưa vào hai khái niệm mới: *nhiệm vụ* và *phần việc*. Vòng đời nhẹ: thực tập sinh tự cập nhật trạng thái, không có bước nộp kết quả hay mentor xác nhận, nên "Hoàn thành" là **thực tập sinh tự báo**, không phải bằng chứng đã làm xong. Hạn nhiệm vụ hiện trên lịch của thực tập sinh.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Mentor:** tạo, giao, sửa, hủy nhiệm vụ cho thực tập sinh mình hướng dẫn; xem tiến độ của từng người.
- **Thực tập sinh:** xem nhiệm vụ được giao và cập nhật trạng thái phần việc của chính mình.
- **Hệ thống:** ghi nhật ký, đánh dấu quá hạn.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Nhiệm vụ | Tiêu đề, mô tả (văn bản, có thể chứa liên kết), hạn (ngày), người giao (mentor), trạng thái (*Đang hiệu lực* hoặc *Đã hủy*), nhãn "đã chỉnh sửa", danh sách người nhận. |
| Phần việc | Một nhiệm vụ đối với một thực tập sinh, có trạng thái riêng *Chưa làm → Đang làm → Hoàn thành*. |
| Nhật ký | Ghi ai làm gì, khi nào, giá trị cũ và mới (sửa nhiệm vụ, đổi người nhận, đổi trạng thái). |

### Quyết định thiết kế

- Vòng đời: **giao việc + thực tập sinh tự cập nhật trạng thái** (Chưa làm / Đang làm / Hoàn thành).
- **Một nhiệm vụ giao cho nhiều thực tập sinh; mỗi người có trạng thái riêng.**
- Sửa hoặc hủy sau khi giao: **sửa tự do, có nhãn "đã chỉnh sửa" và nhật ký**; hủy là đặt "Đã hủy", không xóa.

### Giả định đã xác nhận

1. **Người nhận chỉ gồm thực tập sinh đang được chính mentor đó hướng dẫn** (đã "Được nhận" và được phân công cho mentor). Mentor không giao được cho người của mentor khác.
2. **Hạn là một ngày** (không có giờ) và là tùy chọn. Khi tạo hoặc đổi hạn, hạn không được trước hôm nay. Phần việc chưa "Hoàn thành" mà quá hạn sẽ hiện nhãn "Quá hạn", không chặn việc cập nhật.
3. **Trạng thái đổi qua lại tự do:** thực tập sinh chuyển giữa ba trạng thái, kể cả từ "Hoàn thành" về "Đang làm"; mọi thay đổi có nhật ký. Mentor không đổi trạng thái thay thực tập sinh.
4. **Hạn nhiệm vụ hiện trên lịch** của thực tập sinh như một mốc tự động (loại "Hạn nhiệm vụ") cho đến khi họ "Hoàn thành" hoặc nhiệm vụ bị hủy.

### Phạm vi

Mentor tạo, sửa, hủy, xem tiến độ; thực tập sinh xem và cập nhật trạng thái.

## 2. Luồng nghiệp vụ

1. **Danh sách của mentor.** Mentor vào "Nhiệm vụ": các nhiệm vụ mình đã giao, kèm tiêu đề, hạn, số người nhận, tiến độ (ví dụ 2/5 đã hoàn thành), trạng thái và nhãn. Lọc theo trạng thái, quá hạn, thực tập sinh; tìm theo tiêu đề.
2. **Tạo và giao.** Mentor nhập tiêu đề (bắt buộc), mô tả, hạn (tùy chọn), chọn một hoặc nhiều người nhận trong nhóm mình (có "chọn tất cả"). Hệ thống tạo nhiệm vụ cùng một phần việc "Chưa làm" cho mỗi người, ghi nhật ký, và đưa hạn lên lịch từng người.
3. **Xem tiến độ.** Chi tiết nhiệm vụ liệt kê từng người nhận, trạng thái, thời điểm cập nhật cuối, nhãn "Quá hạn".
4. **Sửa.** Mentor sửa tiêu đề, mô tả, hạn, thêm hoặc bớt người nhận. Đổi tiêu đề, mô tả hoặc hạn thì nhiệm vụ mang nhãn "đã chỉnh sửa"; giá trị cũ nằm trong nhật ký. Thêm người thì có phần việc mới "Chưa làm". Bớt người thì phần việc của họ chuyển "Đã gỡ": họ không còn thấy, mentor vẫn thấy mục "Đã gỡ", dữ liệu giữ trong nhật ký.
5. **Hủy và mở lại.** Mentor đặt nhiệm vụ "Đã hủy" (lý do tùy chọn) và có thể mở lại. Nhiệm vụ đã hủy vẫn hiện cho thực tập sinh ở mục "Đã hủy", họ không đổi trạng thái được, và hạn biến khỏi lịch.
6. **Thực tập sinh xem.** "Nhiệm vụ của tôi": tiêu đề, người giao, hạn, trạng thái, nhãn "đã chỉnh sửa" hoặc "Quá hạn"; lọc theo trạng thái; mở chi tiết.
7. **Cập nhật trạng thái.** Thực tập sinh chọn *Chưa làm*, *Đang làm* hoặc *Hoàn thành* cho phần việc của mình; hệ thống ghi thời điểm và nhật ký.

### Vòng đời

`Nhiệm vụ: Đang hiệu lực ⇄ Đã hủy` `Phần việc: Chưa làm ⇄ Đang làm ⇄ Hoàn thành` (đổi qua lại tự do), và `Đã gỡ` khi bị bớt khỏi nhiệm vụ.

### Quy tắc nghiệp vụ

- Mentor chỉ giao cho thực tập sinh của mình và chỉ thấy nhiệm vụ mình tạo; thực tập sinh chỉ thấy phần việc của mình.
- Tiêu đề bắt buộc. Khi tạo hoặc đổi hạn, hạn không được trước hôm nay. "Quá hạn" tính sau khi hết ngày hạn.
- **"Hoàn thành" là thực tập sinh tự báo**, không phải mentor xác nhận; giao diện ghi rõ điều đó.
- Nhiệm vụ không xóa được, chỉ hủy. Mọi thao tác đều có nhật ký.
- Hạn nhiệm vụ trên lịch biến mất khi phần việc "Hoàn thành", bị gỡ hoặc nhiệm vụ bị hủy.
- Khi thực tập sinh đổi mentor: phần việc chưa hoàn thành trong nhiệm vụ của mentor cũ chuyển "Đã gỡ" (biến khỏi lịch, thực tập sinh không còn thấy); phần đã hoàn thành giữ để tra cứu; mentor mới không thấy nhiệm vụ cũ.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Thiếu tiêu đề, tiêu đề hoặc mô tả quá dài, hạn trước hôm nay, chưa chọn người nhận:** chặn lưu và đánh dấu đúng trường; dữ liệu đã nhập không bị mất.
- **Mentor chưa có thực tập sinh nào:** không giao được; hệ thống giải thích rằng cần HR phân công thực tập sinh trước, không để mentor gặp danh sách người nhận trống không lý do.
- **Bấm giao hai lần hoặc mất kết nối giữa chừng:** chỉ tạo một nhiệm vụ, không có nhiệm vụ trùng hay nhiệm vụ dở dang không có người nhận.
- **Hai lần sửa đồng thời** (mentor mở hai tab): lần lưu sau thấy thông báo "nhiệm vụ đã được cập nhật", phải tải lại trước khi lưu, tránh ghi đè lặng lẽ.
- **Thực tập sinh đang xem hoặc cập nhật trong lúc mentor sửa hoặc hủy:** lần thao tác kế tiếp báo "nhiệm vụ đã thay đổi" và hiển thị bản mới; cập nhật trạng thái trên nhiệm vụ đã hủy bị từ chối.
- **Bớt người đã "Hoàn thành":** phần việc vẫn chuyển "Đã gỡ" và biến khỏi danh sách của họ; hệ thống nhắc mentor rằng người này đã báo hoàn thành trước khi xác nhận.
- **Thêm lại người đã bị gỡ:** tạo phần việc mới "Chưa làm", không khôi phục trạng thái cũ; lịch sử cũ vẫn trong nhật ký.
- **Hủy rồi mở lại:** các phần việc giữ nguyên trạng thái trước khi hủy; hạn trở lại trên lịch của những người chưa hoàn thành.
- **Thực tập sinh đổi mentor:** phần việc chưa hoàn thành trong nhiệm vụ của mentor cũ chuyển "Đã gỡ" và biến khỏi lịch; phần đã hoàn thành giữ để tra cứu; mentor mới không thấy nhiệm vụ cũ.
- **Thực tập sinh không còn "Được nhận" hoặc hết kỳ thực tập:** phần việc giữ nguyên, vẫn xem và cập nhật được; không tự gỡ.
- **Hạn rơi vào ngày nghỉ chung hoặc cuối tuần:** vẫn cho phép, hiển thị bình thường, không cảnh báo.
- **Mentor hoặc thực tập sinh mở nhiệm vụ không thuộc mình bằng cách đổi đường dẫn:** bị từ chối, không lộ sự tồn tại của nhiệm vụ đó.
- **Lỗi tải hoặc mất kết nối:** hiển thị lỗi kèm nút thử lại, không hiện danh sách trống như thể chưa có nhiệm vụ nào.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Mentor nhập tiêu đề hợp lệ, chọn ít nhất một người nhận trong nhóm mình thì nhiệm vụ được tạo, mỗi người có một phần việc "Chưa làm", và nhật ký ghi lại. |
| 2 | Mentor không chọn được thực tập sinh ngoài nhóm mình làm người nhận. |
| 3 | Thiếu tiêu đề, chưa chọn người nhận, hoặc hạn trước hôm nay thì không lưu được và đúng trường sai được đánh dấu. |
| 4 | Hạn nhiệm vụ hiện trên lịch của từng người nhận như một mốc "Hạn nhiệm vụ"; mốc biến mất khi người đó "Hoàn thành", bị gỡ, hoặc nhiệm vụ bị hủy. |
| 5 | Thực tập sinh thấy "Nhiệm vụ của tôi" gồm tiêu đề, người giao, hạn, trạng thái, nhãn "đã chỉnh sửa" hoặc "Quá hạn", và lọc được theo trạng thái. |
| 6 | Thực tập sinh đổi trạng thái phần việc giữa Chưa làm, Đang làm, Hoàn thành (hai chiều) thì lưu ngay, ghi thời điểm và nhật ký. |
| 7 | Phần việc chưa "Hoàn thành" sau ngày hạn thì hiện "Quá hạn" mà không chặn cập nhật. |
| 8 | Mentor xem được chi tiết nhiệm vụ với trạng thái và thời điểm cập nhật cuối của từng người, và tiến độ tổng (ví dụ 2/5 hoàn thành) ở danh sách. |
| 9 | Mentor đổi tiêu đề, mô tả hoặc hạn thì nhiệm vụ mang nhãn "đã chỉnh sửa" và nhật ký giữ giá trị cũ, giá trị mới. |
| 10 | Mentor thêm người thì có phần việc "Chưa làm" mới; bớt người thì phần việc chuyển "Đã gỡ", người đó không còn thấy, và dữ liệu giữ trong nhật ký. |
| 11 | Mentor hủy nhiệm vụ thì thực tập sinh thấy ở mục "Đã hủy", không đổi trạng thái được, và hạn biến khỏi lịch; mở lại thì nhiệm vụ trở về trạng thái trước khi hủy. |
| 12 | Thực tập sinh đổi mentor thì phần việc chưa xong trong nhiệm vụ của mentor cũ chuyển "Đã gỡ", phần đã xong được giữ, và mentor mới không thấy nhiệm vụ cũ. |
| 13 | Nhiệm vụ không xóa được; mentor và thực tập sinh không xem được nhiệm vụ không thuộc mình. |

### Yêu cầu phi chức năng tối thiểu

- Quyền xem và sửa kiểm tra ở phía máy chủ, không dựa vào giao diện.
- Tạo nhiệm vụ cho nhiều người an toàn khi bấm lặp hoặc mất kết nối: không tạo nhiệm vụ trùng hay bản ghi dở dang.
- Danh sách nhiệm vụ phản hồi nhanh khi mentor có nhiều nhiệm vụ và nhiều người nhận.
- Nhật ký không sửa được từ giao diện.

### Ngoài phạm vi (để story sau)

- Nộp kết quả.
- Mentor xác nhận hoặc chấm điểm "Hoàn thành".
- Bình luận trao đổi.
- File đính kèm.
- Nhắc việc hoặc email thông báo giao việc.
- HR xem và báo cáo nhiệm vụ.
- Nhiệm vụ lặp lại hoặc mẫu.
- Độ ưu tiên và nhãn phân loại.
- Nhiệm vụ con.
- Giữ phần việc chưa xong khi đổi mentor.

## 4. Tác động lên các tài liệu trước

- **Lịch:** hạn nhiệm vụ là một loại **mốc tự động mới** ("Hạn nhiệm vụ"), tách khỏi sự kiện do HR/mentor nhập; mốc chỉ hiện cho phần việc chưa hoàn thành của nhiệm vụ đang hiệu lực.
- **Phân công mentor:** mentor có thêm quyền giao việc cho thực tập sinh mình hướng dẫn; đổi mentor làm các phần việc chưa xong của mentor cũ chuyển "Đã gỡ".

## 5. Cập nhật theo story "Thực tập sinh cập nhật tiến độ công việc"

- **Tiến độ chi tiết:** phần việc nay có thêm phần trăm hoàn thành. "Chưa làm" là 0%, "Hoàn thành" là 100%, "Đang làm" bắt buộc nhập từ 1 đến 99%. Các nút tắt "Chưa làm" và "Hoàn thành" ở luồng cập nhật trạng thái vẫn đặt 0% và 100%.
- **Lịch sử cập nhật:** mỗi lần đổi trạng thái hoặc phần trăm tạo một dòng lịch sử (thời điểm, trạng thái, phần trăm), chỉ đọc; thực tập sinh xem lịch sử phần việc của mình, mentor xem lịch sử từng người nhận trong nhiệm vụ mình giao.
- **Danh sách của mentor:** ngoài số người hoàn thành (ví dụ 2/5) còn có tiến độ trung bình của các phần việc đang hiệu lực, không tính phần việc "Đã gỡ".
- Chi tiết đầy đủ ở tài liệu "Thực tập sinh cập nhật tiến độ công việc".