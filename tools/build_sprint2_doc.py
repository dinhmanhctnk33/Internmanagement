from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.section import WD_SECTION, WD_ORIENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.enum.style import WD_STYLE_TYPE
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "Sprint_2_Backlog_va_Wireframe.docx"
BLUE = "1F4E78"
LIGHT = "D9EAF7"
PALE = "F4F8FC"
GRAY = "D9D9D9"
WHITE = "FFFFFF"

def set_font(run, bold=None, color="000000"):
    run.font.name = "Times New Roman"
    run._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), "Times New Roman")
    run._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), "Times New Roman")
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), "Times New Roman")
    run.font.size = Pt(13)
    run.font.color.rgb = RGBColor.from_string(color)
    if bold is not None:
        run.bold = bold

def shade(cell, fill):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = tcPr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tcPr.append(shd)
    shd.set(qn("w:fill"), fill)

def borders(cell, color=GRAY, size="6"):
    tcPr = cell._tc.get_or_add_tcPr()
    b = tcPr.find(qn("w:tcBorders"))
    if b is None:
        b = OxmlElement("w:tcBorders")
        tcPr.append(b)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = qn(f"w:{edge}")
        el = b.find(tag)
        if el is None:
            el = OxmlElement(f"w:{edge}")
            b.append(el)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), size)
        el.set(qn("w:color"), color)

def margins(cell, top=100, start=120, bottom=100, end=120):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = tcPr.find(qn("w:tcMar"))
    if tcMar is None:
        tcMar = OxmlElement("w:tcMar")
        tcPr.append(tcMar)
    for name, val in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        el = tcMar.find(qn(f"w:{name}"))
        if el is None:
            el = OxmlElement(f"w:{name}")
            tcMar.append(el)
        el.set(qn("w:w"), str(val)); el.set(qn("w:type"), "dxa")

def set_cell_text(cell, text, bold=False, color="000000", align=WD_ALIGN_PARAGRAPH.LEFT):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = align
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.05
    r = p.add_run(str(text))
    set_font(r, bold, color)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
    borders(cell); margins(cell)

def set_repeat_header(row):
    trPr = row._tr.get_or_add_trPr()
    tblHeader = OxmlElement("w:tblHeader")
    tblHeader.set(qn("w:val"), "true")
    trPr.append(tblHeader)

def table(doc, headers, rows, widths=None, header_fill=BLUE):
    t = doc.add_table(rows=1, cols=len(headers))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = False
    for i, h in enumerate(headers):
        set_cell_text(t.rows[0].cells[i], h, True, WHITE, WD_ALIGN_PARAGRAPH.CENTER)
        shade(t.rows[0].cells[i], header_fill)
        if widths: t.rows[0].cells[i].width = Inches(widths[i])
    set_repeat_header(t.rows[0])
    for ri, row in enumerate(rows):
        cells = t.add_row().cells
        for i, val in enumerate(row):
            set_cell_text(cells[i], val, False, "000000", WD_ALIGN_PARAGRAPH.CENTER if i == 0 else WD_ALIGN_PARAGRAPH.LEFT)
            if ri % 2: shade(cells[i], PALE)
            if widths: cells[i].width = Inches(widths[i])
    doc.add_paragraph().paragraph_format.space_after = Pt(0)
    return t

def p(doc, text="", bold=False, align=None, before=0, after=6, keep=False):
    par = doc.add_paragraph()
    if align is not None: par.alignment = align
    par.paragraph_format.space_before = Pt(before)
    par.paragraph_format.space_after = Pt(after)
    par.paragraph_format.line_spacing = 1.15
    par.paragraph_format.keep_with_next = keep
    r = par.add_run(text); set_font(r, bold)
    return par

def heading(doc, text, level=1):
    par = doc.add_paragraph(style=f"Heading {level}")
    par.paragraph_format.space_before = Pt(10 if level == 1 else 7)
    par.paragraph_format.space_after = Pt(5)
    par.paragraph_format.keep_with_next = True
    r = par.add_run(text); set_font(r, True)
    return par

def bullets(doc, items):
    for item in items:
        par = doc.add_paragraph(style="List Bullet")
        par.paragraph_format.space_after = Pt(3)
        par.paragraph_format.line_spacing = 1.1
        set_font(par.add_run(item))

def numbered(doc, items):
    for item in items:
        par = doc.add_paragraph(style="List Number")
        par.paragraph_format.space_after = Pt(3)
        set_font(par.add_run(item))

def landscape(doc):
    sec = doc.add_section(WD_SECTION.NEW_PAGE)
    sec.orientation = WD_ORIENT.LANDSCAPE
    sec.page_width, sec.page_height = Inches(11), Inches(8.5)
    sec.top_margin = sec.bottom_margin = Inches(.55)
    sec.left_margin = sec.right_margin = Inches(.55)
    return sec

def portrait(doc):
    sec = doc.add_section(WD_SECTION.NEW_PAGE)
    sec.orientation = WD_ORIENT.PORTRAIT
    sec.page_width, sec.page_height = Inches(8.5), Inches(11)
    sec.top_margin = sec.bottom_margin = Inches(.7)
    sec.left_margin = sec.right_margin = Inches(.75)
    return sec

def wf_box(doc, title, rows, note=None):
    heading(doc, title, 2)
    outer = doc.add_table(rows=1, cols=1)
    outer.alignment = WD_TABLE_ALIGNMENT.CENTER
    c = outer.cell(0,0); borders(c, "7F8C8D", "10"); margins(c, 140, 150, 140, 150)
    shade(c, "FFFFFF")
    set_cell_text(c, "IMS Portal     |     " + title + "                                      [Thông báo]  [Tài khoản]", True, WHITE)
    shade(c, BLUE)
    for kind, content in rows:
        row = outer.add_row().cells[0]
        if kind == "bar":
            set_cell_text(row, content, True); shade(row, LIGHT)
        elif kind == "buttons":
            set_cell_text(row, content, True, BLUE, WD_ALIGN_PARAGRAPH.RIGHT)
        else:
            set_cell_text(row, content)
    if note:
        p(doc, "Ghi chú tương tác: " + note, False, None, 3, 8)

doc = Document()
sec = doc.sections[0]
sec.page_width, sec.page_height = Inches(8.5), Inches(11)
sec.top_margin = sec.bottom_margin = Inches(.7)
sec.left_margin = sec.right_margin = Inches(.75)

