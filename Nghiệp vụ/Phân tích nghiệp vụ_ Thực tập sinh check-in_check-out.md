# Phân tích nghiệp vụ: Thực tập sinh check-in/check-out

**User story:** Là thực tập sinh, tôi muốn check-in/check-out trên hệ thống để ghi nhận thời gian làm việc.

**Bối cảnh:** Story này nối tiếp các story trước và **đưa vào một khái niệm mới: bản ghi chấm công**. Nó kế thừa: chỉ thực tập sinh "Được nhận" dùng được, kỳ thực tập có ngày bắt đầu/kết thúc (từ hợp đồng hoặc chương trình), và mỗi thực tập sinh có một mentor. Chấm công là tự khai, không xác minh, nên giá trị dữ liệu dựa vào việc có người rà soát (mentor, HR) và dấu vết khi sửa.

## 1. Phạm vi, tác nhân và khái niệm chính

### Tác nhân

- **Thực tập sinh:** check-in, check-out, xem lịch sử chấm công của mình, tự sửa trong hạn.
- **Mentor:** xem bản ghi của các thực tập sinh mình hướng dẫn (đây là cách "rà soát sau" xảy ra).
- **HR:** xem bản ghi của mọi thực tập sinh và sửa khi đã quá hạn tự sửa.
- **Hệ thống:** ghi giờ, đánh dấu ngày thiếu check-out, ghi nhật ký.

### Khái niệm chính

| Khái niệm | Mô tả |
| --- | --- |
| Bản ghi chấm công | Mỗi thực tập sinh một bản ghi mỗi ngày: giờ check-in, giờ check-out, tổng thời gian, trạng thái (*Đang làm → Hoàn tất*, hoặc *Thiếu check-out* khi sang ngày mới mà chưa check-out, hoặc *Hủy*) và nhãn "đã chỉnh sửa". |
| Chỉnh sửa | Đổi giờ check-in hoặc check-out kèm lý do bắt buộc. Giá trị gốc được giữ trong nhật ký, bản ghi mang nhãn "đã chỉnh sửa". |
| Nhật ký | Ghi ai làm gì, khi nào, giá trị cũ và mới. |

### Quyết định thiết kế

- **Tự bấm check-in/check-out, không xác minh thêm** (không thu thập vị trí hay địa chỉ mạng); rà soát sau bởi mentor và HR.
- **Một lượt mỗi ngày:** đúng một check-in và một check-out.
- Khi quên check-out hoặc bấm nhầm: **thực tập sinh tự sửa trong hạn, có lý do và dấu vết**; quá hạn chỉ HR sửa.
- Hệ thống **không bao giờ tự điền giờ** cho ngày thiếu check-out.

### Giả định đã xác nhận

1. **Giờ check-in/out do máy chủ ghi** tại thời điểm bấm, thực tập sinh không nhập giờ khi chấm. Chỉ khi tự sửa mới nhập giờ tay, kèm lý do. "Ngày" tính theo múi giờ của hệ thống.
2. **Hạn tự sửa là đến hết ngày hôm sau** của ngày làm việc, cấu hình được. Quá hạn thì chỉ HR sửa.
3. Chỉ chấm được khi thực tập sinh ở trạng thái **"Được nhận"** và **trong kỳ thực tập** (nguồn ưu tiên là hợp đồng rồi chương trình). Không giới hạn thứ trong tuần vì chưa có lịch làm việc chuẩn.

### Phạm vi

Check-in, check-out, xem lịch sử, tự sửa trong hạn, mentor và HR xem, HR sửa quá hạn.

## 2. Luồng nghiệp vụ

1. **Màn chấm công.** Thực tập sinh thấy trạng thái hôm nay (chưa check-in, đang làm kèm giờ vào, hoặc hoàn tất) và một nút duy nhất: "Check-in" hoặc "Check-out" tùy trạng thái.
2. **Check-in.** Hệ thống kiểm tra thực tập sinh được nhận, đang trong kỳ, hôm nay chưa có bản ghi; ghi giờ máy chủ; trạng thái chuyển "Đang làm".
3. **Check-out.** Hệ thống kiểm tra hôm nay đã check-in mà chưa check-out; ghi giờ ra, tính tổng thời gian; trạng thái chuyển "Hoàn tất".
4. **Xem lịch sử.** Thực tập sinh xem theo tháng: ngày, giờ vào, giờ ra, tổng thời gian, trạng thái, nhãn "đã chỉnh sửa", và tổng giờ của tháng.
5. **Tự sửa trong hạn.** Trong thời hạn tự sửa, thực tập sinh mở bản ghi, đổi giờ vào hoặc ra (hoặc bổ sung giờ ra cho ngày thiếu check-out, hoặc thêm bản ghi cho ngày hoàn toàn chưa chấm, hoặc đặt bản ghi nhầm sang "Hủy"), nhập lý do bắt buộc. Hệ thống kiểm tra, lưu, gắn nhãn và ghi nhật ký giá trị gốc.
6. **Mentor xem.** Mentor thấy bản ghi theo ngày và tháng của các thực tập sinh mình hướng dẫn, kèm nhãn chỉnh sửa (có lý do và giá trị gốc) và các ngày thiếu check-out.
7. **HR xem và sửa quá hạn.** HR lọc theo chương trình, thực tập sinh, khoảng ngày; sửa các bản ghi đã quá hạn tự sửa (lý do bắt buộc, có nhật ký).

