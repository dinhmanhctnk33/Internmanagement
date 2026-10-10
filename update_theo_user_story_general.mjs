import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "outputs/sprint2-jira/reorganized/Sprint_2_Jira_Backlog_Theo_User_Story.xlsx";
const outputDir = "outputs/sprint2-jira/theo-user-story-tong-quat";
const outputPath = `${outputDir}/Sprint_2_Jira_Theo_User_Story_Mo_Ta_Tong_Quat.xlsx`;

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

function taskCode(text) {
  return String(text ?? "").match(/^\[([^\]]+)\]/)?.[1] ?? "";
}

function category(row, idx) {
  const title = String(row[idx["Summary"]] ?? "").toLowerCase();
  const assignee = String(row[idx["Assignee Role"]] ?? "").toUpperCase();
  if (assignee === "QA" || /(^|\])\s*test\b|kiểm thử|regression/.test(title)) return "Kiểm thử";
  if (assignee === "FE" || /dựng trang|dựng màn|giao diện|menu theo role|badge trạng thái/.test(title)) return "Giao diện";
  if (assignee.includes("DEVOPS") || /migration|cấu hình smtp|biến môi trường|retention log|triển khai/.test(title)) return "Vận hành";
  if (/tạo\s+(applications|program_document|application_decisions|email_result_queue|contract_versions|mentor_assignment|calendar_events|public_holidays)|mở rộng\s+(internship_programs|contracts|attendance_records)|bổ sung\s+(users\.|application_decisions)|enum trạng thái/.test(title)) return "Dữ liệu";
  return "Nghiệp vụ";
}

function description(cat, scope) {
  if (cat === "Dữ liệu") return `Xây dựng cấu trúc dữ liệu phục vụ ${scope}`;
  if (cat === "Nghiệp vụ") return `Xây dựng các luồng xử lý nghiệp vụ cho ${scope}`;
  if (cat === "Giao diện") return `Xây dựng giao diện cho ${scope}`;
  if (cat === "Kiểm thử") return `Kiểm thử toàn bộ quy trình ${scope}`;
  return `Cấu hình và triển khai các thành phần phục vụ ${scope}`;
}

await fs.mkdir(outputDir, { recursive: true });
const wb = await SpreadsheetFile.importXlsx(await FileBlob.load(inputPath));
const source = wb.worksheets.getItem("Jira Import");
const raw = source.getUsedRange().values;
const headers = raw[3];
const idx = Object.fromEntries(headers.map((h, i) => [h, i]));
const records = raw.slice(4).filter(r => r[idx["Issue ID"]] != null);
const taskMap = new Map();
for (const r of records.filter(r => r[idx["Issue Type"]] === "Sub-task")) {
  taskMap.set(taskCode(r[idx["Summary"]]), r);
}

const sheet = wb.worksheets.getItem("Theo User Story");
const used = sheet.getUsedRange();
const values = used.values;
let updated = 0;
for (let i = 0; i < values.length; i++) {
  const code = String(values[i]?.[0] ?? "").trim();
  const sourceTask = taskMap.get(code);
  if (!sourceTask) continue;
  const parentId = Number(sourceTask[idx["Parent ID"]]);
  sheet.getCell(i, 1).values = [[description(category(sourceTask, idx), scopes[parentId])]];
  updated++;
}
sheet.getRange("A2:H2").values = [["Các công việc trong từng user story được mô tả ở mức tổng quát theo nhóm dữ liệu, nghiệp vụ, giao diện, kiểm thử và vận hành."]];

console.log((await wb.inspect({ kind: "table", sheetId: "Theo User Story", range: "A1:H35", include: "values,formulas", tableMaxRows: 35, tableMaxCols: 8, maxChars: 14000 })).ndjson);
console.log((await wb.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!", options: { useRegex: true, maxResults: 300 }, summary: "final formula error scan" })).ndjson);
const preview = await wb.render({ sheetName: "Theo User Story", range: `A1:H${values.length}`, scale: 1, format: "png" });
await fs.writeFile(`${outputDir}/Theo_User_Story_Tong_Quat_preview.png`, new Uint8Array(await preview.arrayBuffer()));
const output = await SpreadsheetFile.exportXlsx(wb);
await output.save(outputPath);
console.log(JSON.stringify({ outputPath, updated }));