styles = doc.styles
for sname in ["Normal", "Title", "Heading 1", "Heading 2", "Heading 3", "List Bullet", "List Number"]:
    st = styles[sname]
    st.font.name = "Times New Roman"; st.font.size = Pt(13); st.font.color.rgb = RGBColor(0,0,0)
    st._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), "Times New Roman")
    st._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), "Times New Roman")
    st._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), "Times New Roman")

title = doc.add_paragraph(style="Title"); title.alignment = WD_ALIGN_PARAGRAPH.CENTER
set_font(title.add_run("KẾ HOẠCH CÔNG VIỆC VÀ WIREFRAME SPRINT 2"), True)
p(doc, "Hệ thống quản lý thực tập sinh", True, WD_ALIGN_PARAGRAPH.CENTER, 6, 10)
p(doc, "Phạm vi: 11 user story Sprint 2 theo tài liệu phân tích nghiệp vụ trong dự án", False, WD_ALIGN_PARAGRAPH.CENTER, 0, 18)
table(doc, ["Thông tin", "Nội dung"], [
    ["Mục đích", "Dùng để chia việc cho thành viên, thống nhất phạm vi phát triển và làm cơ sở nghiệm thu Sprint 2."],
    ["Nền tảng hiện tại", "Java 17, Jakarta Servlet/JSP, JDBC, MySQL 8, Maven; giao diện dùng JSP và CSS hiện có."],
    ["Định dạng", "Times New Roman, cỡ 13; wireframe mức độ thấp để xác nhận bố cục và luồng thao tác."],
    ["Lưu ý phạm vi", "Thiết lập ngày bắt đầu và kết thúc là một phần của chức năng HR tạo chương trình thực tập, không tách thành module riêng."],
], [1.5, 5.3])

heading(doc, "1 Kết luận và định hướng triển khai", 1)
p(doc, "Sprint 2 là một chuỗi nghiệp vụ liên thông từ mở chương trình, ứng viên đăng ký và nộp hồ sơ, HR xét duyệt, gửi kết quả, quản lý hợp đồng, phân công mentor, lịch cá nhân, chấm công đến báo cáo chuyên cần. Vì vậy nhóm nên triển khai theo lát cắt dọc có thứ tự phụ thuộc, đồng thời dùng chung các thành phần trạng thái, phân quyền, tải tệp, nhật ký và thông báo.")
bullets(doc, [
    "Ưu tiên nền tảng dữ liệu và trạng thái trước; không phát triển từng màn hình như các chức năng rời rạc.",
    "Tách hồ sơ ứng tuyển khỏi intern_profiles hiện tại: candidates hiện chưa gắn user và chương trình, chưa hỗ trợ lưu nháp hay nhiều hồ sơ theo chương trình.",
    "Giữ contracts, attendance_records, internship_programs và audit_logs làm nền nhưng cần migration bổ sung trường, bảng và ràng buộc theo tài liệu nghiệp vụ.",
    "Email kết quả phải đi qua hàng đợi, gửi theo lịch, chống gửi trùng; không gọi SMTP đồng bộ trong request duyệt hồ sơ.",
    "Báo cáo nghỉ phép trong Sprint 2 chỉ hiển thị Chưa có dữ liệu vì phân tích nghiệp vụ chưa có nguồn xin và duyệt nghỉ phép, dù schema đang có bảng leave_requests.",
])

heading(doc, "2 Phạm vi user story và màn hình", 1)
stories = [
    ["US01", "Ứng viên đăng ký tài khoản, xác thực email và nộp hồ sơ", "Ứng viên", "Đăng ký; xác thực; danh sách chương trình; form hồ sơ; hồ sơ của tôi"],
    ["US02", "HR duyệt hoặc từ chối hồ sơ", "HR", "Danh sách hồ sơ; chi tiết; xác nhận duyệt; từ chối hàng loạt"],
    ["US03", "Hệ thống gửi email kết quả theo lịch", "Hệ thống và HR", "Hàng đợi email; thu hồi; trạng thái gửi"],
    ["US04", "HR tải lên hợp đồng thực tập", "HR", "Danh sách người được nhận; form hợp đồng; lịch sử phiên bản"],
    ["US05", "Thực tập sinh xác nhận hợp đồng", "TTS", "Chi tiết hợp đồng; xác nhận; từ chối xác nhận"],
    ["US06", "HR tạo chương trình theo phòng ban, gồm ngày bắt đầu và kết thúc", "HR", "Danh sách; tạo sửa; chi tiết chương trình"],
    ["US07", "HR phân công thực tập sinh cho mentor", "HR", "Phân công; chia đều; xác nhận đổi mentor"],
    ["US08", "Thực tập sinh xem lịch cá nhân", "TTS", "Lịch tháng tuần; chi tiết sự kiện"],
    ["US09", "Thực tập sinh check-in và check-out", "TTS", "Chấm công hôm nay; lịch sử; tự sửa"],
    ["US10", "HR xem báo cáo đi làm và nghỉ phép", "HR", "Báo cáo tổng hợp; chi tiết ngày; ngày nghỉ chung"],
]
table(doc, ["Mã", "Phạm vi", "Tác nhân", "Giao diện chính"], stories, [.65, 3.0, 1.1, 2.1])

heading(doc, "3 Đánh giá hệ thống hiện tại và khoảng trống", 1)
gaps = [
    ["Tài khoản", "Đã có users, role, login, quên và đổi mật khẩu", "Thiếu đăng ký công khai, trạng thái xác thực email rõ ràng, token một lần, gửi lại và giới hạn tần suất"],
    ["Ứng tuyển", "Có candidates đơn giản và intern_profiles", "Thiếu application theo user và program, nháp, GPA/năm học, tài liệu theo yêu cầu, vòng đời xét duyệt"],
    ["Chương trình", "Có internship_programs, departments", "Thiếu ngày mở đóng hồ sơ, thứ làm việc, yêu cầu giấy tờ, trạng thái tự tính, version chống ghi đè, đóng sớm"],
    ["Hợp đồng", "Có contracts cơ bản", "Thiếu phiên bản tệp, thời hạn xác nhận, lý do từ chối, lịch sử thao tác và quy tắc một hợp đồng hiện hành"],
    ["Mentor", "Có mentors và intern_profiles.mentor_id", "Thiếu quy trình đề xuất chia đều, kiểm tra cùng phòng ban, lịch sử phân công và thao tác hàng loạt"],
    ["Lịch", "Có program_milestones", "Thiếu calendar_events, đối tượng nhận và API tổng hợp mốc hợp đồng/chương trình"],
    ["Chấm công", "Có attendance_records", "Thiếu trạng thái đang làm, thiếu checkout, hủy; lịch sử sửa; hạn tự sửa; tổng giờ"],
    ["Báo cáo", "Có attendance_records và leave_requests", "Chưa có ngày nghỉ chung, thứ làm việc; công thức báo cáo; nghỉ phép chưa có quy trình nguồn theo tài liệu"],
    ["Hạ tầng chung", "Có EmailUtility, notifications và audit_logs", "Thiếu hàng đợi email bền vững, scheduler, idempotency, audit service dùng chung, quyền Sprint 2"],
]
table(doc, ["Khu vực", "Đã có", "Cần bổ sung"], gaps, [1.2, 2.6, 3.3])

