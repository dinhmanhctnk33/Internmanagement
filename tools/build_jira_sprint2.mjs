import fs from "node:fs/promises";
import path from "node:path";
import { SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const root = process.cwd();
const src = JSON.parse(await fs.readFile(path.join(root, "tmp", "jira_source.json"), "utf8"));
const outDir = path.join(root, "outputs", "sprint2-jira");
await fs.mkdir(outDir, { recursive: true });

const epicDefs = [
  ["EP-S2-01", "Nền tảng dữ liệu và dùng chung"],
  ["EP-S2-02", "Đăng ký và nộp hồ sơ"],
  ["EP-S2-03", "Chương trình thực tập"],
  ["EP-S2-04", "Xét duyệt và email kết quả"],
  ["EP-S2-05", "Hợp đồng thực tập"],
  ["EP-S2-06", "Phân công mentor và lịch cá nhân"],
  ["EP-S2-07", "Chấm công và báo cáo chuyên cần"],
  ["EP-S2-08", "Hoàn thiện và nghiệm thu Sprint 2"],
];

const stories = [
  ["ST-S2-00", "EP-S2-01", "Nền tảng kỹ thuật dùng chung cho Sprint 2", "Technical Enabler", "Highest", "Nhóm hoàn thiện schema, trạng thái, audit, phân quyền, lưu tệp và dữ liệu kiểm thử dùng chung."],
  ["ST-S2-01", "EP-S2-02", "Thực tập sinh đăng ký tài khoản và nộp hồ sơ trực tuyến", "User Story", "Highest", "Thực tập sinh có thể đăng ký, xác thực email, chọn chương trình, lưu nháp, tải hồ sơ và nộp hồ sơ hợp lệ."],
  ["ST-S2-02", "EP-S2-04", "HR duyệt hoặc từ chối hồ sơ", "User Story", "Highest", "HR lọc, xem chi tiết và ra quyết định cho từng hồ sơ hoặc theo lô, có lịch sử và kiểm soát đồng thời."],
  ["ST-S2-03", "EP-S2-04", "Hệ thống gửi email thông báo kết quả xét duyệt", "User Story", "Highest", "Kết quả được xếp hàng gửi theo lịch, chống gửi trùng, có retry, thu hồi trước thời điểm gửi và theo dõi lỗi."],
  ["ST-S2-04", "EP-S2-05", "HR tải lên và quản lý hợp đồng thực tập", "User Story", "High", "HR tạo hợp đồng cho ứng viên được nhận, tải PDF, thay phiên bản trước xác nhận và giữ lịch sử."],
  ["ST-S2-05", "EP-S2-05", "Thực tập sinh xác nhận hợp đồng", "User Story", "High", "Thực tập sinh xem đúng hợp đồng hiện hành, tải xuống và xác nhận hoặc từ chối trong hạn."],
  ["ST-S2-06", "EP-S2-03", "HR tạo chương trình thực tập theo phòng ban và thiết lập thời gian", "User Story", "Highest", "HR tạo và quản lý chương trình, chỉ tiêu, hạn nhận hồ sơ, ngày bắt đầu/kết thúc và ngày làm việc. Ngày chương trình nằm trong cùng form tạo chương trình."],
  ["ST-S2-07", "EP-S2-06", "HR phân công thực tập sinh cho mentor", "User Story", "High", "HR phân công đúng phòng ban, xem tải mentor, nhận đề xuất chia đều và theo dõi lịch sử thay đổi."],
  ["ST-S2-08", "EP-S2-06", "Thực tập sinh xem lịch thực tập cá nhân", "User Story", "Medium", "Thực tập sinh xem lịch tháng/tuần, mốc chương trình hoặc hợp đồng và sự kiện đúng phạm vi cá nhân."],
  ["ST-S2-09", "EP-S2-07", "Thực tập sinh check-in/check-out", "User Story", "High", "Thực tập sinh ghi nhận giờ máy chủ, xem lịch sử, bổ sung hoặc sửa trong hạn; HR và mentor xem theo quyền."],
  ["ST-S2-10", "EP-S2-07", "HR xem báo cáo đi làm và nghỉ phép", "User Story", "High", "HR lọc báo cáo chuyên cần, xem tổng hợp và chi tiết ngày; cột nghỉ phép ghi rõ chưa có dữ liệu nếu chưa phát triển quy trình nghỉ phép."],
  ["ST-S2-11", "EP-S2-08", "Hoàn thiện, bảo mật, triển khai và nghiệm thu", "Technical Enabler", "Highest", "Nhóm hoàn thiện trải nghiệm, rà soát bảo mật, regression, cấu hình môi trường và demo nghiệm thu."],
];

const storyToEpic = Object.fromEntries(stories.map(x => [x[0], x[1]]));
const usToStory = {"US01":"ST-S2-01","US02":"ST-S2-02","US03":"ST-S2-03","US04":"ST-S2-04","US05":"ST-S2-05","US06":"ST-S2-06","US07":"ST-S2-07","US08":"ST-S2-08","US09":"ST-S2-09","US10":"ST-S2-10"};
const splitOverride = {12:"US01",28:"US02",34:"US02",35:"US04",41:"US04"};

function ownerRole(owner, taskNo) {
  if (owner === "FE") return ["FE", "BE-2"];
  if (owner === "QA") return ["QA", taskNo >= 35 ? "BE-2" : "BE-1"];
  if (owner === "DevOps") return ["DB/DevOps", "BE-1"];
  if (owner === "Nhóm") return ["Trưởng nhóm", "QA"];
  if (owner.includes("DB")) return ["BE-1", "DB/DevOps"];
  if (owner.includes("FE")) return ["BE-2", "FE"];
  if (owner.includes("BE")) return [taskNo <= 34 ? "BE-1" : "BE-2", "QA"];
  return [owner, "QA"];
}

function points(summary, owner) {
  const complex = /transaction|scheduler|thuật toán|báo cáo|hàng loạt|migration|FileStorage|regression|bảo mật|check-in|check-out/i.test(summary);
  if (owner === "QA" || owner === "FE") return complex ? 5 : 3;
  return complex ? 5 : 3;
}

function component(storyId) {
  return ({"ST-S2-00":"Platform","ST-S2-01":"Recruitment","ST-S2-02":"Application Review","ST-S2-03":"Notification","ST-S2-04":"Contract","ST-S2-05":"Contract","ST-S2-06":"Program","ST-S2-07":"Mentor","ST-S2-08":"Calendar","ST-S2-09":"Attendance","ST-S2-10":"Reporting","ST-S2-11":"Release"})[storyId];
}

function primaryStory(storyCell, n, groupIndex) {
  if (groupIndex === 0) return "ST-S2-00";
  if (groupIndex === 7) return "ST-S2-11";
  const chosen = splitOverride[n] || storyCell.split(/\s+/).find(x => usToStory[x]);
  return usToStory[chosen] || "ST-S2-11";
}

const wb = Workbook.create();
const jira = wb.worksheets.add("Jira Import");
const board = wb.worksheets.add("Sprint Board");
const members = wb.worksheets.add("Phân công");
const guide = wb.worksheets.add("Hướng dẫn import");
const checks = wb.worksheets.add("DoR-DoD");

const headers = ["Issue ID","Issue Type","Summary","Epic Name","Epic Link","Parent ID","Description","Acceptance Criteria","Priority","Story Points","Original Estimate","Assignee Role","Reviewer Role","Component/s","Labels","Dependencies","Sprint","Status"];
const rows = [];
let issueNo = 1000;
const issueIdByKey = {};
for (const [key, title] of epicDefs) {
  issueIdByKey[key] = issueNo;
  rows.push([issueNo++,"Epic",title,key,"","",`Nhóm chức năng: ${title}.`,"Tất cả story con đạt Definition of Done và được PO nghiệm thu.","Highest","","","Trưởng nhóm","PO","Management",`sprint-2 ${key.toLowerCase()}`,"","Sprint 2","To Do"]);
}
for (const [key, epic, title, type, priority, desc] of stories) {
  issueIdByKey[key] = issueNo;
  rows.push([issueNo++,"Story",title,"",epic,"",desc,"Các sub-task bắt buộc hoàn tất; kiểm tra quyền ở server; luồng đúng, lỗi và gửi lặp đều có bằng chứng test.",priority,"","","Trưởng nhóm","QA",component(key),`sprint-2 ${key.toLowerCase()}`,"","Sprint 2","To Do"]);
}

for (let gi = 0; gi < src.groups.length; gi++) {
  const [, tasks] = src.groups[gi];
  for (const [tid, storyCell, owner, summary, dep, acceptance] of tasks) {
    const n = Number(tid.split("-")[1]);
    const storyId = primaryStory(storyCell, n, gi);
    const [assignee, reviewer] = ownerRole(owner, n);
    const steps = src.specific[String(n)] || [];
    const description = [`Mục tiêu: ${summary}`, "", "Các bước thực hiện:", ...steps.map((s,i)=>`${i+1}. ${s}`), "", `Kết quả bàn giao: mã nguồn/cấu hình liên quan; dữ liệu hoặc giao diện hoạt động; bằng chứng tự kiểm tra cho reviewer.`].join("\n");
    const ac = [`1. ${acceptance}.`,`2. Server từ chối rõ ràng khi sai quyền hoặc dữ liệu không hợp lệ.`,`3. Không làm hỏng luồng hiện có liên quan; có bằng chứng test và reviewer xác nhận.`].join("\n");
    rows.push([issueNo++,"Sub-task",`[${tid}] ${summary.replace(/\.$/,"")}`,"","",issueIdByKey[storyId],description,ac,n <= 6 || n >= 63 ? "Highest" : (storyId === "ST-S2-08" ? "Medium" : "High"),points(summary,owner),points(summary,owner) <= 3 ? "1d" : "2d",assignee,reviewer,component(storyId),`sprint-2 ${tid.toLowerCase()}`,dep,"Sprint 2","To Do"]);
  }
}

function titleBlock(sheet, title, subtitle, lastCol) {
  sheet.getRange(`A1:${lastCol}1`).merge();
  sheet.getRange("A1").values = [[title]];
  sheet.getRange("A1").format = {fill:"#1F4E78",font:{bold:true,color:"#FFFFFF",size:16},verticalAlignment:"center"};
  sheet.getRange(`A2:${lastCol}2`).merge();
  sheet.getRange("A2").values = [[subtitle]];
  sheet.getRange("A2").format = {fill:"#D9EAF7",font:{italic:true,color:"#1F1F1F"},wrapText:true};
  sheet.getRange("1:1").format.rowHeight = 28;
  sheet.getRange("2:2").format.rowHeight = 32;
  sheet.showGridLines = false;
}

titleBlock(jira,"SPRINT 2 – BACKLOG JIRA-READY","Cấu trúc Epic → Story → Sub-task. Thay Assignee Role bằng tên thành viên trước khi import; giữ Issue ID và Parent ID để Jira nối sub-task với story.","R");
jira.getRange("A4:R4").values = [headers];
jira.getRange(`A5:R${rows.length+4}`).values = rows;
jira.getRange("A4:R4").format = {fill:"#2F75B5",font:{bold:true,color:"#FFFFFF"},wrapText:true,verticalAlignment:"center",borders:{preset:"all",style:"thin",color:"#D9E2F3"}};
jira.getRange(`A5:R${rows.length+4}`).format = {wrapText:true,verticalAlignment:"top",borders:{preset:"all",style:"thin",color:"#D9E2F3"}};
jira.freezePanes.freezeRows(4); jira.freezePanes.freezeColumns(3);
const widths = [10,12,42,16,14,10,72,58,12,11,14,16,16,20,22,20,12,12];
widths.forEach((w,i)=>jira.getRangeByIndexes(0,i,rows.length+4,1).format.columnWidth=w);
jira.getRange(`A5:A${rows.length+4}`).format.numberFormat = "0";
jira.getRange(`J5:J${rows.length+4}`).format.numberFormat = "0";
jira.getRange(`B5:B${rows.length+4}`).dataValidation = {rule:{type:"list",values:["Epic","Story","Sub-task"]}};
jira.getRange(`I5:I${rows.length+4}`).dataValidation = {rule:{type:"list",values:["Highest","High","Medium","Low"]}};
jira.getRange(`R5:R${rows.length+4}`).dataValidation = {rule:{type:"list",values:["To Do","In Progress","Review","Testing","Done","Blocked"]}};

const taskRows = rows.filter(r=>r[1]==="Sub-task");
const boardHeaders = ["Story ID","Epic","Story / Nhóm việc","Ưu tiên","Số sub-task","Tổng SP","BE-1","BE-2","FE","QA","DB/DevOps","Trạng thái đề xuất"];
const boardRows = stories.map(s=>{
  const subs = taskRows.filter(r=>r[5]===issueIdByKey[s[0]]);
  const count = role => subs.filter(r=>r[11]===role).length;
  return [s[0],s[1],s[2],s[4],subs.length,subs.reduce((a,r)=>a+(Number(r[9])||0),0),count("BE-1"),count("BE-2"),count("FE"),count("QA"),count("DB/DevOps"),"Sprint 2"];
});
titleBlock(board,"TỔNG QUAN PHÂN RÃ CÔNG VIỆC","Dùng để cân tải trước Planning. Story Points là ước lượng ban đầu, nhóm cần hiệu chỉnh theo velocity thực tế.","L");
board.getRange("A4:L4").values=[boardHeaders]; board.getRange(`A5:L${boardRows.length+4}`).values=boardRows;
board.getRange("A4:L4").format={fill:"#2F75B5",font:{bold:true,color:"#FFFFFF"},wrapText:true,borders:{preset:"all",style:"thin",color:"#D9E2F3"}};
board.getRange(`A5:L${boardRows.length+4}`).format={wrapText:true,borders:{preset:"all",style:"thin",color:"#D9E2F3"},verticalAlignment:"top"};
board.freezePanes.freezeRows(4); [14,14,48,12,12,10,9,9,9,9,12,18].forEach((w,i)=>board.getRangeByIndexes(0,i,boardRows.length+4,1).format.columnWidth=w);

titleBlock(members,"BẢNG GÁN THÀNH VIÊN","Điền tên hoặc username Jira vào cột Thành viên thực tế, sau đó lọc Jira Import theo Assignee Role để giao ticket.","E");
const memberRows=[["BE-1","Backend tuyển dụng/chương trình","","Migration, tài khoản, hồ sơ, xét duyệt, email",""],["BE-2","Backend vận hành","","Hợp đồng, mentor, lịch, chấm công, báo cáo",""],["FE","Frontend","","JSP/CSS/JavaScript và trạng thái màn hình",""],["QA","Kiểm thử","","Test case, regression, security verification",""],["DB/DevOps","Dữ liệu và triển khai","","DB review, SMTP, upload path, môi trường",""],["Trưởng nhóm","Tích hợp và điều phối","","Theo dõi phụ thuộc, review, demo",""],["PO","Nghiệm thu","","Xác nhận acceptance criteria",""]];
members.getRange("A4:E4").values=[["Vai trò","Phạm vi","Thành viên thực tế","Trách nhiệm chính","Ghi chú tải"]]; members.getRange("A5:E11").values=memberRows;
members.getRange("A4:E4").format={fill:"#2F75B5",font:{bold:true,color:"#FFFFFF"},borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; members.getRange("A5:E11").format={wrapText:true,borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; [16,28,24,52,30].forEach((w,i)=>members.getRangeByIndexes(0,i,11,1).format.columnWidth=w);

titleBlock(guide,"HƯỚNG DẪN ĐƯA LÊN JIRA","Bảng dưới đây dùng cho Jira CSV importer. Tên trường có thể khác giữa Jira Cloud team-managed và company-managed.","D");
const guideRows=[
  ["1","Điền người nhận","Ở sheet Phân công, thay vai trò bằng username Jira; trong Jira Import thay cột Assignee Role nếu muốn import assignee trực tiếp.","Không để nhiều người trong một Assignee."],
  ["2","Xuất CSV","Mở sheet Jira Import và Save As CSV UTF-8.","Chỉ xuất đúng sheet Jira Import."],
  ["3","Map trường cơ bản","Issue Type, Summary, Description, Priority, Labels, Component/s, Sprint, Status.","Nếu workflow Jira không có Status tương ứng, bỏ map Status."],
  ["4","Map Epic","Map Epic Name cho dòng Epic; map Epic Link cho dòng Story nếu dự án company-managed.","Team-managed có thể dùng Parent thay Epic Link."],
  ["5","Map Sub-task","Map Issue ID và Parent ID. Parent ID của sub-task trỏ tới Issue ID của Story.","Bật tạo sub-task trong project trước khi import."],
  ["6","Map nội dung tùy chỉnh","Acceptance Criteria, Story Points, Original Estimate, Assignee Role, Reviewer Role, Dependencies.","Tạo custom field trước hoặc bỏ map các cột chưa có."],
  ["7","Import thử","Import 1 Epic + 1 Story + 2 sub-task vào project test trước.","Kiểm tra tiếng Việt, hierarchy và xuống dòng mô tả."],
];
guide.getRange("A4:D4").values=[["Bước","Thao tác","Cách làm","Lưu ý"]]; guide.getRange("A5:D11").values=guideRows;
guide.getRange("A4:D4").format={fill:"#2F75B5",font:{bold:true,color:"#FFFFFF"},borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; guide.getRange("A5:D11").format={wrapText:true,verticalAlignment:"top",borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; [8,24,72,48].forEach((w,i)=>guide.getRangeByIndexes(0,i,11,1).format.columnWidth=w);

titleBlock(checks,"DEFINITION OF READY / DEFINITION OF DONE","Dùng checklist này khi chuyển ticket vào Sprint và khi kéo sang Done.","C");
const checkRows=[
  ["DoR","Đã đọc phân tích nghiệp vụ và biết rõ vai trò, đầu vào, đầu ra, trạng thái và ngoại lệ.",""],
  ["DoR","Đã chốt schema, URL, request/response và phụ thuộc với ticket liên quan.",""],
  ["DoR","Ticket có một assignee chính, reviewer, ước lượng và tiêu chí nghiệm thu kiểm thử được.",""],
  ["DoD","Mã nguồn/cấu hình đã review; migration có rollback; không chứa secret.",""],
  ["DoD","Đã test luồng đúng, lỗi, phân quyền, gửi lặp/hai tab và dữ liệu biên phù hợp ticket.",""],
  ["DoD","Có bằng chứng test; không phá chức năng Sprint 1; tài liệu vận hành được cập nhật nếu cần.",""],
  ["DoD","PO hoặc người được ủy quyền xác nhận acceptance criteria.",""],
];
checks.getRange("A4:C4").values=[["Loại","Điều kiện","Xác nhận"]]; checks.getRange("A5:C11").values=checkRows;
checks.getRange("A4:C4").format={fill:"#2F75B5",font:{bold:true,color:"#FFFFFF"},borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; checks.getRange("A5:C11").format={wrapText:true,borders:{preset:"all",style:"thin",color:"#D9E2F3"}}; [12,92,20].forEach((w,i)=>checks.getRangeByIndexes(0,i,11,1).format.columnWidth=w);

for (const s of [jira,board,members,guide,checks]) s.getUsedRange().format.font = {name:"Times New Roman",size:11};

const outputPath = path.join(outDir, "Sprint_2_Jira_Backlog_Chi_Tiet.xlsx");
const blob = await SpreadsheetFile.exportXlsx(wb);
await blob.save(outputPath);

for (const sheet of ["Jira Import","Sprint Board","Phân công","Hướng dẫn import","DoR-DoD"]) {
  const preview = await wb.render({sheetName:sheet,autoCrop:"all",scale:0.9,format:"png"});
  await fs.writeFile(path.join(outDir, `preview-${sheet.replace(/[^a-zA-Z0-9]+/g,"-")}.png`), new Uint8Array(await preview.arrayBuffer()));
}
const inspection = await wb.inspect({kind:"workbook,sheet,table",maxChars:8000,tableMaxRows:5,tableMaxCols:8,tableMaxCellChars:100});
await fs.writeFile(path.join(outDir,"inspection.txt"), inspection.ndjson || String(inspection), "utf8");
console.log(JSON.stringify({outputPath, rowCount:rows.length, taskCount:taskRows.length, sheets:5}));
