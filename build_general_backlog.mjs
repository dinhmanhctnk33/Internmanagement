import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "outputs/sprint2-jira/reorganized/Sprint_2_Jira_Backlog_Theo_User_Story.xlsx";
const outputDir = "outputs/sprint2-jira/generalized";
const outputPath = `${outputDir}/Sprint_2_Jira_Backlog_Tong_Quat.xlsx`;

const scopes = {
  1008: "hệ thống dùng chung của Sprint 2",
  1009: "thực tập sinh đăng ký tài khoản và nộp hồ sơ trực tuyến",
  1010: "HR xét duyệt hoặc từ chối hồ sơ ứng tuyển",
  1011: "hệ thống gửi email thông báo kết quả xét duyệt",
  1012: "HR tải lên và quản lý hợp đồng thực tập",
  1013: "thực tập sinh xem và xác nhận hợp đồng",
  1014: "HR tạo và quản lý chương trình thực tập",
  1015: "HR phân công thực tập sinh cho mentor",
  1016: "thực tập sinh xem lịch thực tập cá nhân",
  1017: "thực tập sinh check-in, check-out và quản lý chấm công",
  1018: "HR xem báo cáo đi làm và nghỉ phép",
  1019: "hoàn thiện, bảo mật, triển khai và nghiệm thu Sprint 2",
};

const categoryOrder = ["Dữ liệu", "Nghiệp vụ", "Giao diện", "Kiểm thử", "Vận hành"];

function code(text) {
  return String(text ?? "").match(/^\[([^\]]+)\]/)?.[1] ?? "";
}

function category(row, idx) {
  const title = String(row[idx["Summary"]] ?? "").toLowerCase();
  const assignee = String(row[idx["Assignee Role"]] ?? "").toUpperCase();
  if (assignee === "QA" || /(^|\])\s*test\b|kiểm thử|regression/.test(title)) return "Kiểm thử";
  if (assignee === "FE" || /dựng trang|dựng màn|giao diện|menu theo role|badge trạng thái/.test(title)) return "Giao diện";
  if (assignee.includes("DEVOPS") || /cấu hình smtp|biến môi trường|retention log|triển khai/.test(title)) return "Vận hành";
  if (/migration|tạo\s+(applications|program_document|application_decisions|email_result_queue|contract_versions|mentor_assignment|calendar_events|public_holidays)|mở rộng\s+(internship_programs|contracts|attendance_records)|bổ sung\s+(users\.|application_decisions)|enum trạng thái/.test(title)) return "Dữ liệu";
  return "Nghiệp vụ";
}

function generalTask(cat, scope) {
  if (cat === "Dữ liệu") return `Xây dựng cấu trúc dữ liệu phục vụ ${scope}`;
  if (cat === "Nghiệp vụ") return `Xây dựng các luồng xử lý nghiệp vụ cho ${scope}`;
  if (cat === "Giao diện") return `Xây dựng giao diện cho ${scope}`;
  if (cat === "Kiểm thử") return `Kiểm thử toàn bộ quy trình ${scope}`;
  return `Cấu hình và triển khai các thành phần phục vụ ${scope}`;
}

function sumDays(rows, idx) {
  let total = 0;
  for (const row of rows) {
    const m = String(row[idx["Original Estimate"]] ?? "").match(/([0-9.]+)d/i);
    if (m) total += Number(m[1]);
  }
  return total ? `${total}d` : "";
}

function unique(rows, idx, key) {
  return [...new Set(rows.map(r => String(r[idx[key]] ?? "").trim()).filter(Boolean))].join(", ");
}

await fs.mkdir(outputDir, { recursive: true });
const wb = await SpreadsheetFile.importXlsx(await FileBlob.load(inputPath));
const source = wb.worksheets.getItem("Jira Import");
const values = source.getUsedRange().values;
const headers = values[3];
const idx = Object.fromEntries(headers.map((h, i) => [h, i]));
const records = values.slice(4).filter(r => r[idx["Issue ID"]] != null);
const stories = records.filter(r => r[idx["Issue Type"]] === "Story");
const tasks = records.filter(r => r[idx["Issue Type"]] === "Sub-task");