landscape(doc)
heading(doc, "4 Danh sách công việc Sprint 2 để phân công", 1)
p(doc, "Quy ước gợi ý phụ trách: DB BE là backend thiên về dữ liệu; BE là Java Servlet DAO Service; FE là JSP CSS JavaScript; QA là kiểm thử. Có thể gán một người cho nhiều dòng nhưng mỗi dòng cần một chủ sở hữu chính.")

backlog_groups = [
("4.1 Nền tảng dữ liệu và dùng chung", [
["S2-01","Chung","DB/BE","Thiết kế migration Sprint 2, sao lưu và script rollback; không sửa trực tiếp schema đã triển khai.","Trước mọi module","Migration chạy được trên DB mới và DB hiện có"],
["S2-02","Chung","DB/BE","Chuẩn hóa enum trạng thái cho account, application, program, contract, email queue và attendance.","S2-01","DB constraint và Java constant thống nhất"],
["S2-03","Chung","BE","Tạo AuditService ghi ai, lúc nào, IP, giá trị cũ mới cho thao tác quan trọng.","S2-01","Mọi luồng bắt buộc có audit test"],
["S2-04","Chung","BE","Bổ sung permission và ánh xạ role cho ứng tuyển, chương trình, hợp đồng, mentor, lịch, chấm công, báo cáo.","S2-01","Server chặn đúng role dù đổi URL"],
["S2-05","Chung","BE/FE","Xây FileStorageService: tên tệp an toàn, loại MIME, dung lượng, đường dẫn ngoài webroot, download có kiểm quyền.","S2-01","Không truy cập tệp người khác qua URL"],
["S2-06","Chung","QA","Tạo bộ dữ liệu test và ma trận trạng thái liên module.","S2-01 S2-02","Có dữ liệu cho ứng viên HR mentor TTS"],
]),
("4.2 Đăng ký và nộp hồ sơ", [
["S2-07","US01","DB/BE","Bổ sung users.email_verified_at, verification token hash, expiry, used_at và resend counter.","S2-01","Email duy nhất; token hết hạn và dùng một lần"],
["S2-08","US01","BE","Tạo RegisterServlet và service kiểm email, mật khẩu, băm mật khẩu, gán role CANDIDATE.","S2-07","Đăng ký an toàn và chống gửi lặp"],
["S2-09","US01","BE","Tạo luồng gửi, xác thực và gửi lại email xác thực có rate limit.","S2-07 S2-08","Tài khoản chưa xác thực không nộp hồ sơ"],
["S2-10","US01","FE","Dựng trang đăng ký, trang kết quả xác thực và gửi lại liên kết.","S2-08 S2-09","Lỗi hiển thị sát trường, không mất dữ liệu"],
["S2-11","US01","DB/BE","Tạo applications gắn user và program; unique user_id program_id; trường học, ngành, năm học, GPA, trạng thái và submitted_at.","S2-01 S2-02","Một hồ sơ cho mỗi chương trình"],
["S2-12","US01 US06","DB/BE","Tạo program_document_requirements và application_documents; hỗ trợ bắt buộc hoặc tùy chọn.","S2-11","Kiểm đúng yêu cầu hiện hành khi nộp"],
["S2-13","US01","BE","API DAO Service danh sách chương trình đang nhận hồ sơ, tạo và lưu hồ sơ nháp.","S2-11 S2-12","Nháp tồn tại sau đăng nhập lại"],
["S2-14","US01","BE","Xử lý tải CV và giấy tờ, thay thế tệp ở trạng thái được sửa, validation MIME và dung lượng.","S2-05 S2-12","Lỗi một tệp không làm mất form"],
["S2-15","US01","BE","Service nộp hồ sơ theo transaction: khóa bản ghi, kiểm email, hạn, trường, tài liệu và chống bấm hai lần.","S2-13 S2-14","Chuyển SUBMITTED một lần duy nhất"],
["S2-16","US01","FE","Dựng danh sách chương trình, chi tiết, form hồ sơ nhiều phần, lưu nháp, nộp và hồ sơ của tôi.","S2-13 S2-15","Hiển thị đúng trạng thái và trường bị khóa"],
["S2-17","US01","QA","Test đăng ký, token, hạn chương trình, thiếu file, file lỗi, hai tab, quyền sở hữu hồ sơ.","S2-08 đến S2-16","Đạt 6 AC và ngoại lệ chính"],
]),
("4.3 HR tạo chương trình và thiết lập thời gian", [
["S2-18","US06","DB/BE","Mở rộng internship_programs: application_open_date, application_close_date, internship_start_date, internship_end_date, work_days, closed_early_at, version.","S2-01","Ràng buộc ngày và ít nhất một thứ làm việc"],
["S2-19","US06","BE","ProgramDAO Service tính trạng thái theo ngày, kiểm tên trùng trong phòng ban, chỉ tiêu và optimistic locking.","S2-18","PLANNED OPEN CLOSED được tính nhất quán"],
["S2-20","US06","BE","CRUD chương trình, đóng sớm, mở lại bằng gia hạn, xóa khi chưa có hồ sơ; khóa yêu cầu giấy tờ sau hồ sơ nộp.","S2-12 S2-19","Audit đủ giá trị cũ mới"],
["S2-21","US06","BE","Truy vấn thống kê số hồ sơ, đã duyệt, được nhận so với chỉ tiêu và link lọc hồ sơ.","S2-11 S2-20","Số liệu đúng theo chương trình"],
["S2-22","US06","FE","Dựng danh sách, form tạo sửa, chi tiết chương trình; trường ngày bắt đầu kết thúc nằm trong form này.","S2-19 S2-21","Đánh dấu đúng trường sai và trường bị khóa"],
["S2-23","US06","QA","Test biên ngày, tên trùng, đóng mở, xóa, hai HR cùng sửa, quota và quyền.","S2-18 đến S2-22","Đạt 12 AC tài liệu chương trình"],
]),
("4.4 HR xét duyệt và email kết quả", [
["S2-24","US02","DB/BE","Bổ sung application_decisions và dữ liệu quyết định, người thực hiện, lý do, version.","S2-11","Lưu lịch sử quyết định và chống ghi đè"],
["S2-25","US02","BE","Danh sách hồ sơ với phân trang và bộ lọc chương trình, phòng ban, trạng thái, trường, ngành, năm, GPA, ngày, tên email.","S2-11","Nháp không xuất hiện; index phù hợp"],
["S2-26","US02","BE","Mở chi tiết chuyển SUBMITTED sang UNDER_REVIEW; kiểm quyền tải tài liệu.","S2-14 S2-25","Mở lần đầu an toàn khi chạy lặp"],
["S2-27","US02","BE","Duyệt và từ chối hàng loạt theo transaction từng hồ sơ; bỏ qua hồ sơ bị người khác xử lý; lý do riêng bắt buộc.","S2-24 S2-25","Kết quả lô nêu thành công và bị bỏ qua"],
["S2-28","US02 US06","BE/FE","Cảnh báo quota khi duyệt làm tổng đã duyệt cộng được nhận đạt hoặc vượt chỉ tiêu; yêu cầu xác nhận.","S2-21 S2-27","Cảnh báo không chặn sau xác nhận"],
["S2-29","US02","FE","Dựng danh sách hồ sơ, bộ lọc, chọn kết quả hiện tại, chi tiết và modal duyệt từ chối hàng loạt.","S2-25 S2-27","Lý do thiếu được đánh dấu theo dòng"],
["S2-30","US03","DB/BE","Tạo email_result_queue với unique pending per application, payload, scheduled_at, attempts, locked_at, sent_at, error.","S2-01","Hàng đợi bền vững và chống trùng"],
["S2-31","US03","BE","Tích hợp tạo thông báo PENDING cùng transaction quyết định; nội dung lấy dữ liệu mới nhất.","S2-27 S2-30","Không có quyết định mà thiếu thông báo"],
["S2-32","US03","BE","Scheduler gửi theo giờ cấu hình, chốt lô, khóa công việc, retry lỗi tạm thời, FAILED với lỗi vĩnh viễn.","S2-30 S2-31","Chạy trùng không gửi email trùng"],
["S2-33","US03","BE/FE","Màn HR xem hàng đợi, thu hồi hàng loạt, sửa quyết định trước giờ gửi và xem lỗi.","S2-31 S2-32","Sau chốt lô không thu hồi được"],
["S2-34","US02 US03","QA","Test cạnh tranh hai HR, retry, job gián đoạn, sửa nhiều lần, thu hồi sát giờ và ẩn kết quả trước khi gửi.","S2-24 đến S2-33","Đạt AC xét duyệt và email"],
]),
("4.5 Hợp đồng và xác nhận", [
["S2-35","US04 US05","DB/BE","Mở rộng contracts và tạo contract_versions, confirmation_deadline, uploaded_by, reject_reason, version, checksum.","S2-01","Một hợp đồng hiện hành, giữ lịch sử tệp"],
["S2-36","US04","BE","Danh sách người được nhận có trạng thái hợp đồng và mentor; upload PDF; gợi ý ngày từ chương trình.","S2-21 S2-35","Chỉ người được nhận mới có hợp đồng"],
["S2-37","US04","BE","Cho HR thay tệp trước xác nhận, vô hiệu phiên bản cũ, xóa theo quy tắc và audit.","S2-35 S2-36","TTS luôn thấy phiên bản hiện hành"],
["S2-38","US04","FE","Dựng danh sách hợp đồng và form tải lên gồm số, ngày, hạn xác nhận, phụ cấp và PDF.","S2-36","Validation ngày và file rõ ràng"],
["S2-39","US05","BE","Trang hợp đồng của tôi, download kiểm quyền, xác nhận hoặc từ chối với lý do; ghi thời gian và IP.","S2-35 S2-37","Xác nhận chạy lặp không tạo kết quả trùng"],
["S2-40","US05","FE","Dựng chi tiết hợp đồng, checkbox cam kết, nút xác nhận và dialog từ chối.","S2-39","Không thể xác nhận khi hết hạn hoặc tệp đổi"],
["S2-41","US04 US05","QA","Test thay phiên bản, hai tab, hạn xác nhận, quyền tải, ngày hợp đồng ưu tiên cho lịch chấm công.","S2-35 đến S2-40","Đạt AC hợp đồng hai phía"],
]),
("4.6 Phân công mentor và lịch cá nhân", [
["S2-42","US07","DB/BE","Tạo mentor_assignment_history và ràng buộc một mentor hiện hành cho mỗi TTS.","S2-01","Lưu mentor cũ mới, HR, thời gian"],
["S2-43","US07","BE","Truy vấn TTS được nhận và mentor active cùng phòng ban; thống kê tải hiện tại.","S2-21 S2-42","Không phân công chéo phòng ban"],
["S2-44","US07","BE","Thuật toán chia đều ổn định theo mentor ít người nhất và thứ tự cố định; chỉ lưu khi HR xác nhận.","S2-43","Cùng đầu vào cho cùng đề xuất"],
["S2-45","US07","FE","Dựng màn phân công, đề xuất, chỉnh từng dòng, xác nhận và đổi mentor.","S2-44","Hiển thị tải trước sau của mentor"],
["S2-46","US07","QA","Test phòng ban không có mentor, mentor inactive, thay đổi đồng thời, chia đều và lịch sử.","S2-42 đến S2-45","Đạt AC phân công"],
["S2-47","US08","DB/BE","Tạo calendar_events và event_recipients cho cá nhân, mentor group, chương trình; trạng thái hủy.","S2-01","Có cấu trúc giao tiếp cho story tạo sự kiện sau"],
["S2-48","US08","BE","CalendarService hợp nhất sự kiện với mốc bắt đầu kết thúc; ưu tiên ngày hợp đồng rồi chương trình.","S2-37 S2-42 S2-47","Chỉ trả lịch của user đăng nhập"],
["S2-49","US08","FE","Dựng lịch tháng tuần, hôm nay, chuyển kỳ, +N mục khác và chi tiết mốc sự kiện.","S2-48","Read only; tô kỳ thực tập"],
["S2-50","US08","QA","Test sự kiện nhiều ngày, hủy, đổi mentor, nguồn mốc, chưa được nhận và truy cập lịch người khác.","S2-47 đến S2-49","Đạt 12 AC lịch cá nhân"],
]),
("4.7 Chấm công và báo cáo chuyên cần", [
["S2-51","US09","DB/BE","Mở rộng attendance_records: workflow status, total_minutes, edited flag, cancelled; tạo attendance_change_logs và cấu hình hạn tự sửa.","S2-01","Unique ngày hoạt động; giữ giá trị gốc"],
["S2-52","US09","BE","Check-in check-out lấy giờ máy chủ, transaction và idempotency; kiểm kỳ theo hợp đồng rồi chương trình.","S2-35 S2-51","Không check-in hoặc checkout hai lần"],
["S2-53","US09","BE","Job đánh dấu thiếu checkout khi sang ngày mới; không tự điền giờ.","S2-51","Có thể chạy lại an toàn"],
["S2-54","US09","BE","Tự sửa trong hạn, bổ sung ngày, hủy bản ghi; quá hạn chỉ HR; lý do bắt buộc và audit.","S2-51 S2-52","Giờ cùng ngày, checkout sau checkin"],
["S2-55","US09","BE","Lịch sử tháng và tổng giờ; mentor xem TTS hiện được phân công; HR lọc và sửa.","S2-42 S2-54","Quyền kiểm ở server"],
["S2-56","US09","FE","Dựng chấm công hôm nay, lịch sử tháng, form sửa bổ sung và màn HR mentor xem.","S2-52 S2-55","Nêu giờ server, hạn sửa và nhãn chỉnh sửa"],
["S2-57","US09","QA","Test hai tab, mất kết nối, thiếu checkout, ngoài kỳ, sửa sai giờ, hủy và đổi mentor.","S2-51 đến S2-56","Đạt 13 AC chấm công"],
["S2-58","US10","DB/BE","Tạo public_holidays; dùng work_days trên program; index báo cáo theo intern và work_date.","S2-18 S2-51","Ngày nghỉ không trùng và có audit"],
["S2-59","US10","BE","AttendanceReportService tính kỳ giao, ngày kỳ vọng đã qua, đi làm, vắng, thiếu checkout, chỉnh sửa, tổng giờ và tỷ lệ.","S2-55 S2-58","Mẫu số 0 hiển thị gạch ngang"],
["S2-60","US10","BE","API lọc, phân trang, sắp xếp, dòng tổng toàn bộ tập lọc và chi tiết từng ngày.","S2-59","Không tính tổng chỉ trên trang hiện tại"],
["S2-61","US10","FE","Dựng báo cáo tổng hợp, chi tiết ngày, quản lý ngày nghỉ chung; cột nghỉ phép ghi Chưa có dữ liệu.","S2-60","Không trình bày nghỉ phép là 0"],
["S2-62","US10","QA","Test kỳ giữa tháng, lễ khai báo muộn, cuối tuần có chấm công, hôm nay, dữ liệu cũ thiếu work_days và quyền.","S2-58 đến S2-61","Đạt 13 AC báo cáo"],
]),
("4.8 Hoàn thiện và nghiệm thu", [
["S2-63","Chung","FE","Cập nhật menu theo role, trạng thái trống, lỗi và nút thử lại; thống nhất badge trạng thái.","Các màn đã xong","Không lộ chức năng ngoài quyền"],
["S2-64","Chung","BE/QA","Kiểm tra bảo mật: CSRF, XSS output encoding, upload độc hại, SQL injection, IDOR, rate limit.","Các module đã xong","Không có lỗi mức cao"],
["S2-65","Chung","QA","Regression luồng xuyên suốt từ tạo chương trình đến báo cáo; test rollback transaction và concurrency.","S2-07 đến S2-62","Có biên bản pass fail theo AC"],
["S2-66","Chung","DevOps","Cấu hình SMTP, email schedule, upload directory, retention log; tài liệu biến môi trường.","S2-32 S2-35","Triển khai không chứa secret trong source"],
["S2-67","Chung","Nhóm","Demo, rà soát dữ liệu, chốt Definition of Done và cập nhật tài liệu vận hành.","S2-63 đến S2-66","PO nghiệm thu theo từng user story"],
]),
]
for title_text, rows in backlog_groups:
    heading(doc, title_text, 2)
    table(doc, ["ID","Story","Gợi ý phụ trách","Công việc","Phụ thuộc","Điều kiện hoàn thành"], rows, [.65,.75,1.05,4.25,1.15,2.1])

