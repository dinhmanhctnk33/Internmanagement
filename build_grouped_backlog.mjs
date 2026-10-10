import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "outputs/sprint2-jira/Sprint_2_Jira_Backlog_Chi_Tiet.xlsx";
const outputDir = "outputs/sprint2-jira/reorganized";
const outputPath = `${outputDir}/Sprint_2_Jira_Backlog_Theo_User_Story.xlsx`;

function taskCode(text) {
  return (String(text ?? "").match(/^\[([^\]]+)\]/)?.[1] ?? "").trim();
}

function simplify(text) {
  let s = String(text ?? "").replace(/^\[[^\]]+\]\s*/, "").trim();
  const replacements = [
    [/^Dựng\s+/i, "Xây dựng giao diện "],
    [/^Test\s+/i, "Kiểm thử "],
    [/^CRUD\s+/i, "Xây dựng chức năng tạo, xem, sửa và xóa "],
    [/^API DAO Service\s+/i, "Xây dựng DAO, service và API cho "],
    [/^ProgramDAO Service\s+/i, "Xây dựng DAO và service chương trình để "],
    [/^CalendarService\s+/i, "Xây dựng dịch vụ lịch để "],
    [/^AttendanceReportService\s+/i, "Xây dựng dịch vụ báo cáo chấm công để "],
    [/^Scheduler\s+/i, "Tạo bộ lập lịch để "],
    [/^Job\s+/i, "Tạo tác vụ tự động để "],
    [/^Check-in check-out\s+/i, "Xây dựng chức năng check-in/check-out "],
    [/^Mở rộng\s+/i, "Bổ sung dữ liệu cho "],
    [/^Tích hợp\s+/i, "Kết nối chức năng "],
    [/^Truy vấn\s+/i, "Xây dựng truy vấn "],
    [/^Cấp nhật\s+/i, "Cập nhật "],
  ];
  for (const [pattern, value] of replacements) s = s.replace(pattern, value);
  s = s.replace(/;\s*/g, ". ").replace(/\s+/g, " ").replace(/\s+([,.])/g, "$1");
  s = s.replace(/(^|\.\s+)([a-zà-ỹ])/giu, (_, p, c) => p + c.toUpperCase());
  return s.charAt(0).toUpperCase() + s.slice(1);
}

function firstCriterion(text) {
  const s = String(text ?? "").replace(/\r/g, "").trim();
  const first = s.split("\n").map(x => x.replace(/^\d+[.)]\s*/, "").trim()).find(Boolean) ?? "";
  return first.replace(/;\s*/g, ". ");
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

const sheet = wb.worksheets.add("Theo User Story");
sheet.showGridLines = false;
sheet.getRange("A1:H1").merge();
sheet.getRange("A1").values = [["CÔNG VIỆC SPRINT 2 THEO USER STORY"]];
sheet.getRange("A2:H2").merge();
sheet.getRange("A2").values = [["Các công việc đã được nhóm theo user story và viết lại ngắn gọn để dễ giao việc, theo dõi và kiểm thử."]];
sheet.getRange("A1:H1").format = { fill: "#163A5F", font: { name: "Arial", size: 16, bold: true, color: "#FFFFFF" }, rowHeight: 30, verticalAlignment: "center" };
sheet.getRange("A2:H2").format = { font: { name: "Arial", size: 10, italic: true, color: "#475569" }, rowHeight: 26, wrapText: true, verticalAlignment: "center" };

let row = 4;
for (const story of stories) {
  const storyId = story[idx["Issue ID"]];
  const storyTasks = tasks.filter(t => Number(t[idx["Parent ID"]]) === Number(storyId));
  const storyLabel = `${story[idx["Labels"]] ?? ""}`.split(/\s+/).find(x => /^st-s2-/i.test(x))?.toUpperCase() ?? `STORY-${storyId}`;
  sheet.getRange(`A${row}:H${row}`).merge();
  sheet.getRange(`A${row}`).values = [[`${storyLabel} — ${story[idx["Summary"]]}`]];
  sheet.getRange(`A${row}:H${row}`).format = { fill: "#DCEAF7", font: { name: "Arial", size: 12, bold: true, color: "#163A5F" }, rowHeight: 28, verticalAlignment: "center" };
  row++;
  sheet.getRange(`A${row}:H${row}`).values = [["Mã", "Công việc cần làm", "Kết quả cần đạt", "Người thực hiện", "Người kiểm tra", "Ước lượng", "Ưu tiên", "Phụ thuộc"]];
  sheet.getRange(`A${row}:H${row}`).format = { fill: "#356A93", font: { name: "Arial", size: 10, bold: true, color: "#FFFFFF" }, horizontalAlignment: "center", verticalAlignment: "center", wrapText: true, rowHeight: 26, borders: { preset: "inside", style: "thin", color: "#DCE6EF" } };
  row++;
  const start = row;
  for (const task of storyTasks) {
    sheet.getRange(`A${row}:H${row}`).values = [[
      taskCode(task[idx["Summary"]]),
      simplify(task[idx["Summary"]]),
      firstCriterion(task[idx["Acceptance Criteria"]]),
      task[idx["Assignee Role"]],
      task[idx["Reviewer Role"]],
      task[idx["Original Estimate"]],
      task[idx["Priority"]],
      task[idx["Dependencies"]],
    ]];
    row++;
  }
  if (row > start) {
    const body = sheet.getRange(`A${start}:H${row - 1}`);
    body.format = { font: { name: "Arial", size: 10, color: "#1F2937" }, verticalAlignment: "top", wrapText: true, borders: { bottom: { style: "thin", color: "#E5E7EB" } } };
    sheet.getRange(`A${start}:A${row - 1}`).format.horizontalAlignment = "center";
    sheet.getRange(`D${start}:H${row - 1}`).format.horizontalAlignment = "center";
    sheet.getRange(`A${start}:H${row - 1}`).format.rowHeight = 38;
  }
  row += 1;
}

sheet.getRange(`A1:H${row}`).format.font.name = "Arial";
sheet.getRange(`A1:H${row}`).format.verticalAlignment = "top";
sheet.getRange("A:A").format.columnWidth = 11;
sheet.getRange("B:B").format.columnWidth = 48;
sheet.getRange("C:C").format.columnWidth = 38;
sheet.getRange("D:E").format.columnWidth = 16;
sheet.getRange("F:H").format.columnWidth = 13;
sheet.freezePanes.freezeRows(2);

const check = await wb.inspect({ kind: "table", sheetId: "Theo User Story", range: `A1:H${Math.min(row, 35)}`, include: "values,formulas", tableMaxRows: 35, tableMaxCols: 8, maxChars: 15000 });
console.log(check.ndjson);
const errors = await wb.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!", options: { useRegex: true, maxResults: 300 }, summary: "final formula error scan" });
console.log(errors.ndjson);
const preview = await wb.render({ sheetName: "Theo User Story", range: `A1:H${row - 1}`, scale: 1, format: "png" });
await fs.writeFile(`${outputDir}/Theo_User_Story_preview.png`, new Uint8Array(await preview.arrayBuffer()));
const output = await SpreadsheetFile.exportXlsx(wb);
await output.save(outputPath);
console.log(JSON.stringify({ outputPath, stories: stories.length, tasks: tasks.length, rows: row - 1 }));
