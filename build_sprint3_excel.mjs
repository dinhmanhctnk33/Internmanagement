import fs from "node:fs/promises";
import { Workbook, SpreadsheetFile } from "@oai/artifact-tool";

const outDir = "outputs/sprint3";
const outPath = `${outDir}/Sprint_3_Cong_Viec_Theo_User_Story.xlsx`;

const stories = [
  ["US-S3-01", "Mentor giao nhiệm vụ cho thực tập sinh", "Mentor"],
  ["US-S3-02", "Thực tập sinh cập nhật tiến độ công việc", "Thực tập sinh"],
  ["US-S3-03", "Thực tập sinh nộp báo cáo tuần", "Thực tập sinh"],
  ["US-S3-04", "Mentor xem báo cáo và phản hồi", "Mentor"],
  ["US-S3-05", "Mentor đánh giá kỹ năng và thái độ của thực tập sinh", "Mentor"],
  ["US-S3-06", "HR tổng hợp đánh giá thành báo cáo cuối kỳ", "HR"],
  ["US-S3-07", "HR thiết lập lịch làm việc linh hoạt", "HR"],
  ["US-S3-08", "Thực tập sinh đăng ký nghỉ phép", "Thực tập sinh"],
  ["US-S3-09", "HR thêm mới mentor", "HR"],
  ["US-S3-10", "HR gán mentor cho thực tập sinh", "HR"],
  ["US-S3-11", "HR xem số lượng thực tập sinh mỗi mentor quản lý", "HR"],
  ["US-S3-12", "Hệ thống gửi email tự động khi có lịch họp", "Hệ thống"],
  ["US-S3-13", "Thực tập sinh nhận thông báo trên ứng dụng", "Thực tập sinh"],
];

const configs = {
  "US-S3-01": { scope:"mentor giao và quản lý nhiệm vụ của thực tập sinh", dep:"US-S3-10", priority:"Highest" },
  "US-S3-02": { scope:"thực tập sinh cập nhật tiến độ và kết quả công việc", dep:"US-S3-01", priority:"Highest" },
  "US-S3-03": { scope:"thực tập sinh lập và nộp báo cáo tuần", dep:"US-S3-01", priority:"High" },
  "US-S3-04": { scope:"mentor xem, nhận xét và yêu cầu chỉnh sửa báo cáo tuần", dep:"US-S3-03", priority:"High" },
  "US-S3-05": { scope:"mentor đánh giá kỹ năng và thái độ của thực tập sinh", dep:"US-S3-01, US-S3-03", priority:"High" },
  "US-S3-06": { scope:"HR tổng hợp đánh giá và lập báo cáo cuối kỳ", dep:"US-S3-05", priority:"High" },
  "US-S3-07": { scope:"HR thiết lập lịch làm việc linh hoạt cho từng nhóm", dep:"", priority:"High" },
  "US-S3-08": { scope:"thực tập sinh đăng ký và theo dõi nghỉ phép", dep:"US-S3-07", priority:"High" },
  "US-S3-09": { scope:"HR thêm mới và quản lý thông tin mentor", dep:"", priority:"High" },
  "US-S3-10": { scope:"HR gán và thay đổi mentor cho thực tập sinh", dep:"US-S3-09", priority:"Highest" },
  "US-S3-11": { scope:"HR theo dõi số lượng thực tập sinh của từng mentor", dep:"US-S3-10", priority:"Medium" },
  "US-S3-12": { scope:"hệ thống gửi email tự động khi lịch họp được tạo hoặc thay đổi", dep:"US-S3-07, US-S3-10", priority:"Medium" },
  "US-S3-13": { scope:"thực tập sinh nhận và quản lý thông báo trong ứng dụng", dep:"US-S3-01, US-S3-07, US-S3-08", priority:"High" },
};

const taskTemplates = [
  ["DATA", "Xây dựng cấu trúc dữ liệu phục vụ {scope}", "Dữ liệu được lưu đầy đủ, có lịch sử và liên kết đúng đối tượng.", "BE-1", "DB/DevOps", "1d"],
  ["BE", "Xây dựng các luồng xử lý nghiệp vụ cho {scope}", "Luồng tạo, cập nhật, xem và kiểm soát quyền hoạt động đúng.", "BE-2", "QA", "2d"],
  ["FE", "Xây dựng giao diện cho {scope}", "Giao diện dễ sử dụng, hiển thị đúng dữ liệu và trạng thái.", "FE", "BE-2", "2d"],
  ["QA", "Kiểm thử toàn bộ quy trình {scope}", "Các luồng chính, ngoại lệ và phân quyền đều có bằng chứng kiểm thử.", "QA", "PO", "1d"],
];

const wb = Workbook.create();
const sheet = wb.worksheets.add("Theo User Story");
sheet.showGridLines = false;
sheet.getRange("A1:H1").merge();
sheet.getRange("A1").values = [["SPRINT 3 - CÔNG VIỆC THEO USER STORY"]];
sheet.getRange("A2:H2").merge();
sheet.getRange("A2").values = [["Các công việc được mô tả ở mức tổng quát theo dữ liệu, nghiệp vụ, giao diện và kiểm thử."]];
sheet.getRange("A1:H1").format = { fill:"#163A5F", font:{name:"Arial",size:16,bold:true,color:"#FFFFFF"}, rowHeight:30, verticalAlignment:"center" };
sheet.getRange("A2:H2").format = { font:{name:"Arial",size:10,italic:true,color:"#475569"}, rowHeight:25, wrapText:true };

