from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from pathlib import Path
import textwrap

OUT = Path("outputs/sprint3")
OUT.mkdir(parents=True, exist_ok=True)
OUTFILE = OUT / "Sprint_3_Wireframe_Bo_Cuc_Giao_Dien.docx"

stories = [
 {"id":"US-S3-01","title":"Mentor giao nhiệm vụ cho thực tập sinh","story":"Là mentor, tôi muốn giao nhiệm vụ cho thực tập sinh để họ có công việc cụ thể.","screen":"Quản lý nhiệm vụ","flow":["Chọn thực tập sinh","Nhập nội dung và thời hạn","Giao nhiệm vụ","Theo dõi trạng thái"],"filters":"Tìm kiếm | Thực tập sinh | Trạng thái | Hạn hoàn thành","body":["Danh sách nhiệm vụ: tiêu đề, người nhận, ưu tiên, thời hạn, trạng thái","Nút: Tạo nhiệm vụ","Form: tiêu đề, mô tả, người nhận, ưu tiên, ngày bắt đầu, hạn hoàn thành, tệp đính kèm"],"states":"Trống; đang tải; giao thành công; dữ liệu không hợp lệ; không có quyền"},
 {"id":"US-S3-02","title":"Thực tập sinh cập nhật tiến độ công việc","story":"Là thực tập sinh, tôi muốn cập nhật tiến độ công việc để mentor theo dõi.","screen":"Công việc của tôi","flow":["Mở nhiệm vụ","Cập nhật phần trăm và trạng thái","Ghi chú kết quả","Lưu cập nhật"],"filters":"Trạng thái | Ưu tiên | Thời hạn","body":["Thẻ nhiệm vụ: mô tả, hạn hoàn thành, người giao","Thanh tiến độ và trạng thái: Chưa làm / Đang làm / Hoàn thành / Bị chặn","Form cập nhật: phần trăm, ghi chú, liên kết hoặc tệp minh chứng"],"states":"Chưa bắt đầu; đang thực hiện; bị chặn; hoàn thành; quá hạn"},
 {"id":"US-S3-03","title":"Thực tập sinh nộp báo cáo tuần","story":"Là thực tập sinh, tôi muốn nộp báo cáo tuần để báo cáo kết quả thực tập.","screen":"Báo cáo tuần của tôi","flow":["Chọn tuần báo cáo","Nhập kết quả và khó khăn","Đính kèm tài liệu","Nộp báo cáo"],"filters":"Tuần | Trạng thái báo cáo","body":["Thông tin tuần: từ ngày, đến ngày, tổng thời gian","Các mục: việc đã làm, kết quả, khó khăn, kế hoạch tuần tới","Nút: Lưu nháp / Nộp báo cáo"],"states":"Nháp; đã nộp; cần chỉnh sửa; đã được mentor phản hồi; quá hạn"},
 {"id":"US-S3-04","title":"Mentor xem báo cáo và phản hồi","story":"Là mentor, tôi muốn xem báo cáo và phản hồi để hỗ trợ thực tập sinh.","screen":"Duyệt báo cáo tuần","flow":["Chọn thực tập sinh và tuần","Đọc báo cáo","Nhập phản hồi","Xác nhận hoặc yêu cầu chỉnh sửa"],"filters":"Thực tập sinh | Tuần | Trạng thái","body":["Danh sách báo cáo: người nộp, tuần, ngày nộp, trạng thái","Khung chi tiết báo cáo và tệp đính kèm","Khung phản hồi: nhận xét, đề xuất, trạng thái xử lý"],"states":"Chưa xem; đã xem; cần chỉnh sửa; đã phản hồi"},
 {"id":"US-S3-05","title":"Mentor đánh giá kỹ năng và thái độ","story":"Là mentor, tôi muốn đánh giá kỹ năng và thái độ của thực tập sinh để tổng kết.","screen":"Đánh giá thực tập sinh","flow":["Chọn thực tập sinh","Chấm từng tiêu chí","Nhập nhận xét","Lưu hoặc hoàn tất đánh giá"],"filters":"Chương trình | Thực tập sinh | Kỳ đánh giá","body":["Nhóm kỹ năng: chuyên môn, giải quyết vấn đề, quản lý thời gian","Nhóm thái độ: chủ động, hợp tác, trách nhiệm, kỷ luật","Thang điểm, nhận xét chung, điểm mạnh, điểm cần cải thiện"],"states":"Chưa đánh giá; đang đánh giá; đã hoàn tất; đã khóa"},
 {"id":"US-S3-06","title":"HR tổng hợp báo cáo cuối kỳ","story":"Là HR, tôi muốn tổng hợp đánh giá thành báo cáo cuối kỳ để gửi cho trường hoặc ban lãnh đạo.","screen":"Báo cáo cuối kỳ","flow":["Chọn chương trình hoặc kỳ","Kiểm tra dữ liệu đánh giá","Tổng hợp kết quả","Xuất báo cáo"],"filters":"Chương trình | Phòng ban | Trường | Trạng thái đánh giá","body":["Chỉ số: số TTS, số đã đánh giá, điểm trung bình, tỷ lệ hoàn thành","Bảng tổng hợp theo thực tập sinh và mentor","Nút: Xem chi tiết / Xuất Excel / Xuất PDF"],"states":"Thiếu đánh giá; dữ liệu đầy đủ; đang xuất; xuất thành công"},
 {"id":"US-S3-07","title":"HR thiết lập lịch làm việc linh hoạt","story":"Là HR, tôi muốn thiết lập lịch làm việc linh hoạt để phù hợp với từng nhóm.","screen":"Thiết lập lịch làm việc","flow":["Chọn nhóm hoặc chương trình","Thiết lập ngày và khung giờ","Thêm ngoại lệ","Lưu và áp dụng lịch"],"filters":"Chương trình | Phòng ban | Nhóm | Khoảng thời gian","body":["Lịch tuần: ngày làm, giờ bắt đầu, giờ kết thúc, hình thức làm việc","Ngoại lệ: ngày nghỉ, đổi ca, làm bù","Phạm vi áp dụng và ngày hiệu lực"],"states":"Lịch mặc định; lịch tùy chỉnh; xung đột; chưa áp dụng; đã áp dụng"},
 {"id":"US-S3-08","title":"Thực tập sinh đăng ký nghỉ phép","story":"Là thực tập sinh, tôi muốn đăng ký nghỉ phép để báo trước cho HR.","screen":"Đăng ký nghỉ phép","flow":["Chọn loại nghỉ và thời gian","Nhập lý do","Gửi yêu cầu","Theo dõi kết quả"],"filters":"Tháng | Trạng thái yêu cầu","body":["Số ngày đã nghỉ và đang chờ xử lý","Form: loại nghỉ, từ ngày, đến ngày, buổi nghỉ, lý do, tệp đính kèm","Lịch sử yêu cầu: thời gian, trạng thái, người xử lý"],"states":"Nháp; chờ duyệt; đã duyệt; từ chối; hủy; trùng lịch"},
 {"id":"US-S3-09","title":"HR thêm mới mentor","story":"Là HR, tôi muốn thêm mới mentor để phân công cho thực tập sinh.","screen":"Quản lý mentor","flow":["Mở form thêm mentor","Nhập thông tin","Gán phòng ban và quyền","Lưu mentor"],"filters":"Tìm kiếm | Phòng ban | Trạng thái","body":["Danh sách mentor: họ tên, email, phòng ban, trạng thái, số TTS","Form: thông tin cá nhân, tài khoản, phòng ban, chuyên môn","Nút: Thêm mentor / Chỉnh sửa / Khóa tài khoản"],"states":"Đang hoạt động; chưa kích hoạt; đã khóa; email trùng"},
 {"id":"US-S3-10","title":"HR gán mentor cho thực tập sinh","story":"Là HR, tôi muốn gán mentor cho thực tập sinh để họ được hướng dẫn.","screen":"Phân công mentor","flow":["Chọn thực tập sinh","Xem mentor phù hợp và tải hiện tại","Chọn mentor","Xác nhận phân công"],"filters":"Chương trình | Phòng ban | Mentor | Trạng thái phân công","body":["Danh sách TTS chưa hoặc đã có mentor","Bảng mentor đề xuất: chuyên môn, phòng ban, số TTS hiện tại","Nút: Gán mentor / Đổi mentor / Xem lịch sử"],"states":"Chưa phân công; đã phân công; mentor không hoạt động; khác phòng ban"},
 {"id":"US-S3-11","title":"HR theo dõi tải quản lý của mentor","story":"Là HR, tôi muốn xem số lượng thực tập sinh mỗi mentor quản lý để cân bằng khối lượng công việc.","screen":"Tải quản lý của mentor","flow":["Chọn chương trình hoặc phòng ban","So sánh số lượng TTS","Mở danh sách chi tiết","Điều chỉnh phân công"],"filters":"Chương trình | Phòng ban | Trạng thái mentor","body":["Thẻ tổng quan: tổng mentor, tổng TTS, tải trung bình","Biểu đồ hoặc thanh tải theo mentor","Bảng: mentor, số TTS, giới hạn đề xuất, trạng thái tải"],"states":"Chưa có dữ liệu; tải bình thường; gần quá tải; quá tải"},
 {"id":"US-S3-12","title":"Hệ thống gửi email khi có lịch họp","story":"Là hệ thống, tôi muốn gửi email tự động khi có lịch họp để thông báo cho thực tập sinh.","screen":"Cấu hình và nhật ký email lịch họp","flow":["Nhận sự kiện tạo hoặc đổi lịch","Xác định người nhận","Xếp hàng gửi email","Ghi nhận kết quả"],"filters":"Khoảng ngày | Trạng thái gửi | Loại sự kiện","body":["Cấu hình: mẫu email, thời điểm nhắc, số lần gửi lại","Nhật ký: lịch họp, người nhận, thời gian gửi, trạng thái, lỗi","Nút quản trị: Gửi lại / Hủy email đang chờ"],"states":"Đang chờ; đã gửi; gửi lại; thất bại; đã hủy"},
 {"id":"US-S3-13","title":"Thực tập sinh nhận thông báo trên ứng dụng","story":"Là thực tập sinh, tôi muốn nhận thông báo trên ứng dụng để không bỏ lỡ lịch trình.","screen":"Trung tâm thông báo","flow":["Hệ thống tạo thông báo","Hiển thị số chưa đọc","Mở thông báo","Đi đến nội dung liên quan"],"filters":"Tất cả | Chưa đọc | Lịch họp | Công việc | Báo cáo | Nghỉ phép","body":["Biểu tượng chuông và số thông báo chưa đọc","Danh sách: tiêu đề, nội dung ngắn, thời gian, loại, trạng thái đọc","Nút: Đánh dấu đã đọc / Đánh dấu tất cả / Mở chi tiết"],"states":"Không có thông báo; chưa đọc; đã đọc; liên kết không còn hiệu lực"},
]

