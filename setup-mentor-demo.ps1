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
('intern_demo','123456','intern.demo@ims.local','Thực tập sinh Demo 01','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_02','123456','intern.demo02@ims.local','Thực tập sinh Demo 02','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_03','123456','intern.demo03@ims.local','Thực tập sinh Demo 03','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_04','123456','intern.demo04@ims.local','Thực tập sinh Demo 04','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_05','123456','intern.demo05@ims.local','Thực tập sinh Demo 05','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_06','123456','intern.demo06@ims.local','Thực tập sinh Demo 06','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_07','123456','intern.demo07@ims.local','Thực tập sinh Demo 07','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_08','123456','intern.demo08@ims.local','Thực tập sinh Demo 08','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_09','123456','intern.demo09@ims.local','Thực tập sinh Demo 09','ACTIVE',CURDATE(),CURDATE()),
('intern_demo_10','123456','intern.demo10@ims.local','Thực tập sinh Demo 10','ACTIVE',CURDATE(),CURDATE());

INSERT IGNORE INTO user_roles(user_id,role_id)
SELECT u.id,r.id FROM users u JOIN roles r
WHERE (u.username='mentor_demo' AND r.role_code='MENTOR')
   OR (u.username REGEXP '^intern_demo(_0[2-9]|_10)?$' AND r.role_code='INTERN');

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
       'Kỹ sư hướng dẫn',10,'ACTIVE'
WHERE NOT EXISTS(SELECT 1 FROM mentors WHERE user_id=@mentor_user_id OR mentor_code='MTR-DEMO');

UPDATE mentors
SET full_name='Mentor Demo',email='mentor.demo@ims.local',department_id=@department_id,
    job_title='Kỹ sư hướng dẫn',max_capacity=10,status='ACTIVE'
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

-- Bổ sung đủ 10 hồ sơ thực tập sinh cho mentor demo.
INSERT INTO intern_profiles
(user_id,intern_code,identity_number,date_of_birth,gender,address,university_name,major_name,
 program_id,mentor_id,start_date,end_date,phone_number,internship_status)
SELECT u.id,CONCAT('TTS-DEMO-',LPAD(SUBSTRING(u.username,13),2,'0')),
       CONCAT('00120000',LPAD(SUBSTRING(u.username,13),4,'0')),
       DATE_ADD('2003-01-01',INTERVAL CAST(SUBSTRING(u.username,13) AS UNSIGNED)*12 DAY),
       IF(MOD(CAST(SUBSTRING(u.username,13) AS UNSIGNED),2)=0,'Nữ','Nam'),'Hà Nội',
       IF(MOD(CAST(SUBSTRING(u.username,13) AS UNSIGNED),2)=0,'Đại học Kinh tế Quốc dân','Đại học Bách Khoa Hà Nội'),
       'Công nghệ thông tin',@program_id,@mentor_id,
       DATE_SUB(CURDATE(),INTERVAL 35 DAY),DATE_ADD(CURDATE(),INTERVAL 55 DAY),
       CONCAT('09123456',LPAD(SUBSTRING(u.username,13),2,'0')),'ACTIVE'
FROM users u
WHERE u.username REGEXP '^intern_demo_(0[2-9]|10)$'
  AND NOT EXISTS(SELECT 1 FROM intern_profiles ip WHERE ip.user_id=u.id);

