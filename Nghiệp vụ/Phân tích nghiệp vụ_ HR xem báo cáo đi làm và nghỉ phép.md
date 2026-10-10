# Phân tích nghiệp vụ: HR xem báo cáo đi làm và nghỉ phép

**User story:** Là HR, tôi muốn xem báo cáo đi làm và nghỉ phép để quản lý sự chuyên cần.

**Bối cảnh:** Story này nối tiếp story check-in/check-out. Nó đưa mục "báo cáo tổng hợp" (trước đây ngoài phạm vi của chấm công) vào làm cho HR. Phần **đi làm** có nguồn dữ liệu là bản ghi chấm công; phần **nghỉ phép hoàn toàn chưa có nguồn** (chưa có xin nghỉ, duyệt nghỉ hay ghi nhận ngày nghỉ), nên được để cho story riêng. Hệ quả cần nhớ: cho đến khi có nguồn nghỉ phép, báo cáo **không phân biệt được vắng có phép và vắng không phép**, nên chưa nên dùng để kết luận về kỷ luật của từng người.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **HR:** xem báo cáo, khai báo ngày nghỉ chung, đặt thứ làm việc cho từng chương trình.
- **Hệ thống:** tính các chỉ số từ chấm công, thứ làm việc và ngày nghỉ chung.
- **Mentor và thực tập sinh:** không dùng báo cáo này (mentor vẫn xem bản ghi chấm công như story trước).

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Thứ làm việc | Trường mới của chương trình: tập các thứ trong tuần mà chương trình làm việc. |
| Ngày nghỉ chung | Danh sách ngày (kèm tên, ví dụ "Quốc khánh") do HR khai báo, áp dụng cho mọi chương trình. |
| Ngày làm việc kỳ vọng | Ngày thuộc thứ làm việc của chương trình, nằm trong kỳ thực tập, và không phải ngày nghỉ chung. |
| Báo cáo tổng hợp | Mỗi dòng một thực tập sinh: ngày làm việc kỳ vọng, ngày đi làm, ngày vắng, tổng giờ, số ngày thiếu check-out, số bản ghi đã chỉnh sửa, tỷ lệ chuyên cần. Cột **nghỉ phép hiển thị "Chưa có dữ liệu"** thay vì số 0. |
| Chi tiết theo ngày | Bấm vào một dòng để thấy từng ngày là đi làm, vắng, ngày nghỉ chung hay thiếu check-out. |

### Quyết định thiết kế

- Báo cáo chỉ dùng dữ liệu **chấm công**; nghỉ phép để story riêng, cột nghỉ phép chờ nguồn dữ liệu.
- **HR cấu hình thứ làm việc riêng cho từng chương trình**; việc thiết lập nằm trong story này.
- **HR khai báo danh sách ngày nghỉ chung**, loại khỏi ngày làm việc kỳ vọng của mọi chương trình.
- Hình thức báo cáo: **bảng tổng hợp + chi tiết theo ngày** (chưa có biểu đồ hay xuất file).

### Giả định đã xác nhận

1. **Ngày đi làm** là ngày kỳ vọng có bản ghi chưa bị "Hủy" (kể cả "Thiếu check-out", được đếm riêng và không có giờ). **Ngày vắng** là ngày kỳ vọng *đã qua* mà không có bản ghi hợp lệ. Hôm nay chưa tính vắng cho đến hết ngày; ngày tương lai không tính. Tỷ lệ chuyên cần = ngày đi làm chia ngày kỳ vọng đã qua.
2. **Chấm công ngoài ngày kỳ vọng** (cuối tuần, ngày lễ) vẫn cộng vào tổng giờ nhưng không làm đổi số ngày đi làm hay ngày vắng.
3. Chương trình hiện có **mặc định thứ Hai đến thứ Sáu** cho đến khi HR sửa. Thứ làm việc áp dụng cho cả kỳ, nên đổi thứ làm việc sẽ tính lại cả phần quá khứ của báo cáo.

### Phạm vi

Báo cáo tổng hợp, chi tiết theo ngày, lọc theo chương trình, phòng ban, thực tập sinh và khoảng ngày; thiết lập thứ làm việc của chương trình; khai báo ngày nghỉ chung.

## 2. Luồng nghiệp vụ

1. **Thiết lập (điều kiện tiên quyết, nằm trong story này).**
   - HR khai báo *ngày nghỉ chung*: thêm, sửa, xóa từng ngày kèm tên; không trùng ngày.
   - HR chọn *thứ làm việc* khi tạo hoặc sửa chương trình (ít nhất một thứ).