### Vòng đời bản ghi

`(chưa có) → Đang làm → Hoàn tất` `Đang làm → Thiếu check-out (khi sang ngày mới) → Hoàn tất (sau khi bổ sung)` `Bất kỳ → Hủy (bản ghi nhầm, trong hạn tự sửa)`

Mọi trạng thái đều sửa được trong hạn; sau khi sửa bản ghi mang nhãn "đã chỉnh sửa".

### Quy tắc nghiệp vụ

- Mỗi ngày tối đa một bản ghi. Không check-in lần hai trong ngày; không check-out khi chưa check-in hoặc đã check-out.
- Giờ do máy chủ ghi; giờ ra phải sau giờ vào và cùng ngày (**không hỗ trợ ca xuyên đêm**).
- Tổng thời gian = giờ ra trừ giờ vào, không tự trừ nghỉ trưa vì chưa có lịch nghỉ.
- Ngày thiếu check-out **không bao giờ được tự điền giờ**; hệ thống chỉ đánh dấu và hiện cho cả thực tập sinh, mentor, HR.
- Bản ghi không xóa được; bản ghi nhầm đặt sang "Hủy" (kèm lý do, trong hạn): không tính giờ nhưng vẫn còn trong nhật ký, ngày đó chấm lại được.
- Sửa cần lý do bắt buộc, trong hạn; quá hạn chỉ HR. Giá trị gốc luôn giữ trong nhật ký.
- Thực tập sinh chỉ thấy bản ghi của mình; mentor chỉ thấy của thực tập sinh mình hướng dẫn.

## 3. Ngoại lệ, tiêu chí chấp nhận, ngoài phạm vi

### Tình huống ngoại lệ

- **Bấm check-in hai lần hoặc mở hai tab:** chỉ ghi một check-in, lần còn lại báo "hôm nay đã check-in lúc HH:mm".
- **Bấm check-out hai lần:** chỉ ghi một check-out, lần còn lại báo đã check-out.
- **Check-out khi chưa check-in:** từ chối, hướng dẫn check-in trước hoặc dùng tự sửa để bổ sung.
- **Quên check-out:** sang ngày mới bản ghi chuyển "Thiếu check-out" (không tự điền giờ); thực tập sinh bổ sung trong hạn, quá hạn thì HR sửa.
- **Quên cả check-in lẫn check-out:** thực tập sinh thêm bản ghi cho ngày đó trong hạn tự sửa, nhập giờ vào, giờ ra và lý do.
- **Mất kết nối khi bấm:** nếu thao tác không tới được máy chủ thì không ghi gì và báo lỗi rõ để thực tập sinh thử lại; hệ thống không ghi giờ từ đồng hồ thiết bị.
- **Bấm chấm lúc ngoài kỳ thực tập:** từ chối, nêu rõ kỳ thực tập hiện hành.
- **Sửa giờ ra trước giờ vào, hoặc giờ ở tương lai, hoặc khác ngày:** chặn lưu và đánh dấu đúng trường sai.
- **Sửa thiếu lý do hoặc chỉ có khoảng trắng:** chặn lưu.
- **Quá hạn tự sửa:** thực tập sinh không sửa được, giao diện giải thích hạn và hướng dẫn liên hệ HR.
- **Hủy bản ghi nhầm:** thực tập sinh đặt "Hủy" trong hạn, kèm lý do; bản ghi không tính giờ nhưng vẫn còn trong nhật ký, và ngày đó có thể chấm lại nếu còn trong kỳ.
- **Ca xuyên đêm:** không hỗ trợ; thực tập sinh làm qua nửa đêm phải tách thành hai ngày bằng cách sửa giờ ra về 23:59 của ngày đầu rồi thêm bản ghi cho ngày sau.
- **Mentor đổi giữa chừng:** mentor mới thấy được toàn bộ lịch sử bản ghi của thực tập sinh, mentor cũ không còn thấy.
- **Xem bản ghi của người khác bằng cách đổi đường dẫn:** bị từ chối, không lộ sự tồn tại của thực tập sinh đó.