portrait(doc)
heading(doc, "5 Gợi ý chia việc theo thành viên", 1)
p(doc, "Bảng dưới đây là gợi ý theo năng lực, không giả định số lượng hay tên thành viên. Trưởng nhóm có thể gán tên thật vào cột Chủ sở hữu trong công cụ quản lý Sprint.")
table(doc, ["Nhóm năng lực", "Phạm vi ưu tiên", "Các mã việc"], [
["Thành viên A DB và nền tảng", "Migration, trạng thái, audit, RBAC, storage", "S2-01 đến S2-06, S2-18, S2-30, S2-35, S2-42, S2-47, S2-51, S2-58"],
["Thành viên B Backend tuyển dụng", "Đăng ký, hồ sơ, chương trình, xét duyệt, email", "S2-07 đến S2-17, S2-19 đến S2-34"],
["Thành viên C Backend vận hành", "Hợp đồng, mentor, lịch, chấm công, báo cáo", "S2-36 đến S2-60"],
["Thành viên D Frontend", "Toàn bộ JSP CSS JS và trạng thái giao diện", "S2-10, S2-16, S2-22, S2-29, S2-33, S2-38, S2-40, S2-45, S2-49, S2-56, S2-61, S2-63"],
["Thành viên E QA hoặc cả nhóm luân phiên", "Test nghiệp vụ, concurrency, bảo mật, regression", "S2-06, S2-17, S2-23, S2-34, S2-41, S2-46, S2-50, S2-57, S2-62, S2-64 đến S2-67"],
], [1.6, 3.5, 1.8])