2. **Mở báo cáo.** HR vào "Báo cáo chuyên cần". Mặc định là tháng hiện tại, mọi thực tập sinh "Được nhận". HR lọc theo chương trình, phòng ban, thực tập sinh (tên/email) và khoảng ngày (tháng này, tháng trước, toàn kỳ, hoặc tự chọn).
3. **Bảng tổng hợp.** Mỗi dòng một thực tập sinh với các chỉ số đã chốt; sắp xếp theo bất kỳ cột nào (ví dụ tỷ lệ chuyên cần tăng dần để thấy ai thấp nhất). Có dòng tổng của nhóm đang lọc.
4. **Chi tiết.** Bấm vào một dòng để xem từng ngày: ngày, thứ, loại (*Đi làm*, *Vắng*, *Ngày nghỉ chung* kèm tên, *Ngoài ngày kỳ vọng có chấm công*, *Thiếu check-out*, *Hủy*), giờ vào/ra, tổng giờ, nhãn "đã chỉnh sửa". Có liên kết sang bản ghi chấm công để HR sửa nếu cần.
5. **Tính lúc xem.** Số liệu luôn tính từ dữ liệu hiện tại, không lưu bản chụp.

### Quy tắc nghiệp vụ

- **Chỉ HR** xem báo cáo, đặt thứ làm việc và khai báo ngày nghỉ chung.
- Ngày làm việc kỳ vọng = thứ làm việc của chương trình, trong kỳ thực tập (nguồn ưu tiên hợp đồng rồi chương trình), không phải ngày nghỉ chung.
- Khoảng ngày được lọc chỉ tính **phần giao** với kỳ thực tập của từng người; thực tập sinh bắt đầu hoặc kết thúc giữa khoảng lọc chỉ bị tính trong phần kỳ của họ.
- Đổi thứ làm việc hoặc ngày nghỉ chung thì **báo cáo tính lại ngay, kể cả phần quá khứ**; mọi thay đổi có nhật ký (ai, khi nào, giá trị cũ và mới).
- Ngày nghỉ chung khai báo *sau khi* thực tập sinh đã chấm công vào ngày đó: bản ghi giữ nguyên, tính vào giờ nhưng không vào số ngày đi làm kỳ vọng.
- Cột nghỉ phép luôn hiện "Chưa có dữ liệu" cho đến khi có nguồn.
- Báo cáo chỉ đọc, không sửa chấm công ngay trong báo cáo.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Thực tập sinh có 0 ngày kỳ vọng trong khoảng lọc** (mới được nhận, hoặc khoảng lọc chỉ gồm toàn ngày nghỉ): tỷ lệ chuyên cần hiển thị "—" thay vì chia cho 0 hay hiện 0% gây hiểu nhầm.
- **Thực tập sinh chưa có hợp đồng:** kỳ lấy từ chương trình; khi hợp đồng được tải lên với ngày khác, báo cáo dùng kỳ theo hợp đồng ở lần xem sau.
- **Chương trình chưa chọn thứ làm việc** (dữ liệu cũ, hoặc bản ghi bị thiếu): dùng mặc định thứ Hai đến thứ Sáu và báo cho HR rằng đang dùng mặc định.
- **HR bỏ hết thứ làm việc khi sửa chương trình:** chặn lưu, yêu cầu chọn ít nhất một thứ.
- **Ngày nghỉ chung bị trùng, sai định dạng, hoặc nằm ngoài mọi kỳ thực tập:** trùng thì chặn và báo ngày đã có; ngoài mọi kỳ thì vẫn lưu hợp lệ nhưng không ảnh hưởng báo cáo.
- **Xóa hoặc sửa ngày nghỉ chung đã tác động báo cáo:** báo cáo tính lại ngay; màn xác nhận nêu rõ ngày nào sẽ trở lại thành ngày làm việc kỳ vọng.
- **Khoảng lọc không hợp lệ** (ngày bắt đầu sau ngày kết thúc, hoặc quá dài): chặn và đánh dấu đúng trường.
- **Nhóm lọc quá nhiều thực tập sinh:** bảng phân trang, chỉ số tổng vẫn tính trên toàn bộ nhóm đã lọc chứ không chỉ trang hiện tại.
- **Bản ghi chấm công bị sửa hoặc hủy sau khi HR đã mở báo cáo:** lần tải lại hoặc đổi bộ lọc tiếp theo phản ánh số liệu mới; báo cáo không giữ bản chụp cũ.
- **Hôm nay chưa kết thúc:** ngày hôm nay không bị tính vắng; nếu thực tập sinh đã check-in nhưng chưa check-out thì hiện "Đang làm", không bị đếm là thiếu check-out cho đến khi sang ngày mới.
- **Người không phải HR cố mở báo cáo hoặc màn thiết lập:** bị từ chối, kể cả khi đổi đường dẫn.
- **Lỗi tải hoặc mất kết nối:** hiển thị lỗi kèm nút thử lại, không hiện bảng trống như thể không có thực tập sinh nào.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | HR khai báo ngày nghỉ chung kèm tên thì ngày đó không còn tính là ngày làm việc kỳ vọng của mọi chương trình, và không ai bị tính vắng vào ngày đó. |
| 2 | HR đặt thứ làm việc cho chương trình thì chỉ những thứ đó mới được tính là ngày kỳ vọng của thực tập sinh trong chương trình. |
| 3 | Chương trình hiện có chưa chọn thứ làm việc thì được tính theo thứ Hai đến thứ Sáu và báo cho HR biết. |
| 4 | HR mở báo cáo thì thấy mặc định tháng hiện tại, mọi thực tập sinh "Được nhận", mỗi dòng đủ các chỉ số đã chốt. |
| 5 | Ngày kỳ vọng đã qua có bản ghi chưa "Hủy" thì tính là đi làm; không có bản ghi hợp lệ thì tính là vắng; hôm nay và ngày tương lai không bị tính vắng. |
| 6 | Ngày "Thiếu check-out" được đếm riêng, tính là đi làm nhưng không có giờ. |
| 7 | Chấm công ngoài ngày kỳ vọng được cộng vào tổng giờ nhưng không đổi số ngày đi làm hay vắng. |
| 8 | Tỷ lệ chuyên cần bằng ngày đi làm chia ngày kỳ vọng đã qua; khi mẫu số bằng 0 thì hiển thị "—". |
| 9 | HR lọc theo chương trình, phòng ban, thực tập sinh, khoảng ngày thì cả bảng lẫn dòng tổng đều tính đúng trên nhóm đã lọc, chỉ trong phần giao với kỳ của từng người. |
| 10 | Bấm vào một dòng thì thấy chi tiết từng ngày với đúng loại, giờ vào/ra, tổng giờ, và nhãn chỉnh sửa; có liên kết sang bản ghi chấm công. |
| 11 | Cột nghỉ phép luôn hiển thị "Chưa có dữ liệu", không hiển thị 0. |
| 12 | Đổi thứ làm việc hoặc ngày nghỉ chung thì báo cáo tính lại ngay kể cả phần quá khứ, và có nhật ký giá trị cũ, giá trị mới. |
| 13 | Chỉ HR xem được báo cáo và thao tác thiết lập; người dùng khác bị từ chối. |