def shade(cell, fill):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = tcPr.find(qn('w:shd'))
    if shd is None:
        shd = OxmlElement('w:shd'); tcPr.append(shd)
    shd.set(qn('w:fill'), fill)

def set_cell_text(cell, text, bold=False, color="1F2937", size=9, align=None):
    cell.text = ""
    p = cell.paragraphs[0]
    if align is not None: p.alignment = align
    r = p.add_run(text); r.bold = bold; r.font.name = "Arial"; r.font.size = Pt(size); r.font.color.rgb = RGBColor.from_string(color)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER

def ascii_wireframe(item):
    layouts = {
      "US-S3-01": {"toolbar":"[ Tìm nhiệm vụ... ] [ Thực tập sinh ▼ ] [ Trạng thái ▼ ]   [ + TẠO NHIỆM VỤ ]","summary":"Tất cả: 24     Đang làm: 10     Quá hạn: 3     Hoàn thành: 11","table":["□  Nhiệm vụ                 Người nhận       Ưu tiên   Hạn       Trạng thái","□  Hoàn thiện API báo cáo   Nguyễn An         Cao       18/10     Đang làm","□  Kiểm thử form đăng ký    Trần Bình         Vừa      20/10     Chưa làm"],"panel":["TẠO NHIỆM VỤ","Tiêu đề       [________________________________________]","Người nhận    [ Chọn thực tập sinh ▼ ]   Ưu tiên [ Cao ▼ ]","Thời gian     [ 15/10/2026 ]  đến  [ 20/10/2026 ]","Mô tả         [________________________________________]","Tệp đính kèm  [ Chọn tệp ]"],"actions":"[ HỦY ]                                      [ GIAO NHIỆM VỤ ]"},
      "US-S3-02": {"toolbar":"[ Tìm công việc... ] [ Trạng thái ▼ ] [ Thời hạn ▼ ]","summary":"Việc của tôi: 8      Đang làm: 3      Bị chặn: 1      Hoàn thành: 4","table":["NHIỆM VỤ: Hoàn thiện API báo cáo tuần          Hạn: 18/10/2026","Tiến độ  [██████████████░░░░░░] 70%    Trạng thái [ Đang làm ▼ ]","Người giao: Mentor A     Ưu tiên: Cao"],"panel":["CẬP NHẬT TIẾN ĐỘ","Phần trăm      [ 70% ]","Kết quả        [________________________________________]","Khó khăn       [________________________________________]","Minh chứng     [ Dán liên kết ] [ Chọn tệp ]"],"actions":"[ BÁO BỊ CHẶN ]                         [ LƯU CẬP NHẬT ]"},
      "US-S3-03": {"toolbar":"[ Tuần 14/10 - 20/10 ▼ ] [ Trạng thái ▼ ]       [ + TẠO BÁO CÁO ]","summary":"Báo cáo hiện tại: NHÁP                  Hạn nộp: 18:00 - 20/10/2026","table":["VIỆC ĐÃ HOÀN THÀNH","[____________________________________________________________]","KẾT QUẢ / MINH CHỨNG","[____________________________________________________________]"],"panel":["KHÓ KHĂN / HỖ TRỢ CẦN THIẾT","[____________________________________________________________]","KẾ HOẠCH TUẦN TỚI","[____________________________________________________________]","Tệp đính kèm  [ Chọn tệp ]"],"actions":"[ LƯU NHÁP ]                               [ NỘP BÁO CÁO ]"},
      "US-S3-04": {"toolbar":"[ Thực tập sinh ▼ ] [ Tuần ▼ ] [ Trạng thái ▼ ] [ Tìm kiếm... ]","summary":"Chờ xem: 4        Cần chỉnh sửa: 2        Đã phản hồi: 15","table":["Người nộp       Tuần       Ngày nộp       Trạng thái","Nguyễn An       Tuần 3     18/10 16:20    ● Chưa xem","Trần Bình       Tuần 3     18/10 15:05    ◐ Cần chỉnh sửa"],"panel":["CHI TIẾT BÁO CÁO                    PHẢN HỒI CỦA MENTOR","• Việc đã làm                       Nhận xét [________________]","• Kết quả                           Đề xuất  [________________]","• Khó khăn                          [ ] Yêu cầu chỉnh sửa"],"actions":"[ LƯU PHẢN HỒI ]                         [ XÁC NHẬN BÁO CÁO ]"},
      "US-S3-05": {"toolbar":"[ Chương trình ▼ ] [ Thực tập sinh ▼ ] [ Kỳ đánh giá ▼ ]","summary":"Thực tập sinh: Nguyễn An      Mentor: Lê Minh      Kỳ: Cuối kỳ","table":["TIÊU CHÍ                         1   2   3   4   5      NHẬN XÉT","Chuyên môn                       ○   ○   ○   ●   ○      [__________]","Giải quyết vấn đề                ○   ○   ○   ●   ○      [__________]","Chủ động và trách nhiệm          ○   ○   ○   ○   ●      [__________]","Hợp tác và kỷ luật               ○   ○   ○   ●   ○      [__________]"],"panel":["Điểm mạnh       [________________________________________]","Cần cải thiện   [________________________________________]","Nhận xét chung  [________________________________________]"],"actions":"[ LƯU NHÁP ]                              [ HOÀN TẤT ĐÁNH GIÁ ]"},
      "US-S3-06": {"toolbar":"[ Chương trình ▼ ] [ Phòng ban ▼ ] [ Trường ▼ ] [ Trạng thái ▼ ]","summary":"TTS: 42      Đã đánh giá: 38      Thiếu: 4      Điểm TB: 4.1/5","table":["Thực tập sinh     Mentor       Kỹ năng   Thái độ   Tổng   Trạng thái","Nguyễn An         Lê Minh       4.2        4.5       4.3    Đủ dữ liệu","Trần Bình         Mai Lan       --         4.0       --     Thiếu đánh giá"],"panel":["XEM CHI TIẾT: kết quả thực tập, điểm từng tiêu chí, nhận xét mentor","Mẫu báo cáo [ Trường ▼ ]      Định dạng [ PDF ▼ ]","[ ] Kèm phụ lục đánh giá chi tiết"],"actions":"[ XUẤT EXCEL ]                     [ TẠO BÁO CÁO CUỐI KỲ ]"},
      "US-S3-07": {"toolbar":"[ Chương trình ▼ ] [ Phòng ban ▼ ] [ Nhóm ▼ ] [ Tháng 10/2026 ▼ ]","summary":"Lịch áp dụng: 01/10 - 31/12/2026            Nhóm: Backend Intern","table":["NGÀY       LÀM VIỆC   BẮT ĐẦU   KẾT THÚC   HÌNH THỨC","Thứ 2      [✓]        08:30     17:30      Tại văn phòng ▼","Thứ 3      [✓]        08:30     17:30      Từ xa ▼","Thứ 4      [✓]        09:00     18:00      Linh hoạt ▼"],"panel":["NGOẠI LỆ LỊCH LÀM VIỆC","[ + Thêm ngày nghỉ / đổi ca / làm bù ]","Ngày [__/__/____]  Loại [________▼]  Thời gian [________]"],"actions":"[ XEM TRƯỚC ]                              [ ÁP DỤNG LỊCH ]"},
      "US-S3-08": {"toolbar":"[ Tháng 10/2026 ▼ ] [ Trạng thái ▼ ]          [ + ĐĂNG KÝ NGHỈ ]","summary":"Đã nghỉ: 1 ngày       Đang chờ: 0.5 ngày       Còn lại: Theo chính sách","table":["Từ ngày       Đến ngày       Loại nghỉ       Số ngày    Trạng thái","21/10/2026    21/10/2026     Nghỉ phép       1          Chờ duyệt","05/10/2026    05/10/2026     Nghỉ ốm         0.5        Đã duyệt"],"panel":["ĐĂNG KÝ NGHỈ PHÉP","Loại nghỉ [ Nghỉ phép ▼ ]     Thời gian [ Cả ngày ▼ ]","Từ ngày   [__/__/____]        Đến ngày  [__/__/____]","Lý do     [________________________________________]","Minh chứng [ Chọn tệp ]"],"actions":"[ HỦY ]                                   [ GỬI YÊU CẦU ]"},
      "US-S3-09": {"toolbar":"[ Tìm mentor... ] [ Phòng ban ▼ ] [ Trạng thái ▼ ]   [ + THÊM MENTOR ]","summary":"Tổng mentor: 18       Hoạt động: 16       Chưa kích hoạt: 2","table":["Họ tên          Email                 Phòng ban    Số TTS   Trạng thái","Lê Minh         minh@company.vn       Công nghệ    3        Hoạt động","Mai Lan         lan@company.vn        Marketing    2        Hoạt động"],"panel":["THÊM MENTOR","Họ tên [________________]      Email [____________________]","Phòng ban [____________▼]      Chuyên môn [_______________]","Số điện thoại [___________]    [✓] Gửi email kích hoạt"],"actions":"[ HỦY ]                                     [ LƯU MENTOR ]"},
      "US-S3-10": {"toolbar":"[ Chương trình ▼ ] [ Phòng ban ▼ ] [ Chưa phân công ▼ ]","summary":"Chưa có mentor: 6       Đã phân công: 36       Mentor khả dụng: 14","table":["THỰC TẬP SINH                 MENTOR ĐỀ XUẤT / TẢI HIỆN TẠI","Nguyễn An - Backend             Lê Minh (3 TTS)     [ Chọn ▼ ]","Trần Bình - Backend             Hoàng Nam (2 TTS)   [ Chọn ▼ ]"],"panel":["CHI TIẾT PHÂN CÔNG","Thực tập sinh: Nguyễn An      Mentor mới: [ Lê Minh ▼ ]","Lý do thay đổi [________________________________________]","Lịch sử: Chưa có phân công trước đó"],"actions":"[ XEM LỊCH SỬ ]                         [ XÁC NHẬN PHÂN CÔNG ]"},
      "US-S3-11": {"toolbar":"[ Chương trình ▼ ] [ Phòng ban ▼ ] [ Trạng thái mentor ▼ ]","summary":"Mentor: 18       TTS: 42       Trung bình: 2.3       Quá tải: 2","table":["Mentor          Phòng ban     Số TTS / Giới hạn       Mức tải","Lê Minh         Công nghệ     3 / 4   [███████░░░] 75%   Bình thường","Hoàng Nam       Công nghệ     5 / 4   [██████████] 125%  Quá tải","Mai Lan         Marketing     2 / 4   [█████░░░░░] 50%   Bình thường"],"panel":["DANH SÁCH TTS CỦA MENTOR ĐANG CHỌN","Nguyễn An | Trần Bình | Phạm Hoa","Gợi ý: Chuyển 1 TTS sang mentor có tải thấp hơn"],"actions":"[ XEM CHI TIẾT ]                         [ ĐIỀU CHỈNH PHÂN CÔNG ]"},
      "US-S3-12": {"toolbar":"[ Khoảng ngày ▼ ] [ Trạng thái gửi ▼ ] [ Loại sự kiện ▼ ]","summary":"Đang chờ: 8       Đã gửi: 124       Thất bại: 2       Đã hủy: 1","table":["Lịch họp            Người nhận   Gửi lúc       Trạng thái   Thao tác","Họp tuần Backend     12 người     08:00 20/10  Đang chờ    [Hủy]","Review cuối kỳ       8 người      09:00 18/10  Thất bại    [Gửi lại]"],"panel":["CẤU HÌNH EMAIL NHẮC LỊCH","Mẫu email [ Lịch họp mặc định ▼ ]","Gửi trước [ 24 giờ ▼ ]   Gửi lại tối đa [ 3 ] lần","[✓] Gửi lại khi thời gian hoặc địa điểm thay đổi"],"actions":"[ LƯU CẤU HÌNH ]                          [ GỬI THỬ EMAIL ]"},
      "US-S3-13": {"toolbar":"[ Tất cả ] [ Chưa đọc ] [ Lịch họp ] [ Công việc ] [ Báo cáo ] [ Nghỉ phép ]","summary":"🔔 5 thông báo chưa đọc                         [ Đánh dấu tất cả đã đọc ]","table":["● Lịch họp tuần Backend thay đổi giờ              5 phút trước","  09:00 ngày 20/10 tại Phòng Họp 2               [ Xem lịch ]","● Bạn được giao nhiệm vụ mới                      20 phút trước","  Hoàn thiện API báo cáo tuần - hạn 22/10          [ Mở công việc ]","○ Yêu cầu nghỉ phép đã được duyệt                  Hôm qua"],"panel":["CHI TIẾT THÔNG BÁO","Tiêu đề, nội dung đầy đủ, người tạo và thời gian","Liên kết đến lịch họp / công việc / báo cáo / yêu cầu nghỉ"],"actions":"[ ĐÁNH DẤU ĐÃ ĐỌC ]                    [ MỞ NỘI DUNG LIÊN QUAN ]"},
    }
    ui = layouts[item['id']]
    width, side = 100, 18
    content = width - side - 3
    def border(char='-'): return "+" + char * (width-2) + "+"
    def full(text=""): return "|" + text[:width-2].ljust(width-2) + "|"
    def split(side_text, main_text=""):
        return "|" + side_text[:side].ljust(side) + "|" + main_text[:content].ljust(content) + "|"
    lines=[border(),full(f" INTERN MANAGEMENT     {item['screen'].upper()}"),border(),split(" ĐIỀU HƯỚNG",ui['toolbar']),split(" • Tổng quan",ui['summary']),split(" • Công việc"),split(" • Báo cáo","-"*content)]
    for i,text in enumerate(ui['table']): lines.append(split(" • Lịch" if i==0 else "",text))
    lines += [split(" • Thông báo","-"*content)]
    for i,text in enumerate(ui['panel']): lines.append(split(" • Tài khoản" if i==0 else "",text))
    lines += [border(),full(" " + ui['actions']),border()]
    return "\n".join(lines)