heading(doc, "6 Thứ tự triển khai đề xuất", 1)
numbered(doc, [
    "Ngày đầu: chốt migration, enum trạng thái, RBAC, audit, file storage và dữ liệu test.",
    "Lát cắt 1: HR tạo chương trình, sau đó ứng viên đăng ký, xác thực và nộp hồ sơ.",
    "Lát cắt 2: HR xét duyệt, hàng đợi email và cơ chế thu hồi trước giờ gửi.",
    "Lát cắt 3: hợp đồng hai phía và phân công mentor.",
    "Lát cắt 4: lịch cá nhân, chấm công và báo cáo chuyên cần.",
    "Cuối Sprint: test xuyên suốt, bảo mật, hiệu năng truy vấn, cấu hình triển khai và demo nghiệm thu.",
])
heading(doc, "7 Definition of Done áp dụng cho mọi đầu việc", 1)
bullets(doc, [
    "Có migration và ràng buộc dữ liệu; không chỉ dựa vào kiểm tra phía giao diện.",
    "Kiểm tra quyền ở Servlet hoặc Service; truy cập bằng cách đổi URL vẫn bị chặn.",
    "Luồng ghi dữ liệu quan trọng dùng transaction, chống bấm lặp và có audit.",
    "JSP không chứa logic nghiệp vụ chính; output dữ liệu người dùng được escape.",
    "Có unit hoặc integration test cho quy tắc cốt lõi và test thủ công theo tiêu chí chấp nhận.",
    "Trạng thái trống, lỗi, tải lại, dữ liệu đồng thời và thông báo cho người dùng được xử lý.",
    "Code review hoàn tất, Maven build thành công và không đưa secret hoặc tệp upload vào repository.",
])

