[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Get-DatabaseSetting {
    param([string]$Name, $Config)
    $value = [Environment]::GetEnvironmentVariable($Name)
    if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
    $property = $Config.PSObject.Properties[$Name]
    if ($null -ne $property -and -not [string]::IsNullOrWhiteSpace([string]$property.Value)) {
        return [string]$property.Value
    }
    throw "Thiếu cấu hình $Name. Hãy khai báo biến môi trường hoặc tạo src/main/resources/db-config.json."
}

function Find-MySqlClient {
    $command = Get-Command mysql -ErrorAction SilentlyContinue
    if ($null -ne $command) { return $command.Source }
    foreach ($candidate in @('C:\xampp\mysql\bin\mysql.exe','C:\laragon\bin\mysql\mysql.exe')) {
        if (Test-Path -LiteralPath $candidate -PathType Leaf) { return $candidate }
    }
    $installed = Get-ChildItem -Path 'C:\Program Files\MySQL\MySQL Server *\bin\mysql.exe' `
        -File -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | Select-Object -First 1
    if ($null -ne $installed) { return $installed.FullName }
    return $null
}

function Invoke-MySqlScript {
    param([string]$Executable, [string]$HostName, [string]$Port, [string]$UserName, [string]$ScriptPath)
    if ($HostName -notmatch '^[A-Za-z0-9.-]+$' -or $Port -notmatch '^\d+$' -or $UserName -notmatch '^[A-Za-z0-9_.-]+$') {
        throw 'Thông tin host, port hoặc tài khoản MySQL chứa ký tự không hợp lệ.'
    }
    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $Executable
    $startInfo.Arguments = "--host=$HostName --port=$Port --user=$UserName --default-character-set=utf8mb4"
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    [void]$process.Start()
    $sqlBytes = [Text.Encoding]::UTF8.GetBytes([IO.File]::ReadAllText($ScriptPath, [Text.Encoding]::UTF8))
    $process.StandardInput.BaseStream.Write($sqlBytes, 0, $sqlBytes.Length)
    $process.StandardInput.BaseStream.Close()
    $process.WaitForExit()
    return $process.ExitCode
}

$configPath = Join-Path $PSScriptRoot 'src\main\resources\db-config.json'
$config = if (Test-Path -LiteralPath $configPath -PathType Leaf) {
    Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json
} else { [pscustomobject]@{} }

$jdbcUrl = Get-DatabaseSetting 'IMS_DB_URL' $config
$databaseUser = Get-DatabaseSetting 'IMS_DB_USER' $config
$databasePassword = Get-DatabaseSetting 'IMS_DB_PASSWORD' $config
$urlMatch = [regex]::Match($jdbcUrl, '^jdbc:mysql://(?<host>[^:/?]+)(?::(?<port>\d+))?/(?<database>[^?]+)')
if (-not $urlMatch.Success) { throw "IMS_DB_URL không đúng định dạng JDBC MySQL: $jdbcUrl" }

$databaseHost = $urlMatch.Groups['host'].Value
$databasePort = if ($urlMatch.Groups['port'].Success) { $urlMatch.Groups['port'].Value } else { '3306' }
$databaseName = $urlMatch.Groups['database'].Value
if ($databaseName -ne 'internship_management') {
    throw "Script demo yêu cầu cơ sở dữ liệu internship_management, hiện tại là $databaseName."
}

$mysqlPath = Find-MySqlClient
if ([string]::IsNullOrWhiteSpace($mysqlPath)) {
    throw 'Không tìm thấy lệnh mysql. Hãy cài MySQL 8 Client và thêm thư mục bin của MySQL vào PATH.'
}

$demoSql = @'
USE internship_management;

-- Tài khoản và vai trò dùng cho buổi demo.
INSERT IGNORE INTO users(username,password,email,full_name,status,create_at,update_at) VALUES
('mentor_demo','123456','mentor.demo@ims.local','Mentor Demo','ACTIVE',CURDATE(),CURDATE()),
('intern_demo','123456','intern.demo@ims.local','Thực tập sinh Demo','ACTIVE',CURDATE(),CURDATE());

INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r
WHERE (u.username='mentor_demo' AND r.role_code='MENTOR')
   OR (u.username='intern_demo' AND r.role_code='INTERN');

INSERT IGNORE INTO departments(dept_code,dept_name,is_active)
VALUES('IT','Công nghệ thông tin',TRUE);

SET @department_id=(SELECT id FROM departments WHERE dept_code='IT' LIMIT 1);
INSERT IGNORE INTO internship_programs
(program_code,program_name,department_id,quota_count,start_date,end_date,description,status)
VALUES('DEMO-MENTOR','Chương trình thực tập Demo Mentor',@department_id,10,
       DATE_SUB(CURDATE(),INTERVAL 35 DAY),DATE_ADD(CURDATE(),INTERVAL 55 DAY),
       'Dữ liệu được tạo tự động để trình diễn các nghiệp vụ của mentor.','ACTIVE');

UPDATE internship_programs
SET program_name='Chương trình thực tập Demo Mentor',department_id=@department_id,
    start_date=DATE_SUB(CURDATE(),INTERVAL 35 DAY),end_date=DATE_ADD(CURDATE(),INTERVAL 55 DAY),
    description='Dữ liệu được tạo tự động để trình diễn các nghiệp vụ của mentor.',status='ACTIVE'
WHERE program_code='DEMO-MENTOR';

SET @mentor_user_id=(SELECT id FROM users WHERE username='mentor_demo' LIMIT 1);
INSERT INTO mentors(user_id,full_name,email,mentor_code,department_id,job_title,max_capacity,status)
SELECT @mentor_user_id,'Mentor Demo','mentor.demo@ims.local','MTR-DEMO',@department_id,
       'Kỹ sư hướng dẫn',5,'ACTIVE'
WHERE NOT EXISTS(SELECT 1 FROM mentors WHERE user_id=@mentor_user_id OR mentor_code='MTR-DEMO');

UPDATE mentors
SET full_name='Mentor Demo',email='mentor.demo@ims.local',department_id=@department_id,
    job_title='Kỹ sư hướng dẫn',max_capacity=5,status='ACTIVE'
WHERE user_id=@mentor_user_id OR mentor_code='MTR-DEMO';

SET @mentor_id=(SELECT id FROM mentors WHERE user_id=@mentor_user_id OR mentor_code='MTR-DEMO' ORDER BY id LIMIT 1);
SET @intern_user_id=(SELECT id FROM users WHERE username='intern_demo' LIMIT 1);
SET @program_id=(SELECT id FROM internship_programs WHERE program_code='DEMO-MENTOR' LIMIT 1);

INSERT INTO intern_profiles
(user_id,intern_code,identity_number,date_of_birth,gender,address,university_name,major_name,
 program_id,mentor_id,start_date,end_date,phone_number,internship_status)
SELECT @intern_user_id,'TTS-DEMO','001200009999','2003-05-15','Nam','Hà Nội',
       'Đại học Bách Khoa Hà Nội','Công nghệ thông tin',@program_id,@mentor_id,
       DATE_SUB(CURDATE(),INTERVAL 35 DAY),DATE_ADD(CURDATE(),INTERVAL 55 DAY),'0912345678','ACTIVE'
WHERE NOT EXISTS(SELECT 1 FROM intern_profiles WHERE user_id=@intern_user_id);

UPDATE intern_profiles
SET program_id=@program_id,mentor_id=@mentor_id,start_date=DATE_SUB(CURDATE(),INTERVAL 35 DAY),
    end_date=DATE_ADD(CURDATE(),INTERVAL 55 DAY),internship_status='ACTIVE'
WHERE user_id=@intern_user_id;

SET @intern_id=(SELECT id FROM intern_profiles WHERE user_id=@intern_user_id LIMIT 1);

-- Nhiệm vụ ở nhiều trạng thái để trình diễn theo dõi tiến độ.
INSERT INTO tasks(task_title,description,mentor_id,intern_profile_id,due_date,priority,completion_percent,status,intern_note,progress_updated_at)
SELECT 'Xây dựng API quản lý thực tập sinh','Hoàn thiện API danh sách, tìm kiếm và cập nhật hồ sơ thực tập sinh.',@mentor_id,@intern_id,DATE_ADD(NOW(),INTERVAL 7 DAY),'HIGH',65,'IN_PROGRESS','Đã hoàn thiện API danh sách và đang xử lý phần cập nhật.',NOW()
WHERE NOT EXISTS(SELECT 1 FROM tasks WHERE intern_profile_id=@intern_id AND task_title='Xây dựng API quản lý thực tập sinh');

INSERT INTO tasks(task_title,description,mentor_id,intern_profile_id,due_date,priority,completion_percent,status,intern_note,progress_updated_at)
SELECT 'Viết tài liệu hướng dẫn sử dụng','Tổng hợp quy trình nghiệp vụ và ảnh minh họa cho tài liệu bàn giao.',@mentor_id,@intern_id,DATE_ADD(NOW(),INTERVAL 12 DAY),'MEDIUM',25,'IN_PROGRESS','Đã lập dàn ý tài liệu.',NOW()
WHERE NOT EXISTS(SELECT 1 FROM tasks WHERE intern_profile_id=@intern_id AND task_title='Viết tài liệu hướng dẫn sử dụng');

INSERT INTO tasks(task_title,description,mentor_id,intern_profile_id,due_date,priority,completion_percent,status,intern_note,progress_updated_at)
SELECT 'Kiểm thử luồng đăng nhập và phân quyền','Kiểm thử các vai trò quản trị viên, nhân sự, mentor và thực tập sinh.',@mentor_id,@intern_id,DATE_SUB(NOW(),INTERVAL 2 DAY),'MEDIUM',100,'COMPLETED','Đã hoàn thành kiểm thử và ghi nhận kết quả.',NOW()
WHERE NOT EXISTS(SELECT 1 FROM tasks WHERE intern_profile_id=@intern_id AND task_title='Kiểm thử luồng đăng nhập và phân quyền');

-- Báo cáo tuần: có bản chờ duyệt và bản đã phản hồi.
INSERT INTO weekly_reports(intern_profile_id,week_number,start_date,end_date,completed_work,key_learnings,obstacles,attachment_url,mentor_score,mentor_feedback,status)
VALUES
(@intern_id,1,DATE_SUB(CURDATE(),INTERVAL 28 DAY),DATE_SUB(CURDATE(),INTERVAL 22 DAY),'Tìm hiểu quy trình dự án và cấu trúc hệ thống.','Nắm được Servlet, JSP và mô hình DAO.','Cần làm quen thêm với cơ sở dữ liệu.',NULL,4.2,'Khởi đầu tốt, cần chủ động đặt câu hỏi hơn.','APPROVED'),
(@intern_id,2,DATE_SUB(CURDATE(),INTERVAL 21 DAY),DATE_SUB(CURDATE(),INTERVAL 15 DAY),'Hoàn thành giao diện danh sách nhiệm vụ và tích hợp dữ liệu.','Cải thiện kỹ năng làm việc với Git và xử lý giao diện.','Một số trường hợp kiểm thử chưa đầy đủ.',NULL,4.5,'Tiến độ tốt, bổ sung thêm kiểm thử biên.','APPROVED'),
(@intern_id,3,DATE_SUB(CURDATE(),INTERVAL 14 DAY),DATE_SUB(CURDATE(),INTERVAL 8 DAY),'Xây dựng API quản lý thực tập sinh và cập nhật tài liệu.','Hiểu rõ hơn về phân quyền và kiểm tra dữ liệu đầu vào.','Cần mentor góp ý cách tổ chức mã nguồn.',NULL,NULL,NULL,'SUBMITTED')
ON DUPLICATE KEY UPDATE completed_work=VALUES(completed_work),key_learnings=VALUES(key_learnings),
obstacles=VALUES(obstacles),mentor_score=VALUES(mentor_score),mentor_feedback=VALUES(mentor_feedback),status=VALUES(status);

-- Bản nháp đánh giá tổng kết để mentor có thể tiếp tục chỉnh sửa và chốt.
INSERT INTO mentor_final_evaluations
(intern_profile_id,mentor_id,status,knowledge_score,problem_solving_score,initiative_score,teamwork_score,
 skill_score,attitude_score,overall_score,overall_comments,revision_no,finalization_count)
VALUES(@intern_id,@mentor_id,'DRAFT',4,4,5,4,4.0,4.5,4.3,
       'Thực tập sinh có tiến bộ tốt, chủ động và phối hợp hiệu quả với nhóm.',1,0)
ON DUPLICATE KEY UPDATE mentor_id=VALUES(mentor_id);

-- Lịch họp sắp tới.
INSERT INTO meetings(title,meeting_date,start_time,end_time,meeting_mode,location_or_link,description,creator_user_id,status,version_no)
SELECT 'Họp đánh giá tiến độ tuần',DATE_ADD(CURDATE(),INTERVAL 2 DAY),'09:00:00','09:45:00','ONLINE',
       'https://meet.google.com/demo-mentor','Trao đổi kết quả tuần và thống nhất nhiệm vụ tiếp theo.',@mentor_user_id,'ACTIVE',1
WHERE NOT EXISTS(SELECT 1 FROM meetings WHERE title='Họp đánh giá tiến độ tuần' AND creator_user_id=@mentor_user_id AND status='ACTIVE');

SET @meeting_id=(SELECT id FROM meetings WHERE title='Họp đánh giá tiến độ tuần' AND creator_user_id=@mentor_user_id AND status='ACTIVE' ORDER BY id DESC LIMIT 1);
INSERT INTO meeting_recipients(meeting_id,intern_profile_id,recipient_name,recipient_email,is_active,last_email_status,last_email_type)
VALUES(@meeting_id,@intern_id,'Thực tập sinh Demo','intern.demo@ims.local',TRUE,'PENDING','INVITATION')
ON DUPLICATE KEY UPDATE is_active=TRUE,recipient_name=VALUES(recipient_name),recipient_email=VALUES(recipient_email);

-- Thông báo mẫu trên bảng điều khiển mentor.
INSERT INTO notifications(user_id,title,message,target_url,is_read)
SELECT @mentor_user_id,'Báo cáo tuần mới','Thực tập sinh Demo đã nộp báo cáo tuần 3.','/mentor/reports',FALSE
WHERE NOT EXISTS(SELECT 1 FROM notifications WHERE user_id=@mentor_user_id AND title='Báo cáo tuần mới' AND message LIKE '%tuần 3%');

SELECT 'MENTOR_DEMO_READY' AS result,@mentor_id AS mentor_id,@intern_id AS intern_profile_id;
'@

$tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$tempSqlPath = Join-Path $tempRoot ("ims-mentor-demo-{0}.sql" -f [guid]::NewGuid().ToString('N'))
$previousMysqlPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')

try {
    [IO.File]::WriteAllText($tempSqlPath, $demoSql, [Text.UTF8Encoding]::new($false))
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $databasePassword, 'Process')
    Write-Host 'Đang chuẩn bị dữ liệu demo cho Mentor...' -ForegroundColor Cyan

    $exitCode = Invoke-MySqlScript $mysqlPath $databaseHost $databasePort $databaseUser $tempSqlPath
    if ($exitCode -ne 0) {
        throw "Nạp dữ liệu demo thất bại (mã lỗi $exitCode). Hãy chạy setup-database.ps1 trước."
    }

    Write-Host 'Dữ liệu demo Mentor đã sẵn sàng.' -ForegroundColor Green
    Write-Host 'Đăng nhập: mentor_demo / 123456'
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousMysqlPassword, 'Process')
    if (Test-Path -LiteralPath $tempSqlPath -PathType Leaf) {
        $resolvedTemp = (Resolve-Path -LiteralPath $tempSqlPath).Path
        if ($resolvedTemp.StartsWith($tempRoot)) { Remove-Item -LiteralPath $resolvedTemp -Force }
    }
}