def add_wireframe(doc, item):
    p = doc.add_paragraph()
    p.style = doc.styles['Heading 1']
    p.add_run(f"{item['id']}  {item['title']}")
    p = doc.add_paragraph(item['story']); p.style = doc.styles['Quote']

    t = doc.add_table(rows=2, cols=2); t.alignment = WD_TABLE_ALIGNMENT.CENTER; t.autofit = False
    t.columns[0].width = Inches(1.35); t.columns[1].width = Inches(5.8)
    set_cell_text(t.cell(0,0),"Màn hình chính",True,"FFFFFF"); shade(t.cell(0,0),"356A93")
    set_cell_text(t.cell(0,1),item['screen'],True)
    set_cell_text(t.cell(1,0),"Luồng chính",True,"FFFFFF"); shade(t.cell(1,0),"356A93")
    set_cell_text(t.cell(1,1),"  →  ".join(item['flow']))
    doc.add_paragraph()

    h = doc.add_paragraph("Bố cục giao diện đề xuất"); h.style = doc.styles['Heading 2']
    code = doc.add_paragraph()
    code.paragraph_format.space_before = Pt(0)
    code.paragraph_format.space_after = Pt(8)
    code.paragraph_format.line_spacing = 1.0
    code.paragraph_format.keep_together = True
    ppr = code._p.get_or_add_pPr()
    shd = OxmlElement('w:shd'); shd.set(qn('w:fill'), 'F3F4F6'); ppr.append(shd)
    pbdr = OxmlElement('w:pBdr')
    for edge in ('top','left','bottom','right'):
        el=OxmlElement(f'w:{edge}'); el.set(qn('w:val'),'single'); el.set(qn('w:sz'),'6'); el.set(qn('w:color'),'CBD5E1'); pbdr.append(el)
    ppr.append(pbdr)
    run = code.add_run(ascii_wireframe(item))
    run.font.name = "Courier New"; run.font.size = Pt(7.5); run.font.color.rgb = RGBColor(31,41,55)

    p=doc.add_paragraph(); p.add_run("Trạng thái cần thiết kế: ").bold=True; p.add_run(item['states'])
    p=doc.add_paragraph(); p.add_run("Quy tắc trải nghiệm: ").bold=True; p.add_run("Kiểm tra quyền ở phía máy chủ; xác nhận trước thao tác quan trọng; giữ dữ liệu người dùng khi có lỗi; hỗ trợ màn hình trống, đang tải và thử lại.")