landscape(doc)
heading(doc, "8 Wireframe giao diện Sprint 2", 1)
p(doc, "Các khung dưới đây là wireframe mức độ thấp. Khi triển khai, dùng lại sidebar, card, bảng và màu trạng thái của dashboard.css hiện tại; mọi kiểm tra nghiệp vụ vẫn thực hiện ở server.")

wireframes = [
("WF01 Đăng ký tài khoản", [("bar","ĐĂNG KÝ TÀI KHOẢN  |  Đã có tài khoản? Đăng nhập"),("text","Họ và tên: [________________________________________]"),("text","Email:      [________________________________________]"),("text","Mật khẩu:   [________________________________________]  [Hiện]"),("text","Yêu cầu: tối thiểu 8 ký tự, chữ hoa, chữ thường và số"),("text","Xác nhận:   [________________________________________]"),("buttons","[ Hủy ]     [ Đăng ký ]")], "Sau khi gửi, chuyển sang trang yêu cầu kiểm tra email; có nút gửi lại với thời gian chờ."),
("WF02 Danh sách chương trình đang mở", [("bar","CHƯƠNG TRÌNH THỰC TẬP  |  Tìm kiếm [________]  Phòng ban [Tất cả v]"),("text","[CNTT] Backend Java Intern     Nhận hồ sơ: 01/10 - 20/10     [Xem chi tiết]"),("text","Kỳ thực tập: 01/11 - 31/01    Chỉ tiêu: 10    Trạng thái: Đang nhận hồ sơ"),("text","[Marketing] Content Intern    Nhận hồ sơ: 05/10 - 25/10     [Xem chi tiết]"),("buttons","[ Hồ sơ của tôi ]")], "Chỉ chương trình trạng thái Đang nhận hồ sơ cho phép bắt đầu hồ sơ."),
("WF03 Nộp hồ sơ trực tuyến", [("bar","BACKEND JAVA INTERN  |  Trạng thái hồ sơ: Nháp"),("text","1 Thông tin cá nhân  |  2 Học vấn  |  3 Tài liệu  |  4 Kiểm tra"),("text","Trường: [________________]  Chuyên ngành: [________________]"),("text","Năm học: [____ v]          GPA: [____ / thang điểm ____]"),("text","CV bắt buộc: [Chọn tệp] cv-nguyenvana.pdf  [Thay thế]"),("text","Bảng điểm bắt buộc: [Chọn tệp]   Portfolio tùy chọn: [Chọn tệp]"),("text","Thiếu thông tin sẽ được đánh dấu tại đúng bước."),("buttons","[ Lưu nháp ]   [ Xem trước ]   [ Nộp hồ sơ ]")], "Khi chương trình đóng, vẫn xem nháp nhưng nút Nộp bị khóa và nêu lý do."),
("WF04 HR danh sách chương trình", [("bar","QUẢN LÝ CHƯƠNG TRÌNH  |  [+ Tạo chương trình]"),("text","Tìm [________]  Phòng ban [Tất cả v]  Trạng thái [Tất cả v]  [Lọc]"),("text","Mã | Tên | Phòng ban | Nhận hồ sơ | Kỳ thực tập | Hồ sơ | Duyệt | Nhận | Trạng thái | Thao tác"),("text","PRG01 | Backend Java | CNTT | 01-20/10 | 01/11-31/01 | 42 | 12 | 8/10 | Mở | [Chi tiết] [Sửa]"),("text","PRG02 | Content | Marketing | 05-25/10 | 01/11-31/12 | 18 | 5 | 3/5 | Sắp mở | [Chi tiết] [Sửa]"),("buttons","Trang 1/3   [Trước] [Sau]")], "Từ chi tiết chương trình mở danh sách hồ sơ đã lọc theo chương trình."),
("WF05 Tạo và sửa chương trình", [("bar","TẠO CHƯƠNG TRÌNH THỰC TẬP"),("text","Mã: [__________]   Tên chương trình: [____________________________]"),("text","Phòng ban: [Chọn phòng ban v]   Chỉ tiêu: [____]"),("text","Mở nhận hồ sơ: [dd/mm/yyyy]   Đóng nhận hồ sơ: [dd/mm/yyyy]"),("text","Bắt đầu thực tập: [dd/mm/yyyy]   Kết thúc thực tập: [dd/mm/yyyy]"),("text","Thứ làm việc: [x] T2 [x] T3 [x] T4 [x] T5 [x] T6 [ ] T7 [ ] CN"),("text","Giấy tờ: CV [Bắt buộc]  Bảng điểm [Bắt buộc v]  Portfolio [Tùy chọn v]  [+ Thêm]"),("text","Mô tả: [________________________________________________________]"),("buttons","[ Hủy ]   [ Lưu nháp ]   [ Tạo chương trình ]")], "Ngày bắt đầu và kết thúc nằm trong form này. Yêu cầu giấy tờ bị khóa sau khi có hồ sơ đã nộp."),
("WF06 HR danh sách và xét duyệt hồ sơ", [("bar","HỒ SƠ ỨNG TUYỂN  |  Đã chọn 3  [Duyệt] [Từ chối]"),("text","Chương trình [Backend Java v]  Phòng ban [CNTT v]  Trạng thái [Đã nộp v]"),("text","Trường [____] Ngành [____] Năm [v] GPA từ [__] Ngày nộp [__]-[__] Tìm [____] [Lọc]"),("text","[ ] | Ứng viên | Email | Trường | Ngành | Năm | GPA | Ngày nộp | Trạng thái | Xem"),("text","[x] | Nguyễn Văn A | a@email | FPT | SE | 3 | 3.4 | 10/10 | Đang duyệt | [Chi tiết]"),("text","[x] | Trần Thị B | b@email | HUST | IT | 4 | 3.7 | 11/10 | Đã nộp | [Chi tiết]"),("buttons","[x] Chọn toàn bộ 42 kết quả lọc     Trang 1/5")], "Xác nhận duyệt hiển thị số lượng và cảnh báo quota nếu đạt hoặc vượt chỉ tiêu."),
("WF07 Từ chối hàng loạt", [("bar","TỪ CHỐI 3 HỒ SƠ"),("text","Lý do dùng chung: [________________________________] [Áp dụng cho dòng đã chọn]"),("text","Nguyễn Văn A   Lý do*: [________________________________________]"),("text","Trần Thị B     Lý do*: [________________________________________]"),("text","Lê Văn C       Lý do*: [________________________________________]"),("text","Email sẽ được xếp hàng gửi lúc 09:00 ngày 05/10/2026."),("buttons","[ Quay lại ]   [ Xác nhận từ chối 3 hồ sơ ]")], "Không xác nhận nếu bất kỳ dòng nào thiếu lý do; kết quả chưa hiển thị cho ứng viên trước khi email gửi."),
("WF08 Hàng đợi email kết quả", [("bar","THÔNG BÁO KẾT QUẢ  |  Lần gửi kế tiếp: 09:00 05/10/2026"),("text","Trạng thái [Chờ gửi v]  Loại [Tất cả v]  Tìm ứng viên [________] [Lọc]"),("text","[ ] | Ứng viên | Chương trình | Kết quả | Dự kiến gửi | Trạng thái | Lần thử | Thao tác"),("text","[x] | Nguyễn Văn A | Backend | Đã duyệt | 05/10 09:00 | Chờ gửi | 0 | [Sửa]"),("text","[ ] | Trần Thị B | Backend | Từ chối | 05/10 09:00 | Chờ gửi | 0 | [Sửa]"),("text","[ ] | Lê Văn C | Content | Từ chối | 04/10 09:00 | Gửi lỗi | 3 | [Xem lỗi]"),("buttons","[ Thu hồi các mục đã chọn ]")], "Thu hồi đưa hồ sơ về Đang duyệt; không cho thu hồi mục đã chốt hoặc đã gửi."),
("WF09 HR quản lý hợp đồng", [("bar","HỢP ĐỒNG THỰC TẬP  |  Tìm [________]  Trạng thái [Tất cả v]"),("text","TTS | Chương trình | Mentor | Thời gian | Hợp đồng | Xác nhận | Thao tác"),("text","Nguyễn Văn A | Backend | Chưa có mentor | 01/11-31/01 | Chưa tải | - | [Tải hợp đồng]"),("text","Trần Thị B | Backend | Lê Minh | 01/11-31/01 | HD-2026-08 v2 | Chờ | [Xem] [Thay tệp]"),("bar","TẢI HỢP ĐỒNG: Số [________]  Bắt đầu [01/11]  Kết thúc [31/01]"),("text","Phụ cấp [________]  Hạn xác nhận [dd/mm/yyyy hh:mm]  PDF [Chọn tệp]"),("buttons","[ Hủy ]   [ Gửi hợp đồng cho TTS ]")], "Ngày được gợi ý từ chương trình nhưng HR được sửa; hiển thị mentor hoặc Chưa có mentor."),
("WF10 Thực tập sinh xác nhận hợp đồng", [("bar","HỢP ĐỒNG CỦA TÔI  |  Trạng thái: Chờ xác nhận"),("text","Số hợp đồng: HD-2026-08     Phiên bản: 2     Hạn xác nhận: 25/10/2026 17:00"),("text","Thời gian thực tập: 01/11/2026 - 31/01/2027     Phụ cấp: 5.000.000 VNĐ"),("text","Tệp PDF: [Xem hợp đồng] [Tải xuống]"),("text","[ ] Tôi đã đọc và đồng ý với nội dung của phiên bản hợp đồng hiện tại."),("buttons","[ Không xác nhận ]   [ Xác nhận hợp đồng ]")], "Nếu HR thay phiên bản trước lúc bấm xác nhận, yêu cầu tải lại và xác nhận đúng phiên bản mới."),
("WF11 HR phân công mentor", [("bar","PHÂN CÔNG MENTOR  |  Chương trình [Backend Java v]  [Chia đều]"),("text","Mentor hiện có: Lê Minh 3 TTS | Phạm An 2 TTS | Hoàng Lan 2 TTS"),("text","[ ] | TTS | Trường | Mentor hiện tại | Mentor đề xuất | Ghi chú"),("text","[x] | Nguyễn Văn A | FPT | Chưa có | [Phạm An v] | Sau phân công: 3"),("text","[x] | Trần Thị B | HUST | Chưa có | [Hoàng Lan v] | Sau phân công: 3"),("text","[ ] | Lê Văn C | PTIT | Lê Minh | [Lê Minh v] | Không đổi"),("buttons","[ Hủy đề xuất ]   [ Xác nhận phân công 2 TTS ]")], "Danh sách mentor chỉ gồm người active cùng phòng ban; đề xuất chưa ghi DB trước khi xác nhận."),
("WF12 Lịch thực tập cá nhân", [("bar","LỊCH CỦA TÔI  |  [Tháng] [Tuần]   [<] Tháng 11 2026 [>]   [Hôm nay]"),("text","T2        T3        T4        T5        T6        T7        CN"),("text","2         3         4         5         6         7         8"),("text","Bắt đầu   Họp nhóm  09:00     Demo      +2 mục"),("text","9         10        11        12        13        14        15"),("text","          Training            Review"),("text","Chú giải: Mốc chương trình | Mốc hợp đồng | Sự kiện cá nhân | Sự kiện mentor"),("buttons","[Xem chi tiết mục đã chọn]")], "Chỉ đọc; ngày trong kỳ được tô nền; mốc ưu tiên hợp đồng rồi chương trình."),
("WF13 Check in và check out", [("bar","CHẤM CÔNG HÔM NAY  |  Thứ Hai 05/10/2026  |  Giờ máy chủ 08:42:16"),("text","Trạng thái: Chưa check-in     Kỳ thực tập: 01/10/2026 - 31/12/2026"),("buttons","[ CHECK IN ]"),("bar","LỊCH SỬ THÁNG 10  |  [<] [>]  Tổng giờ: 86 giờ 30 phút"),("text","Ngày | Giờ vào | Giờ ra | Tổng | Trạng thái | Nhãn | Thao tác"),("text","02/10 | 08:31 | 17:29 | 08:58 | Hoàn tất | - | [Xem]"),("text","03/10 | 08:45 | - | - | Thiếu check-out | - | [Bổ sung]"),("text","04/10 | 09:00 | 17:00 | 08:00 | Hoàn tất | Đã chỉnh sửa | [Xem lịch sử]")], "Nút đổi thành Check out sau check-in; giờ lấy từ server; trong hạn có thể sửa kèm lý do."),
("WF14 Báo cáo chuyên cần", [("bar","BÁO CÁO CHUYÊN CẦN  |  Mặc định: Tháng hiện tại"),("text","Chương trình [Tất cả v] Phòng ban [Tất cả v] TTS [________] Khoảng [Tháng này v] [Lọc]"),("text","TTS | Kỳ vọng | Đi làm | Vắng | Thiếu checkout | Đã sửa | Tổng giờ | Tỷ lệ | Nghỉ phép"),("text","Nguyễn Văn A | 22 | 20 | 2 | 1 | 2 | 164:30 | 90,9% | Chưa có dữ liệu"),("text","Trần Thị B | 22 | 22 | 0 | 0 | 0 | 176:00 | 100% | Chưa có dữ liệu"),("text","TỔNG NHÓM | 44 | 42 | 2 | 1 | 2 | 340:30 | 95,5% | Chưa có dữ liệu"),("buttons","Sắp xếp: Tỷ lệ tăng dần   [Xem chi tiết TTS đã chọn]")], "Dòng tổng tính trên toàn bộ nhóm lọc, không chỉ trang; hôm nay chưa kết thúc không tính vắng."),
("WF15 Chi tiết chuyên cần và ngày nghỉ chung", [("bar","CHI TIẾT NGUYỄN VĂN A  |  Tháng 10 2026  |  [Quay lại báo cáo]"),("text","Ngày | Thứ | Phân loại | Vào | Ra | Tổng | Nhãn | Liên kết"),("text","02/10 | T6 | Đi làm | 08:31 | 17:29 | 08:58 | - | [Bản ghi]"),("text","03/10 | T7 | Ngoài ngày kỳ vọng có chấm công | 08:45 | 12:00 | 03:15 | - | [Bản ghi]"),("text","05/10 | T2 | Ngày nghỉ chung - Ngày thành lập | - | - | - | - | -"),("text","06/10 | T3 | Vắng | - | - | - | - | -"),("bar","QUẢN LÝ NGÀY NGHỈ CHUNG: Ngày [dd/mm/yyyy]  Tên [________________]  [Thêm]"),("buttons","[Sửa] [Xóa] ngày nghỉ đã chọn")], "Báo cáo chỉ đọc; sửa chấm công qua liên kết. Xóa ngày nghỉ phải xác nhận tác động tính lại."),
]

