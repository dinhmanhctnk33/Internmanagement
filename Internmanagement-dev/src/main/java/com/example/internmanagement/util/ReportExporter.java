package com.example.internmanagement.util;

import com.example.internmanagement.model.ReportPayload;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Tạo file PDF và Excel từ nội dung một bản chụp báo cáo (ReportPayload). */
public class ReportExporter {
    private static final String NO_DATA = "chưa có dữ liệu";
    private static final String TOO_SMALL = "ẩn vì nhóm quá nhỏ";
    private static final String NOT_EVALUATED = "Chưa có đánh giá";
    private static final Color HEADER_BG = new Color(0xE8, 0xEE, 0xF7);
    private static final DecimalFormatSymbols VI = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("vi-VN"));

    private static BaseFont[] fontCache; // {thường, đậm}

    private ReportExporter() {}

    // ==================================================================
    // PDF
    // ==================================================================

    /** Báo cáo LEADER: toàn bộ. Báo cáo SCHOOL: chỉ trường schoolName (mỗi trường một file). */
    public static byte[] toPdf(ReportPayload p, String schoolName) {
        BaseFont[] bf = fonts();
        Font title = new Font(bf[1], 15);
        Font h2 = new Font(bf[1], 12);
        Font bold = new Font(bf[1], 10);
        Font normal = new Font(bf[0], 10);
        Font note = new Font(bf[0], 9);

        boolean leader = "LEADER".equals(p.reportType);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(leader ? PageSize.A4 : PageSize.A4.rotate(), 36, 36, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();
            if (leader) writeLeaderPdf(doc, p, title, h2, bold, normal, note);
            else writeSchoolPdf(doc, p, schoolName, title, bold, normal, note);
            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Không thể tạo file PDF.", e);
        }
    }

    private static void writeLeaderPdf(Document doc, ReportPayload p, Font title, Font h2, Font bold,
                                       Font normal, Font note) throws DocumentException {
        Paragraph t = new Paragraph("BÁO CÁO TỔNG HỢP THỰC TẬP CUỐI KỲ", title);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);
        Paragraph sub = new Paragraph("Chương trình: " + p.programNames + "\nNgày tạo: " + p.createdAt
                + " - Người tạo: " + p.createdByName, normal);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(12);
        doc.add(sub);

        for (ReportPayload.ProgramStat s : p.leader.programs()) {
            Paragraph name = new Paragraph(s.programName(), h2);
            name.setSpacingBefore(8);
            name.setSpacingAfter(4);
            doc.add(name);

            PdfPTable kv = new PdfPTable(new float[]{50, 50});
            kv.setWidthPercentage(100);
            kv.setSpacingAfter(6);
            kv(kv, "Số thực tập sinh", String.valueOf(s.internCount()), bold, normal);
            kv(kv, "Số có đánh giá đã chốt", String.valueOf(s.finalizedCount()), bold, normal);
            kv(kv, "Số chưa có đánh giá đã chốt", String.valueOf(s.notFinalizedCount()), bold, normal);
            kv(kv, "Điểm trung bình nhóm kỹ năng", scoreText(s.avgTechnical(), s), bold, normal);
            kv(kv, "Điểm trung bình nhóm thái độ", scoreText(s.avgAttitude(), s), bold, normal);
            kv(kv, "Điểm trung bình chung", scoreText(s.avgFinal(), s), bold, normal);
            String[] ranges = {"1,0 đến 1,9", "2,0 đến 2,9", "3,0 đến 3,9", "4,0 đến 5,0"};
            for (int i = 0; i < 4; i++) {
                String value = s.distribution() == null ? statusText(s) : s.distribution()[i] + " thực tập sinh";
                kv(kv, "Phân bố điểm chung: " + ranges[i], value, bold, normal);
            }
            kv(kv, "Tỷ lệ chuyên cần trung bình", attendanceText(s), bold, normal);
            kv(kv, "Báo cáo tuần đã nộp", String.valueOf(s.weeklySubmitted()), bold, normal);
            kv(kv, "Báo cáo tuần nộp muộn", s.weeklyLate() == null ? NO_DATA : String.valueOf(s.weeklyLate()),
                    bold, normal);
            doc.add(kv);

            if (s.departments() != null && !s.departments().isEmpty()) {
                PdfPTable d = new PdfPTable(new float[]{28, 12, 12, 16, 16, 16});
                d.setWidthPercentage(100);
                d.setSpacingAfter(8);
                for (String h : new String[]{"Phòng ban", "Số TTS", "Đã chốt", "TB kỹ năng", "TB thái độ", "TB chung"})
                    d.addCell(cell(h, bold, true));
                for (ReportPayload.DepartmentStat dep : s.departments()) {
                    d.addCell(cell(dep.departmentName(), normal, false));
                    d.addCell(cell(String.valueOf(dep.internCount()), normal, false));
                    d.addCell(cell(String.valueOf(dep.finalizedCount()), normal, false));
                    if (dep.finalizedCount() == 0) {
                        spanned(d, NO_DATA, note, 3);
                    } else if (dep.hidden()) {
                        spanned(d, TOO_SMALL, note, 3);
                    } else {
                        d.addCell(cell(num(dep.avgTechnical()), normal, false));
                        d.addCell(cell(num(dep.avgAttitude()), normal, false));
                        d.addCell(cell(num(dep.avgFinal()), normal, false));
                    }
                }
                doc.add(d);
            }
        }
        Paragraph foot = new Paragraph("Báo cáo chỉ gồm số liệu thống kê, không có thông tin cá nhân. "
                + "Điểm chỉ tính trên các thực tập sinh có đánh giá đã chốt.", note);
        foot.setSpacingBefore(6);
        doc.add(foot);
    }

    private static void writeSchoolPdf(Document doc, ReportPayload p, String schoolName, Font title, Font bold,
                                       Font normal, Font note) throws DocumentException {
        ReportPayload.School school = findSchool(p, schoolName);
        Paragraph t = new Paragraph("BÁO CÁO KẾT QUẢ THỰC TẬP CUỐI KỲ", title);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);
        Paragraph sub = new Paragraph("Trường: " + school.schoolName() + "\nChương trình: " + p.programNames
                + "\nNgày tạo: " + p.createdAt, normal);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(10);
        doc.add(sub);

        PdfPTable table = new PdfPTable(new float[]{4, 15, 17, 12, 9, 7, 7, 8, 7, 24});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String h : new String[]{"STT", "Họ tên", "Chương trình / Phòng ban", "Kỳ thực tập",
                "Số ngày đi làm", "Kỹ năng", "Thái độ", "Chuyên cần", "Điểm chung", "Nhận xét chung"})
            table.addCell(cell(h, bold, true));

        int index = 1;
        for (ReportPayload.InternRow r : school.interns()) {
            table.addCell(cell(String.valueOf(index++), normal, false));
            table.addCell(cell(r.fullName(), normal, false));
            table.addCell(cell(r.programName() + (isBlank(r.departmentName()) ? "" : "\n" + r.departmentName()),
                    normal, false));
            table.addCell(cell(orDash(r.startDate()) + " - " + orDash(r.endDate()), normal, false));
            String days = r.expectedDays() == null ? r.attendedDays() + "/" + NO_DATA
                    : r.attendedDays() + "/" + r.expectedDays();
            table.addCell(cell(days, normal, false));
            if (!r.hasEvaluation()) {
                spanned(table, NOT_EVALUATED, note, 5);
            } else {
                table.addCell(cell(num(r.technicalScore()), normal, false));
                table.addCell(cell(num(r.attitudeScore()), normal, false));
                table.addCell(cell(num(r.punctualityScore()), normal, false));
                table.addCell(cell(num(r.finalScore()), normal, false));
                table.addCell(cell(orDash(r.comments()), normal, false));
            }
        }
        doc.add(table);
        Paragraph foot = new Paragraph("Số ngày đi làm: số ngày có check-in / số ngày làm việc kỳ vọng "
                + "(đã trừ ngày nghỉ có phép). Chỉ các đánh giá đã chốt được đưa vào báo cáo.", note);
        foot.setSpacingBefore(6);
        doc.add(foot);
    }

    // ==================================================================
    // EXCEL
    // ==================================================================

    public static byte[] toXlsx(ReportPayload p, String schoolName) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(wb);
            CellStyle wrap = wb.createCellStyle();
            wrap.setWrapText(true);
            wrap.setVerticalAlignment(VerticalAlignment.TOP);

            if ("LEADER".equals(p.reportType)) writeLeaderSheets(wb, p, header, wrap);
            else writeSchoolSheet(wb, p, schoolName, header, wrap);

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không thể tạo file Excel.", e);
        }
    }

    private static void writeLeaderSheets(Workbook wb, ReportPayload p, CellStyle header, CellStyle wrap) {
        Sheet sheet = wb.createSheet("Tổng hợp theo chương trình");
        String[] heads = {"Chương trình", "Số thực tập sinh", "Có đánh giá chốt", "Chưa có đánh giá chốt",
                "TB nhóm kỹ năng", "TB nhóm thái độ", "TB chung", "Điểm 1,0-1,9", "Điểm 2,0-2,9",
                "Điểm 3,0-3,9", "Điểm 4,0-5,0", "Chuyên cần TB (%)", "BC tuần đã nộp", "BC tuần nộp muộn"};
        headerRow(sheet, heads, header);
        int r = 1;
        for (ReportPayload.ProgramStat s : p.leader.programs()) {
            Row row = sheet.createRow(r++);
            put(row, 0, s.programName(), wrap);
            put(row, 1, s.internCount(), null);
            put(row, 2, s.finalizedCount(), null);
            put(row, 3, s.notFinalizedCount(), null);
            put(row, 4, s.avgTechnical() != null ? s.avgTechnical() : statusText(s), null);
            put(row, 5, s.avgAttitude() != null ? s.avgAttitude() : statusText(s), null);
            put(row, 6, s.avgFinal() != null ? s.avgFinal() : statusText(s), null);
            for (int i = 0; i < 4; i++)
                put(row, 7 + i, s.distribution() != null ? (Object) s.distribution()[i] : statusText(s), null);
            put(row, 11, s.avgAttendanceRate() != null ? s.avgAttendanceRate() : attendanceText(s), null);
            put(row, 12, s.weeklySubmitted(), null);
            put(row, 13, s.weeklyLate() != null ? (Object) s.weeklyLate() : NO_DATA, null);
        }
        widths(sheet, 30, 12, 12, 14, 14, 14, 12, 12, 12, 12, 12, 16, 14, 14);

        Sheet depSheet = wb.createSheet("Theo phòng ban");
        headerRow(depSheet, new String[]{"Chương trình", "Phòng ban", "Số thực tập sinh", "Có đánh giá chốt",
                "TB nhóm kỹ năng", "TB nhóm thái độ", "TB chung"}, header);
        int d = 1;
        for (ReportPayload.ProgramStat s : p.leader.programs()) {
            if (s.departments() == null) continue;
            for (ReportPayload.DepartmentStat dep : s.departments()) {
                Row row = depSheet.createRow(d++);
                String text = dep.finalizedCount() == 0 ? NO_DATA : TOO_SMALL;
                boolean show = !dep.hidden() && dep.finalizedCount() > 0;
                put(row, 0, s.programName(), wrap);
                put(row, 1, dep.departmentName(), wrap);
                put(row, 2, dep.internCount(), null);
                put(row, 3, dep.finalizedCount(), null);
                put(row, 4, show && dep.avgTechnical() != null ? dep.avgTechnical() : text, null);
                put(row, 5, show && dep.avgAttitude() != null ? dep.avgAttitude() : text, null);
                put(row, 6, show && dep.avgFinal() != null ? dep.avgFinal() : text, null);
            }
        }
        widths(depSheet, 30, 26, 14, 14, 14, 14, 12);
    }

    private static void writeSchoolSheet(Workbook wb, ReportPayload p, String schoolName, CellStyle header,
                                         CellStyle wrap) {
        ReportPayload.School school = findSchool(p, schoolName);
        Sheet sheet = wb.createSheet("Kết quả thực tập");
        headerRow(sheet, new String[]{"STT", "Họ tên", "Chương trình", "Phòng ban", "Bắt đầu", "Kết thúc",
                "Số ngày đi làm", "Số ngày kỳ vọng", "Kỹ năng", "Thái độ", "Chuyên cần", "Điểm chung",
                "Nhận xét chung", "Ghi chú"}, header);
        int r = 1;
        for (ReportPayload.InternRow i : school.interns()) {
            Row row = sheet.createRow(r);
            put(row, 0, r, null);
            put(row, 1, i.fullName(), wrap);
            put(row, 2, i.programName(), wrap);
            put(row, 3, i.departmentName(), wrap);
            put(row, 4, i.startDate(), null);
            put(row, 5, i.endDate(), null);
            put(row, 6, i.attendedDays(), null);
            put(row, 7, i.expectedDays() != null ? (Object) i.expectedDays() : NO_DATA, null);
            if (i.hasEvaluation()) {
                put(row, 8, i.technicalScore(), null);
                put(row, 9, i.attitudeScore(), null);
                put(row, 10, i.punctualityScore(), null);
                put(row, 11, i.finalScore(), null);
                put(row, 12, i.comments(), wrap);
            } else {
                put(row, 13, NOT_EVALUATED, null);
            }
            r++;
        }
        widths(sheet, 6, 24, 26, 22, 12, 12, 14, 14, 10, 10, 12, 12, 50, 18);
    }

    // ==================================================================
    // HÀM PHỤ
    // ==================================================================
    private static ReportPayload.School findSchool(ReportPayload p, String schoolName) {
        if (p.schools != null) {
            for (ReportPayload.School s : p.schools) {
                if (s.schoolName().equals(schoolName)) return s;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trường trong báo cáo.");
    }

    /** Điểm trung bình theo quy tắc: chưa có đánh giá chốt / nhóm nhỏ thì ghi chú thay vì số. */
    private static String scoreText(Double value, ReportPayload.ProgramStat s) {
        if (value != null) return num(value);
        return statusText(s);
    }

    private static String statusText(ReportPayload.ProgramStat s) {
        if (s.finalizedCount() == 0) return NO_DATA;
        return s.detailHidden() ? TOO_SMALL : NO_DATA;
    }

    private static String attendanceText(ReportPayload.ProgramStat s) {
        if (s.avgAttendanceRate() != null) return num(s.avgAttendanceRate()) + "%";
        if (s.internCount() == 0) return NO_DATA;
        return s.internCount() < 3 ? TOO_SMALL : NO_DATA;
    }

    private static String num(Double value) {
        return value == null ? "-" : new DecimalFormat("0.0#", VI).format(value);
    }

    private static String orDash(String s) {
        return isBlank(s) ? "-" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static void kv(PdfPTable t, String key, String value, Font keyFont, Font valueFont) {
        t.addCell(cell(key, keyFont, true));
        t.addCell(cell(value, valueFont, false));
    }

    private static PdfPCell cell(String text, Font font, boolean header) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setPadding(4);
        if (header) c.setBackgroundColor(HEADER_BG);
        return c;
    }

    private static void spanned(PdfPTable t, String text, Font font, int colspan) {
        PdfPCell c = cell(text, font, false);
        c.setColspan(colspan);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static void headerRow(Sheet sheet, String[] heads, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < heads.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(heads[i]);
            c.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
    }

    private static void put(Row row, int col, Object value, CellStyle style) {
        if (value == null) return;
        Cell c = row.createCell(col);
        if (value instanceof Number n) c.setCellValue(n.doubleValue());
        else c.setCellValue(value.toString());
        if (style != null) c.setCellStyle(style);
    }

    private static void widths(Sheet sheet, int... chars) {
        for (int i = 0; i < chars.length; i++) sheet.setColumnWidth(i, chars[i] * 256);
    }

    // ------------------------------------------------------------------
    // Font Unicode cho tiếng Việt (font mặc định của PDF không có dấu)
    // ------------------------------------------------------------------
    private static synchronized BaseFont[] fonts() {
        if (fontCache != null) return fontCache;
        BaseFont regular = loadFont("fonts/regular.ttf",
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/Library/Fonts/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf");
        BaseFont bold = loadFont("fonts/bold.ttf",
                "C:/Windows/Fonts/arialbd.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
                "/Library/Fonts/Arial Bold.ttf",
                "/System/Library/Fonts/Supplemental/Arial Bold.ttf");
        if (regular == null) {
            throw new IllegalStateException("Không tìm thấy font Unicode để xuất PDF. Hãy đặt file regular.ttf "
                    + "và bold.ttf vào src/main/resources/fonts/.");
        }
        fontCache = new BaseFont[]{regular, bold == null ? regular : bold};
        return fontCache;
    }

    /** Ưu tiên font nằm trong resources (classpath), sau đó thử các đường dẫn font hệ thống. */
    private static BaseFont loadFont(String classpath, String... systemPaths) {
        try (InputStream in = ReportExporter.class.getClassLoader().getResourceAsStream(classpath)) {
            if (in != null) {
                byte[] data = in.readAllBytes();
                return BaseFont.createFont(classpath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, data, null);
            }
        } catch (Exception ignored) {
            // thử nguồn tiếp theo
        }
        for (String path : systemPaths) {
            if (Files.isRegularFile(Path.of(path))) {
                try {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } catch (Exception ignored) {
                    // thử đường dẫn tiếp theo
                }
            }
        }
        return null;
    }
}