# Phân tích nghiệp vụ: Thực tập sinh xem lịch thực tập cá nhân

**User story:** Là thực tập sinh, tôi muốn xem lịch thực tập cá nhân để biết kế hoạch.

**Bối cảnh:** Story này nối tiếp các story trước và **đưa vào một khái niệm chưa từng có: lịch**. Các tài liệu trước từng đẩy "lịch trình chi tiết trong kỳ thực tập" và "lịch làm việc" ra ngoài phạm vi. Vì thực tập sinh chỉ *xem*, nên dữ liệu lịch đến từ hai nguồn: các mốc tự động tính từ dữ liệu có sẵn, và sự kiện do HR/mentor nhập ở story sau. Story này chỉ làm phần xem và chốt cấu trúc sự kiện.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Thực tập sinh:** xem lịch của chính mình.
- **Hệ thống:** tạo các mốc tự động, hợp nhất với sự kiện thành một lịch.
- **HR/mentor:** *không thao tác trong story này*, nhưng là nguồn tạo sự kiện ở story sau.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Lịch cá nhân | Tập hợp các mục lịch của đúng một thực tập sinh, hiển thị dạng tháng hoặc tuần. |
| Mốc tự động | Hệ thống tính từ dữ liệu có sẵn, không ai nhập. |
| Sự kiện | Do HR/mentor nhập ở story sau (buổi đào tạo, họp, hạn nộp việc…). |
| Cấu trúc mục lịch | Tiêu đề, loại, thời gian bắt đầu và kết thúc, cả ngày hoặc theo giờ, mô tả, địa điểm hoặc liên kết, người tạo, đối tượng nhận. |

### Quyết định thiết kế

- Lịch gồm **hai nguồn**: mốc tự động từ dữ liệu có sẵn và sự kiện do HR/mentor nhập.
- Story này **chỉ làm phần xem + chốt cấu trúc sự kiện**; việc HR/mentor nhập sự kiện để story riêng.
- Hiển thị **lịch dạng tháng/tuần** (lưới lịch). Rủi ro đã chấp nhận: giai đoạn đầu lịch chỉ có vài mốc nên lưới khá thưa; thiết kế bù bằng chi tiết theo ngày và trạng thái trống có lời giải thích.

### Giả định đã xác nhận

1. **Mốc "bắt đầu thực tập" và "kết thúc thực tập" lấy từ hợp đồng nếu đã có, chưa có thì lấy từ chương trình.** Giá trị tính lúc xem, không sao chép, nên tự cập nhật khi nguồn đổi.
2. Thực tập sinh chỉ có lịch **từ khi được nhận** ("Được nhận"). Trước đó họ thấy thông báo "chưa có lịch" kèm lý do.
3. Mỗi sự kiện có **đối tượng nhận**: một hoặc nhiều thực tập sinh cụ thể, hoặc cả nhóm (theo chương trình hay theo mentor). Lịch của một thực tập sinh gồm sự kiện gửi riêng cho họ cộng sự kiện gửi cho nhóm họ thuộc về.

### Phạm vi

Xem lịch tháng/tuần, chuyển tháng/tuần, về hôm nay, bấm vào mục để xem chi tiết, xem đầy đủ khi một ngày có nhiều mục, và cấu trúc sự kiện.

## 2. Luồng nghiệp vụ

1. **Vào lịch.** Thực tập sinh đăng nhập, vào "Lịch của tôi". Mặc định là dạng tháng, tháng hiện tại, ngày hôm nay được đánh dấu.
2. **Điều hướng.** Chuyển tháng/tuần trước hoặc sau, nút "Hôm nay", chuyển giữa dạng tháng và tuần (giữ ngày đang xem).
3. **Lưới lịch.** Mỗi ô ngày hiện tối đa vài mục dưới dạng nhãn rút gọn, còn lại gom thành "+N mục khác". Mốc và sự kiện phân biệt bằng nhãn hoặc ký hiệu. Khoảng từ ngày bắt đầu đến ngày kết thúc thực tập được tô nền nhẹ để thấy mình đang trong kỳ.
4. **Xem chi tiết.** Bấm vào ngày để thấy toàn bộ mục của ngày đó; bấm vào mục để xem chi tiết (tiêu đề, loại, thời gian, mô tả, địa điểm hoặc liên kết; với sự kiện có thêm người tạo; với mốc cho biết nguồn là hợp đồng hay chương trình).
5. **Trạng thái trống.** Chưa được nhận thì hiển thị "Chưa có lịch" kèm lý do. Đã được nhận nhưng chưa có sự kiện thì vẫn thấy các mốc, kèm dòng "Chưa có sự kiện nào được lên lịch".

### Quy tắc nghiệp vụ