for idx, (title_text, rows, note) in enumerate(wireframes):
    if idx > 0:
        doc.add_page_break()
    wf_box(doc, title_text, rows, note)
    if title_text.startswith("WF05"):
        p(doc, "Validation chính: ngày đóng không trước ngày mở; ngày kết thúc thực tập không trước ngày bắt đầu; chỉ tiêu lớn hơn 0; tên không trùng trong cùng phòng ban; ít nhất một thứ làm việc.")

portrait(doc)
heading(doc, "9 Ma trận điều hướng chính", 1)
table(doc, ["Vai trò", "Menu đề xuất", "Điểm vào chính"], [
["Ứng viên", "Chương trình, Hồ sơ của tôi, Thông báo, Tài khoản", "Danh sách chương trình đang nhận hồ sơ"],
["Thực tập sinh", "Trang chủ, Hợp đồng, Lịch của tôi, Chấm công, Hồ sơ, Thông báo", "Tổng quan hợp đồng và chấm công hôm nay"],
["HR", "Dashboard, Chương trình, Hồ sơ ứng tuyển, Email kết quả, Hợp đồng, Phân công mentor, Chấm công, Báo cáo", "Dashboard HR"],
["Mentor", "Thực tập sinh của tôi, Chấm công nhóm", "Danh sách TTS đang hướng dẫn"],
], [1.2, 4.0, 1.7])