### Yêu cầu phi chức năng tối thiểu

- Quyền kiểm tra ở phía máy chủ, không dựa vào giao diện.
- Báo cáo phản hồi nhanh với khoảng lọc dài và nhiều thực tập sinh; chỉ số tổng tính trên toàn bộ nhóm lọc.
- Số liệu nhất quán với màn chấm công: cùng một ngày không thể là "đi làm" ở báo cáo nhưng "chưa có bản ghi" ở chấm công.
- Nhật ký thiết lập (thứ làm việc, ngày nghỉ chung) không sửa được từ giao diện.

### Ngoài phạm vi (để story sau)

- Nguồn dữ liệu nghỉ phép (xin nghỉ, duyệt, hoặc HR ghi nhận).
- Xuất file Excel/CSV.
- Biểu đồ xu hướng.
- Mentor xem báo cáo của nhóm mình.
- Đi muộn về sớm.
- Cảnh báo tự động khi chuyên cần thấp.
- Dùng báo cáo để xét hoàn thành thực tập hay tính phụ cấp.

## 4. Tác động lên các tài liệu trước

- **Chương trình thực tập:** có thêm trường *thứ làm việc* (mặc định thứ Hai đến thứ Sáu, ít nhất một thứ), sửa được bất kỳ lúc nào và áp dụng cho cả kỳ.
- **Chấm công:** báo cáo là nơi tiêu thụ dữ liệu chấm công; mục "báo cáo tổng hợp" chuyển từ ngoài phạm vi sang đã làm cho HR (xuất file và mentor xem báo cáo vẫn ngoài phạm vi).