### Tiêu chí chấp nhận

| # | Given / When / Then |
| --- | --- |
| 1 | Thực tập sinh "Được nhận", trong kỳ, bấm check-in thì hệ thống ghi giờ máy chủ và trạng thái chuyển "Đang làm". |
| 2 | Đã check-in, bấm check-out thì ghi giờ ra, tính đúng tổng thời gian và trạng thái chuyển "Hoàn tất". |
| 3 | Thực tập sinh bấm check-in lần hai trong ngày, hoặc check-out khi chưa check-in, thì bị từ chối kèm lý do rõ. |
| 4 | Sang ngày mới mà chưa check-out thì bản ghi chuyển "Thiếu check-out" và không có giờ ra nào được tự điền. |
| 5 | Thực tập sinh thấy lịch sử theo tháng kèm tổng giờ của tháng, và nhãn "đã chỉnh sửa" ở bản ghi bị sửa. |
| 6 | Trong hạn tự sửa, thực tập sinh sửa giờ vào/ra hoặc bổ sung bản ghi kèm lý do thì lưu được, gắn nhãn, và nhật ký giữ giá trị gốc. |
| 7 | Sửa thiếu lý do, giờ ra trước giờ vào, hoặc giờ khác ngày thì không lưu được. |
| 8 | Quá hạn tự sửa thì thực tập sinh không sửa được; HR sửa được kèm lý do và nhật ký. |
| 9 | Thực tập sinh đặt bản ghi nhầm sang "Hủy" trong hạn thì bản ghi không tính giờ, còn trong nhật ký, và ngày đó chấm lại được. |
| 10 | Mentor xem được bản ghi của thực tập sinh mình hướng dẫn, kèm nhãn chỉnh sửa, lý do và giá trị gốc, và thấy các ngày thiếu check-out. |
| 11 | HR lọc theo chương trình, thực tập sinh, khoảng ngày để xem và sửa bản ghi. |
| 12 | Thực tập sinh không xem được bản ghi người khác; mentor không xem được thực tập sinh không thuộc mình. |
| 13 | Bấm check-in hoặc check-out hai lần liên tiếp chỉ tạo một lần ghi. |

### Yêu cầu phi chức năng tối thiểu

- Giờ ghi nhận lấy từ máy chủ, không tin đồng hồ thiết bị.
- Check-in/out an toàn khi bấm lặp hoặc mất kết nối: không tạo bản ghi trùng hay dở dang.
- Nhật ký chấm công không sửa được từ giao diện.
- Quyền xem kiểm tra ở phía máy chủ.

### Ngoài phạm vi (để story sau)

- Xác minh vị trí hoặc mạng.
- Lịch làm việc và ca chuẩn, đi muộn về sớm.
- Nghỉ phép và nghỉ lễ.
- Tính lương hay phụ cấp.
- Mentor duyệt từng bản ghi.
- Nhắc check-in/out.
- Báo cáo tổng hợp và xuất file.
- Nhiều lượt mỗi ngày, ca xuyên đêm.

## 4. Tác động lên các tài liệu trước

- **Phân công mentor:** mentor có thêm quyền xem chấm công của các thực tập sinh mình hướng dẫn; đổi mentor thì mentor mới thấy toàn bộ lịch sử, mentor cũ không còn thấy.
- **Hợp đồng:** ngày kết thúc hợp đồng giới hạn kỳ được chấm công (nguồn ưu tiên hợp đồng rồi chương trình); xóa hoặc sửa ngày hợp đồng sẽ đổi kỳ được chấm công từ lần chấm sau.

## 5. Cập nhật theo story "HR xem báo cáo đi làm và nghỉ phép"

- **Báo cáo tổng hợp:** mục "báo cáo tổng hợp" đã chuyển từ ngoài phạm vi sang đã làm cho HR (xuất file và mentor xem báo cáo vẫn ngoài phạm vi). Bản ghi chấm công là nguồn của báo cáo; ngày vắng được xác định bằng thứ làm việc của chương trình và danh sách ngày nghỉ chung.
- **Ngày nghỉ chung:** chấm công vào ngày nghỉ chung vẫn được ghi nhận bình thường và cộng vào tổng giờ, nhưng không tính vào số ngày đi làm kỳ vọng của báo cáo.