heading(doc, "10 Các quyết định cần giữ nhất quán khi code", 1)
bullets(doc, [
    "Kết quả xét duyệt chỉ hiển thị cho ứng viên sau khi email gửi thành công; trước đó HR còn có thể thu hồi hoặc sửa.",
    "Nguồn ngày kỳ thực tập ưu tiên hợp đồng hiện hành; nếu chưa có thì lấy chương trình. Quy tắc này dùng chung cho lịch, chấm công và báo cáo.",
    "Mỗi hồ sơ ứng tuyển thuộc một user và một chương trình; một user có thể nộp nhiều chương trình nhưng tối đa một hồ sơ mỗi chương trình.",
    "Yêu cầu giấy tờ của chương trình bị khóa khi đã có hồ sơ nộp; dữ liệu nháp vẫn giữ khi chương trình đóng nhưng không được nộp.",
    "Mentor phải active và cùng phòng ban của chương trình; đổi mentor ảnh hưởng quyền xem lịch và chấm công ngay sau tải lại.",
    "Thiếu check-out không tự sinh giờ ra. Bản ghi hủy vẫn tồn tại trong lịch sử và không tính giờ.",
    "Nghỉ phép chưa có dữ liệu nghiệp vụ trong Sprint 2; giao diện báo cáo phải ghi rõ Chưa có dữ liệu thay vì 0.",
])

doc.add_page_break()
heading(doc, "11 Checklist nghiệm thu Sprint 2", 1)
checks = [
["□","Migration chạy sạch và giữ dữ liệu cũ"],["□","Tất cả 11 user story có luồng chính và ngoại lệ quan trọng"],["□","Mỗi thao tác ghi quan trọng có audit"],["□","Email job chống gửi trùng và có retry"],["□","Tệp chỉ tải qua endpoint kiểm quyền"],["□","Kiểm thử concurrent cho nộp, duyệt, xác nhận, check-in"],["□","Menu và endpoint đúng role"],["□","Báo cáo khớp dữ liệu chấm công"],["□","Maven build và regression pass"],["□","Demo xuyên suốt từ chương trình đến báo cáo"],
]
table(doc, ["Đánh dấu", "Điều kiện"], checks, [.9, 5.9])

footer = doc.sections[0].footer
par = footer.paragraphs[0]
par.text = ""
par.alignment = WD_ALIGN_PARAGRAPH.CENTER
set_font(par.add_run("Sprint 2 - Hệ thống quản lý thực tập sinh"))

doc.core_properties.title = "Kế hoạch công việc và wireframe Sprint 2"
doc.core_properties.subject = "Backlog và wireframe cho hệ thống quản lý thực tập sinh"
doc.core_properties.author = "Nhóm phát triển Intern Management"
doc.save(OUT)
print(OUT)