const sheet = wb.worksheets.add("Công việc tổng quát");
sheet.showGridLines = false;
sheet.getRange("A1:G1").merge();
sheet.getRange("A1").values = [["CÔNG VIỆC TỔNG QUÁT THEO USER STORY"]];
sheet.getRange("A2:G2").merge();
sheet.getRange("A2").values = [["Các công việc kỹ thuật chi tiết được gom thành nhóm dữ liệu, nghiệp vụ, giao diện, kiểm thử và vận hành."]];
sheet.getRange("A1:G1").format = { fill: "#163A5F", font: { name: "Arial", size: 16, bold: true, color: "#FFFFFF" }, rowHeight: 30, verticalAlignment: "center" };
sheet.getRange("A2:G2").format = { font: { name: "Arial", size: 10, italic: true, color: "#475569" }, rowHeight: 25, wrapText: true };

let row = 4;
let generalCount = 0;
for (const story of stories) {
  const storyId = Number(story[idx["Issue ID"]]);
  const storyTasks = tasks.filter(t => Number(t[idx["Parent ID"]]) === storyId);
  const storyLabel = String(story[idx["Labels"]] ?? "").split(/\s+/).find(x => /^st-s2-/i.test(x))?.toUpperCase() ?? `STORY-${storyId}`;
  sheet.getRange(`A${row}:G${row}`).merge();
  sheet.getRange(`A${row}`).values = [[`${storyLabel} — ${story[idx["Summary"]]}`]];
  sheet.getRange(`A${row}:G${row}`).format = { fill: "#DCEAF7", font: { name: "Arial", size: 12, bold: true, color: "#163A5F" }, rowHeight: 28, verticalAlignment: "center" };
  row++;
  sheet.getRange(`A${row}:G${row}`).values = [["Nhóm", "Công việc tổng quát", "Bao gồm công việc", "Người thực hiện", "Người kiểm tra", "Tổng ước lượng", "Ưu tiên"]];
  sheet.getRange(`A${row}:G${row}`).format = { fill: "#356A93", font: { name: "Arial", size: 10, bold: true, color: "#FFFFFF" }, horizontalAlignment: "center", verticalAlignment: "center", wrapText: true, rowHeight: 27 };
  row++;
  const start = row;
  for (const cat of categoryOrder) {
    const grouped = storyTasks.filter(t => category(t, idx) === cat);
    if (!grouped.length) continue;
    sheet.getRange(`A${row}:G${row}`).values = [[
      cat,
      generalTask(cat, scopes[storyId] ?? String(story[idx["Summary"]]).toLowerCase()),
      grouped.map(t => code(t[idx["Summary"]])).filter(Boolean).join(", "),
      unique(grouped, idx, "Assignee Role"),
      unique(grouped, idx, "Reviewer Role"),
      sumDays(grouped, idx),
      unique(grouped, idx, "Priority"),
    ]];
    generalCount++;
    row++;
  }
  sheet.getRange(`A${start}:G${row - 1}`).format = { font: { name: "Arial", size: 10, color: "#1F2937" }, verticalAlignment: "top", wrapText: true, rowHeight: 38, borders: { bottom: { style: "thin", color: "#E5E7EB" } } };
  sheet.getRange(`A${start}:A${row - 1}`).format.font.bold = true;
  sheet.getRange(`A${start}:A${row - 1}`).format.font.color = "#285D84";
  sheet.getRange(`C${start}:G${row - 1}`).format.horizontalAlignment = "center";
  row++;
}

sheet.getRange("A:A").format.columnWidth = 13;
sheet.getRange("B:B").format.columnWidth = 55;
sheet.getRange("C:C").format.columnWidth = 28;
sheet.getRange("D:E").format.columnWidth = 17;
sheet.getRange("F:G").format.columnWidth = 15;
sheet.freezePanes.freezeRows(2);

console.log((await wb.inspect({ kind: "table", sheetId: "Công việc tổng quát", range: `A1:G${Math.min(row, 45)}`, include: "values,formulas", tableMaxRows: 45, tableMaxCols: 7, maxChars: 16000 })).ndjson);
console.log((await wb.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!", options: { useRegex: true, maxResults: 300 }, summary: "final formula error scan" })).ndjson);
const preview = await wb.render({ sheetName: "Công việc tổng quát", range: `A1:G${row - 1}`, scale: 1, format: "png" });
await fs.writeFile(`${outputDir}/Cong_viec_tong_quat_preview.png`, new Uint8Array(await preview.arrayBuffer()));
const output = await SpreadsheetFile.exportXlsx(wb);
await output.save(outputPath);
console.log(JSON.stringify({ outputPath, stories: stories.length, originalTasks: tasks.length, generalTasks: generalCount }));