let row = 4;
let taskNo = 1;
for (const [storyId, title, actor] of stories) {
  const cfg = configs[storyId];
  sheet.getRange(`A${row}:H${row}`).merge();
  sheet.getRange(`A${row}`).values = [[`${storyId} - ${title}`]];
  sheet.getRange(`A${row}:H${row}`).format = { fill:"#DCEAF7", font:{name:"Arial",size:12,bold:true,color:"#163A5F"}, rowHeight:29, verticalAlignment:"center" };
  row++;
  sheet.getRange(`A${row}:H${row}`).values = [["Mã", "Công việc cần làm", "Kết quả cần đạt", "Người thực hiện", "Người kiểm tra", "Ước lượng", "Ưu tiên", "Phụ thuộc"]];
  sheet.getRange(`A${row}:H${row}`).format = { fill:"#356A93", font:{name:"Arial",size:10,bold:true,color:"#FFFFFF"}, horizontalAlignment:"center", verticalAlignment:"center", wrapText:true, rowHeight:27 };
  row++;
  const start = row;
  for (let i=0; i<taskTemplates.length; i++) {
    const [kind, text, result, owner, reviewer, estimate] = taskTemplates[i];
    const id = `S3-${String(taskNo).padStart(2,"0")}`;
    const dependency = i === 0 ? cfg.dep : (i === 1 ? id.replace(/\d+$/, n=>String(Number(n)-1).padStart(2,"0")) : i === 2 ? `S3-${String(taskNo-1).padStart(2,"0")}` : `S3-${String(taskNo-2).padStart(2,"0")} đến S3-${String(taskNo-1).padStart(2,"0")}`);
    sheet.getRange(`A${row}:H${row}`).values = [[id, text.replace("{scope}",cfg.scope), result, owner, reviewer, estimate, cfg.priority, dependency]];
    taskNo++; row++;
  }
  sheet.getRange(`A${start}:H${row-1}`).format = { font:{name:"Arial",size:10,color:"#1F2937"}, verticalAlignment:"top", wrapText:true, rowHeight:40, borders:{bottom:{style:"thin",color:"#E5E7EB"}} };
  sheet.getRange(`A${start}:A${row-1}`).format.horizontalAlignment = "center";
  sheet.getRange(`D${start}:H${row-1}`).format.horizontalAlignment = "center";
  row++;
}

sheet.getRange("A:A").format.columnWidth = 11;
sheet.getRange("B:B").format.columnWidth = 55;
sheet.getRange("C:C").format.columnWidth = 42;
sheet.getRange("D:E").format.columnWidth = 16;
sheet.getRange("F:H").format.columnWidth = 14;
sheet.freezePanes.freezeRows(2);

const summary = wb.worksheets.add("Danh sách User Story");
summary.showGridLines = false;
summary.getRange("A1:D1").merge();
summary.getRange("A1").values = [["SPRINT 3 - DANH SÁCH USER STORY"]];
summary.getRange("A1:D1").format = { fill:"#163A5F", font:{name:"Arial",size:16,bold:true,color:"#FFFFFF"}, rowHeight:30 };
summary.getRange("A3:D3").values = [["Mã", "Vai trò", "User story", "Số công việc"]];
summary.getRange("A3:D3").format = { fill:"#356A93", font:{name:"Arial",size:10,bold:true,color:"#FFFFFF"}, horizontalAlignment:"center" };
summary.getRange(`A4:D${3+stories.length}`).values = stories.map(s=>[s[0],s[2],s[1],4]);
summary.getRange(`A4:D${3+stories.length}`).format = { font:{name:"Arial",size:10}, wrapText:true, verticalAlignment:"top", rowHeight:30, borders:{bottom:{style:"thin",color:"#E5E7EB"}} };
summary.getRange("A:A").format.columnWidth = 14;
summary.getRange("B:B").format.columnWidth = 18;
summary.getRange("C:C").format.columnWidth = 62;
summary.getRange("D:D").format.columnWidth = 15;
summary.getRange(`A4:B${3+stories.length}`).format.horizontalAlignment = "center";
summary.getRange(`D4:D${3+stories.length}`).format.horizontalAlignment = "center";
summary.freezePanes.freezeRows(3);

await fs.mkdir(outDir,{recursive:true});
console.log((await wb.inspect({kind:"table",sheetId:"Theo User Story",range:"A1:H35",include:"values,formulas",tableMaxRows:35,tableMaxCols:8,maxChars:14000})).ndjson);
console.log((await wb.inspect({kind:"match",searchTerm:"#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A|#NUM!|#NULL!|#SPILL!|#CALC!",options:{useRegex:true,maxResults:300},summary:"final formula error scan"})).ndjson);
for (const [name,file] of [["Theo User Story","excel_tasks.png"],["Danh sách User Story","excel_summary.png"]]) {
  const p = await wb.render({sheetName:name,autoCrop:"all",scale:1,format:"png"});
  await fs.writeFile(`${outDir}/${file}`,new Uint8Array(await p.arrayBuffer()));
}
const out = await SpreadsheetFile.exportXlsx(wb);
await out.save(outPath);
console.log(JSON.stringify({outPath,stories:stories.length,tasks:taskNo-1}));