doc=Document()
sec=doc.sections[0]; sec.top_margin=Inches(0.65); sec.bottom_margin=Inches(0.65); sec.left_margin=Inches(0.7); sec.right_margin=Inches(0.7)
styles=doc.styles
styles['Normal'].font.name='Arial'; styles['Normal'].font.size=Pt(10)
styles['Title'].font.name='Arial'; styles['Title'].font.size=Pt(28); styles['Title'].font.bold=True; styles['Title'].font.color.rgb=RGBColor(22,58,95)
styles['Heading 1'].font.name='Arial'; styles['Heading 1'].font.size=Pt(17); styles['Heading 1'].font.bold=True; styles['Heading 1'].font.color.rgb=RGBColor(22,58,95)
styles['Heading 2'].font.name='Arial'; styles['Heading 2'].font.size=Pt(12); styles['Heading 2'].font.bold=True; styles['Heading 2'].font.color.rgb=RGBColor(53,106,147)
styles['Quote'].font.name='Arial'; styles['Quote'].font.size=Pt(10); styles['Quote'].font.italic=True; styles['Quote'].font.color.rgb=RGBColor(71,85,105)

p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER; p.space_after=Pt(12)
r=p.add_run("TÀI LIỆU THIẾT KẾ BỐ CỤC GIAO DIỆN"); r.font.name='Arial'; r.font.size=Pt(25); r.bold=True; r.font.color.rgb=RGBColor(22,58,95)
p=doc.add_paragraph(); p.alignment=WD_ALIGN_PARAGRAPH.CENTER
r=p.add_run("Hệ thống quản lý thực tập - Sprint 3"); r.font.name='Arial'; r.font.size=Pt(17); r.font.color.rgb=RGBColor(53,106,147)
doc.add_paragraph()
t=doc.add_table(rows=4,cols=2); t.alignment=WD_TABLE_ALIGNMENT.CENTER
for label,value,row in [("Phạm vi","13 user story",0),("Mục đích","Định hướng luồng và bố cục giao diện trước khi thiết kế chi tiết",1),("Đối tượng","HR, Mentor, Thực tập sinh và tác vụ hệ thống",2),("Phiên bản","Sprint 3",3)]:
    set_cell_text(t.cell(row,0),label,True,"FFFFFF",10); shade(t.cell(row,0),"356A93"); set_cell_text(t.cell(row,1),value,False,"1F2937",10)
