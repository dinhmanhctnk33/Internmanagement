# Phân tích nghiệp vụ: Thực tập sinh cập nhật tiến độ công việc

**User story:** Là thực tập sinh, tôi muốn cập nhật tiến độ công việc để mentor theo dõi.

**Bối cảnh:** Story này **mở rộng** tài liệu "Mentor giao nhiệm vụ cho thực tập sinh". Ở đó thực tập sinh đã cập nhật được trạng thái phần việc (Chưa làm / Đang làm / Hoàn thành) và mentor đã xem được trạng thái cùng thời điểm cập nhật cuối. Phần mới là **tiến độ chi tiết**: phần trăm hoàn thành và lịch sử các lần cập nhật. Tài liệu này chỉ mô tả phần thay đổi; mọi thứ còn lại giữ nguyên như tài liệu gốc.

## 1. Phạm vi và khái niệm (phần thay đổi)

### Thêm vào phần việc

- **Phần trăm hoàn thành** (0 đến 100).
- **Lịch sử cập nhật:** mỗi lần cập nhật tạo một dòng gồm thời điểm, trạng thái, phần trăm. Chỉ đọc, không sửa hay xóa.

### Ràng buộc trạng thái và phần trăm

"Chưa làm" tự là 0%, "Hoàn thành" tự là 100%, "Đang làm" **bắt buộc nhập** phần trăm từ 1 đến 99. Không có phần việc nào ở một trạng thái mà mang phần trăm của trạng thái khác.

### Ai làm gì

Thực tập sinh cập nhật phần trăm và xem lịch sử phần việc của mình. Mentor xem phần trăm hiện tại và lịch sử của từng người nhận trong nhiệm vụ mình giao.

### Quyết định thiết kế

- Mỗi lần cập nhật gồm **chỉ phần trăm** (không có ghi chú).
- Quan hệ phần trăm và trạng thái: **trạng thái là chính, phần trăm chỉ nhập khi "Đang làm"**.
- **Hiển thị lịch sử các lần cập nhật cho mentor và thực tập sinh**, chỉ đọc.

### Giả định đã xác nhận

1. **Chỉ ghi một dòng lịch sử khi có thay đổi thật** (đổi trạng thái hoặc đổi phần trăm). Bấm lưu lại đúng giá trị cũ thì không tạo dòng mới.
2. Phần trăm là **số nguyên** và **được phép giảm** (tiến độ có thể lùi khi phải làm lại), không bắt buộc luôn tăng.
3. Danh sách của mentor hiển thị cả **số người hoàn thành** (ví dụ 2/5) lẫn **tiến độ trung bình** của các phần việc đang hiệu lực, không tính phần việc "Đã gỡ"; "Chưa làm" tính 0%, "Hoàn thành" tính 100%.

## 2. Luồng nghiệp vụ (phần thay đổi)

1. **Cập nhật.** Thực tập sinh mở nhiệm vụ, chọn trạng thái. Nếu chọn "Đang làm" thì nhập phần trăm (1 đến 99); chọn "Chưa làm" hoặc "Hoàn thành" thì phần trăm tự đặt 0 hoặc 100. Lưu xong, hệ thống ghi một dòng lịch sử theo giờ máy chủ.
2. **Xem lịch sử.** Trong chi tiết phần việc có dòng thời gian: ngày giờ, trạng thái, phần trăm, mục mới nhất ở trên.
3. **Mentor theo dõi.** Chi tiết nhiệm vụ hiện, với mỗi người, phần trăm hiện tại, thanh tiến độ, thời điểm cập nhật cuối, và mở được lịch sử. Danh sách nhiệm vụ hiện cả số hoàn thành (ví dụ 2/5) lẫn tiến độ trung bình.
4. **Giới hạn.** Nhiệm vụ đã hủy hoặc phần việc đã gỡ thì không cập nhật được; lịch sử vẫn xem được. Phần việc "Đã gỡ" chỉ mentor còn thấy lịch sử, thực tập sinh thì không.

### Quy tắc nghiệp vụ

- Phần trăm là số nguyên; khi "Đang làm" phải từ 1 đến 99.
- Giờ ghi lịch sử lấy từ máy chủ.
- Lịch sử không sửa hay xóa được từ giao diện.
- Thực tập sinh chỉ thấy lịch sử phần việc của mình; mentor chỉ thấy lịch sử của người nhận trong nhiệm vụ mình giao.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Phần trăm ngoài 1 đến 99 khi "Đang làm", để trống, hoặc không phải số nguyên:** chặn lưu và đánh dấu đúng trường.
- **Hai tab cập nhật gần nhau:** cả hai đều ghi vào lịch sử theo thứ tự thời gian máy chủ; giá trị cuối cùng là giá trị hiện tại.
- **Mất kết nối giữa chừng:** không ghi gì và báo lỗi rõ.
- **Lịch sử dài:** phân trang.
- **Đổi mentor:** lịch sử của phần việc đã hoàn thành được giữ; phần việc chưa xong chuyển "Đã gỡ" như đã chốt ở tài liệu gốc; mentor mới không thấy lịch sử cũ.
- **Đổi đường dẫn để xem phần việc của người khác:** bị từ chối, không lộ sự tồn tại của phần việc đó.
- **Lưu lại đúng giá trị cũ:** không tạo dòng lịch sử mới.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Chọn "Đang làm" thì phải nhập phần trăm từ 1 đến 99, nếu không thì không lưu được. |
| 2 | Chọn "Chưa làm" thì phần trăm là 0; chọn "Hoàn thành" thì là 100. |
| 3 | Mỗi lần cập nhật có thay đổi tạo đúng một dòng lịch sử có thời điểm, trạng thái, phần trăm; lưu lại giá trị cũ thì không tạo dòng. |
| 4 | Phần trăm giảm được (ví dụ 70 xuống 40) và dòng lịch sử ghi đúng. |
| 5 | Thực tập sinh xem được lịch sử phần việc của mình; mentor xem được lịch sử từng người nhận trong nhiệm vụ mình giao. |
| 6 | Danh sách của mentor hiển thị đúng số hoàn thành và tiến độ trung bình, không tính phần việc "Đã gỡ". |
| 7 | Nhiệm vụ đã hủy hoặc phần việc đã gỡ thì không cập nhật được; lịch sử vẫn xem được. |
| 8 | Thực tập sinh và mentor không xem được lịch sử phần việc không thuộc mình. |

### Ngoài phạm vi (để story sau)

- Ghi chú đi kèm cập nhật.
- Nộp kết quả, mentor xác nhận hoặc chấm.
- Cảnh báo phần việc đứng tiến độ lâu ngày.
- Báo cáo tiến độ tổng hợp cho HR.
- Biểu đồ tiến độ.
- Nhiệm vụ con có phần trăm riêng.

## 4. Tác động lên tài liệu gốc

- **Mentor giao nhiệm vụ:** trạng thái phần việc nay đi kèm phần trăm; có lịch sử cập nhật (mentor và thực tập sinh xem được, chỉ đọc); danh sách của mentor có thêm tiến độ trung bình bên cạnh số hoàn thành. Các nút tắt "Chưa làm" và "Hoàn thành" ở luồng gốc vẫn đặt 0% và 100%.