DROP TEMPORARY TABLE IF EXISTS demo_numbers;
CREATE TEMPORARY TABLE demo_numbers(n INT PRIMARY KEY);
INSERT INTO demo_numbers(n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

-- Mỗi nhóm nghiệp vụ có 10 bản ghi, phân bổ trên 10 thực tập sinh.
INSERT INTO tasks(task_title,description,mentor_id,intern_profile_id,due_date,priority,completion_percent,status,intern_note,progress_updated_at)
SELECT CONCAT('Nhiệm vụ demo ',LPAD(d.n,2,'0')),
       CONCAT('Nội dung nhiệm vụ demo số ',d.n,' dùng để trình diễn theo dõi tiến độ.'),
       @mentor_id,ip.id,DATE_ADD(NOW(),INTERVAL d.n DAY),
       ELT(MOD(d.n-1,4)+1,'LOW','MEDIUM','HIGH','URGENT'),
       ELT(MOD(d.n-1,4)+1,0,35,70,100),
       ELT(MOD(d.n-1,4)+1,'TODO','IN_PROGRESS','IN_PROGRESS','COMPLETED'),
       CONCAT('Cập nhật tiến độ cho nhiệm vụ demo ',d.n,'.'),NOW()
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE NOT EXISTS(SELECT 1 FROM tasks t WHERE t.mentor_id=@mentor_id AND t.task_title=CONCAT('Nhiệm vụ demo ',LPAD(d.n,2,'0')));

-- Báo cáo tuần: có bản chờ duyệt và bản đã phản hồi.
INSERT INTO weekly_reports(intern_profile_id,week_number,start_date,end_date,completed_work,key_learnings,obstacles,attachment_url,mentor_score,mentor_feedback,status)
SELECT ip.id,d.n,DATE_SUB(CURDATE(),INTERVAL (11-d.n)*7 DAY),DATE_SUB(CURDATE(),INTERVAL ((11-d.n)*7-6) DAY),
       CONCAT('Hoàn thành công việc demo tuần ',d.n,'.'),'Cải thiện kỹ năng Java, SQL và làm việc nhóm.',
       IF(MOD(d.n,3)=0,'Cần mentor góp ý thêm về cách tổ chức mã nguồn.','Không có trở ngại lớn.'),NULL,
       IF(d.n<=6,ROUND(3.5+d.n/10,1),NULL),IF(d.n<=6,'Tiến độ tốt, tiếp tục phát huy.',NULL),
       IF(d.n<=6,'APPROVED','SUBMITTED')
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE completed_work=VALUES(completed_work),key_learnings=VALUES(key_learnings),
obstacles=VALUES(obstacles),mentor_score=VALUES(mentor_score),mentor_feedback=VALUES(mentor_feedback),status=VALUES(status);

-- Bản nháp đánh giá tổng kết để mentor có thể tiếp tục chỉnh sửa và chốt.
INSERT INTO mentor_final_evaluations
(intern_profile_id,mentor_id,status,knowledge_score,problem_solving_score,initiative_score,teamwork_score,
 skill_score,attitude_score,overall_score,overall_comments,revision_no,finalization_count)
SELECT ip.id,@mentor_id,IF(d.n<=3,'FINALIZED','DRAFT'),4,4,IF(MOD(d.n,2)=0,5,4),4,
       4.0,4.5,4.3,'Thực tập sinh có tiến bộ tốt, chủ động và phối hợp hiệu quả với nhóm.',1,IF(d.n<=3,1,0)
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE mentor_id=VALUES(mentor_id),status=VALUES(status);

-- Lịch họp sắp tới.
INSERT INTO meetings(title,meeting_date,start_time,end_time,meeting_mode,location_or_link,description,creator_user_id,status,version_no)
SELECT CONCAT('Họp demo ',LPAD(d.n,2,'0')),DATE_ADD(CURDATE(),INTERVAL d.n DAY),
       ADDTIME('08:00:00',SEC_TO_TIME((d.n-1)*3600)),ADDTIME('08:45:00',SEC_TO_TIME((d.n-1)*3600)),
       IF(MOD(d.n,2)=0,'OFFLINE','ONLINE'),IF(MOD(d.n,2)=0,'Phòng họp IT','https://meet.google.com/demo-mentor'),
       CONCAT('Trao đổi kết quả và kế hoạch cho thực tập sinh demo ',d.n,'.'),@mentor_user_id,'ACTIVE',1
FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM meetings m WHERE m.title=CONCAT('Họp demo ',LPAD(d.n,2,'0')) AND m.creator_user_id=@mentor_user_id);

INSERT INTO meeting_recipients(meeting_id,intern_profile_id,recipient_name,recipient_email,is_active,last_email_status,last_email_type)
SELECT m.id,ip.id,u.full_name,u.email,TRUE,'PENDING','INVITATION'
FROM demo_numbers d
JOIN meetings m ON m.title=CONCAT('Họp demo ',LPAD(d.n,2,'0')) AND m.creator_user_id=@mentor_user_id
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
JOIN users u ON u.id=ip.user_id
WHERE TRUE
ON DUPLICATE KEY UPDATE is_active=TRUE,recipient_name=VALUES(recipient_name),recipient_email=VALUES(recipient_email);

-- Thông báo mẫu trên bảng điều khiển mentor.
INSERT INTO notifications(user_id,title,message,target_url,is_read)
SELECT @mentor_user_id,CONCAT('Thông báo demo ',LPAD(d.n,2,'0')),
       CONCAT('Thực tập sinh Demo ',LPAD(d.n,2,'0'),' đã cập nhật tiến độ.'),
       IF(MOD(d.n,2)=0,'/mentor/tasks','/mentor/reports'),d.n<=3
FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM notifications n WHERE n.user_id=@mentor_user_id AND n.title=CONCAT('Thông báo demo ',LPAD(d.n,2,'0')));

DROP TEMPORARY TABLE demo_numbers;

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