doc.add_page_break()

p=doc.add_paragraph("Nguyên tắc thiết kế"); p.style=styles['Heading 1']
for x in ["Mỗi vai trò chỉ nhìn thấy dữ liệu và hành động thuộc phạm vi được phân quyền.","Mọi danh sách dài có tìm kiếm, bộ lọc, phân trang và trạng thái trống.","Form hiển thị lỗi sát trường dữ liệu và không làm mất nội dung đã nhập.","Các thao tác gửi, duyệt, phân công hoặc hoàn tất phải có xác nhận và phản hồi rõ ràng.","Thiết kế đáp ứng cho máy tính và màn hình di động; ưu tiên bảng trên máy tính và thẻ trên di động."]:
    doc.add_paragraph(x,style='List Bullet')
p=doc.add_paragraph("Danh sách màn hình"); p.style=styles['Heading 2']
tbl=doc.add_table(rows=1,cols=3); tbl.alignment=WD_TABLE_ALIGNMENT.CENTER
for i,h in enumerate(["Mã","Vai trò","Màn hình chính"]): set_cell_text(tbl.cell(0,i),h,True,"FFFFFF",9,WD_ALIGN_PARAGRAPH.CENTER); shade(tbl.cell(0,i),"356A93")
for item in stories:
    row=tbl.add_row().cells
    role=item['story'].split(',')[0].replace('Là ','')
    for i,v in enumerate([item['id'],role,item['screen']]): set_cell_text(row[i],v,False,"1F2937",9)

for item in stories:
    doc.add_page_break(); add_wireframe(doc,item)

for section in doc.sections:
    footer=section.footer.paragraphs[0]; footer.alignment=WD_ALIGN_PARAGRAPH.CENTER
    rr=footer.add_run("Intern Management - Sprint 3 | Tài liệu wireframe"); rr.font.name='Arial'; rr.font.size=Pt(8); rr.font.color.rgb=RGBColor(100,116,139)

doc.core_properties.title="Tài liệu thiết kế bố cục giao diện Sprint 3"
doc.core_properties.subject="Wireframe giao diện theo user story"
doc.core_properties.author="Intern Management Team"
doc.save(OUTFILE)
print(OUTFILE)