- Thực tập sinh chỉ thấy lịch của chính mình và **chỉ xem**, không thêm, sửa hay xóa được.
- Mốc tính lúc xem; nguồn ưu tiên là hợp đồng, rồi đến chương trình.
- Một sự kiện hiện ra khi thực tập sinh thuộc đối tượng nhận, trực tiếp hoặc qua nhóm (chương trình hoặc mentor). Nhóm được xác định **tại thời điểm xem**.
- Khi thực tập sinh đổi mentor, sự kiện nhóm theo mentor cũ (kể cả đã diễn ra) không còn trong lịch; sự kiện theo mentor mới xuất hiện. Đây là lựa chọn đã chấp nhận cho đơn giản.
- Sự kiện đã hủy không hiện. Sự kiện nhiều ngày hiện trên mọi ngày nó bao phủ.
- Thứ tự trong một ngày: mục cả ngày trước, rồi theo giờ tăng dần.
- Lịch luôn phản ánh dữ liệu hiện tại khi tải lại trang.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Thực tập sinh chưa được nhận:** vào lịch thấy "Chưa có lịch" kèm lý do, không phải lỗi hay trang trống.
- **Được nhận nhưng chưa có hợp đồng:** mốc bắt đầu và kết thúc lấy từ chương trình; khi hợp đồng được tải lên, mốc chuyển sang lấy từ hợp đồng ở lần xem sau.
- **Ngày hợp đồng khác ngày chương trình:** luôn theo hợp đồng, và chi tiết mốc ghi rõ nguồn để thực tập sinh không thắc mắc vì sao khác ngày công bố của chương trình.
- **HR xóa hợp đồng:** mốc quay về lấy theo chương trình ở lần xem tiếp theo.
- **Ngày có quá nhiều mục:** ô ngày chỉ hiện vài mục kèm "+N mục khác"; bấm vào xem được đầy đủ, không bị cắt mất thông tin.
- **Sự kiện kéo dài nhiều ngày hoặc qua tháng:** hiện trên mọi ngày bao phủ, kể cả khi sang tháng khác.
- **Sự kiện bị hủy hoặc đối tượng nhận bị đổi sau khi thực tập sinh đã mở lịch:** lần tải lại hoặc chuyển tháng tiếp theo phản ánh thay đổi; hệ thống không giữ bản cũ.
- **Thực tập sinh đổi mentor:** sự kiện nhóm theo mentor cũ không còn trong lịch, sự kiện theo mentor mới xuất hiện; sự kiện gửi riêng cho thực tập sinh và sự kiện theo chương trình không đổi.
- **Chương trình bị sửa ngày thực tập sau khi hợp đồng đã tạo:** mốc theo hợp đồng nên không đổi theo chương trình.
- **Mở lịch của người khác bằng cách đổi đường dẫn:** bị từ chối, không lộ sự tồn tại của thực tập sinh đó.
- **Mất kết nối hoặc lỗi tải:** hiển thị thông báo lỗi kèm nút thử lại, không để lưới lịch trống như thể không có gì.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Thực tập sinh "Được nhận" mở "Lịch của tôi" thì thấy lịch dạng tháng của tháng hiện tại, ngày hôm nay được đánh dấu. |
| 2 | Thực tập sinh chuyển giữa dạng tháng và tuần, chuyển tháng/tuần trước hoặc sau, và bấm "Hôm nay" đều hoạt động đúng và giữ đúng ngày đang xem. |
| 3 | Mốc "bắt đầu thực tập" và "kết thúc thực tập" hiển thị đúng ngày: lấy từ hợp đồng nếu đã có, ngược lại lấy từ chương trình. |
| 4 | Khoảng từ ngày bắt đầu đến ngày kết thúc thực tập được tô nền khác với ngoài kỳ. |
| 5 | Sự kiện gửi cho thực tập sinh hoặc cho nhóm của họ thì hiển thị; sự kiện không gửi cho họ thì không hiển thị. |
| 6 | Sự kiện đã hủy không hiện; sự kiện nhiều ngày hiện trên mọi ngày bao phủ. |
| 7 | Ngày có nhiều mục thì ô ngày hiện một phần kèm "+N mục khác", bấm vào hiển thị đủ. |
| 8 | Bấm vào một mục thì thấy chi tiết đúng loại: sự kiện có người tạo, mốc có nguồn (hợp đồng hoặc chương trình). |
| 9 | Trong một ngày, mục cả ngày hiện trước, sau đó theo giờ tăng dần. |
| 10 | Thực tập sinh chưa được nhận thấy "Chưa có lịch" kèm lý do; thực tập sinh đã được nhận nhưng chưa có sự kiện vẫn thấy mốc kèm thông báo chưa có sự kiện. |
| 11 | Thực tập sinh không thêm, sửa, xóa được bất kỳ mục nào, và không xem được lịch của người khác. |
| 12 | Thực tập sinh đổi mentor thì lịch phản ánh sự kiện theo mentor mới sau lần tải lại. |

### Yêu cầu phi chức năng tối thiểu

- Quyền xem kiểm tra ở phía máy chủ: mỗi thực tập sinh chỉ lấy được dữ liệu lịch của chính mình.
- Chuyển tháng/tuần phản hồi nhanh; chỉ tải dữ liệu của khoảng thời gian đang xem.
- Giao diện lưới tháng và tuần dùng được trên màn hình điện thoại (không tràn ngang, mục nhỏ vẫn bấm được).

### Cấu trúc sự kiện cần chốt (hợp đồng giao tiếp cho story nhập sự kiện)

Tiêu đề, loại, thời gian bắt đầu và kết thúc (hoặc cả ngày), mô tả, địa điểm hoặc liên kết, người tạo, đối tượng nhận (thực tập sinh cụ thể, hoặc nhóm theo chương trình/mentor), trạng thái (hiệu lực/đã hủy). Story nhập sự kiện chỉ cần tạo đúng cấu trúc này.

### Ngoài phạm vi (để story sau)

- Nhập/sửa/hủy sự kiện bởi HR và mentor.
- Nhắc việc hoặc email thông báo.
- Xuất lịch .ics hay đồng bộ lịch ngoài.
- HR/mentor xem lịch của thực tập sinh.
- Xác nhận tham dự.
- Lịch phỏng vấn trước khi được nhận.
- Giữ lại sự kiện nhóm của mentor cũ khi đổi mentor.

## 4. Tác động lên các tài liệu trước

- **Hợp đồng:** ngày bắt đầu/kết thúc của hợp đồng là nguồn ưu tiên của hai mốc trên lịch, nên HR sửa ngày hợp đồng sẽ đổi mốc trên lịch của thực tập sinh ở lần xem sau.
- **Phân công mentor:** đổi mentor làm đổi nhóm sự kiện hiển thị trên lịch của thực tập sinh.