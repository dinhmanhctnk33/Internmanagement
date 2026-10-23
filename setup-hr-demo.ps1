[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

# Dùng chung 10 hồ sơ thực tập sinh, mentor, chương trình và phòng ban demo.
& (Join-Path $PSScriptRoot 'setup-mentor-demo.ps1')

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

SET @hr_user_id=(SELECT id FROM users WHERE username='hr_demo' LIMIT 1);
SET @program_id=(SELECT id FROM internship_programs WHERE program_code='DEMO-MENTOR' LIMIT 1);

DROP TEMPORARY TABLE IF EXISTS demo_numbers;
CREATE TEMPORARY TABLE demo_numbers(n INT PRIMARY KEY);
INSERT INTO demo_numbers(n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

-- 10 ứng viên ở nhiều trạng thái để HR xét duyệt.
INSERT INTO candidates(full_name,email,phone,university_name,major_name,desired_position,cv_file_url,application_status,hr_notes,applied_at)
SELECT CONCAT('Ứng viên Demo ',LPAD(d.n,2,'0')),CONCAT('candidate.demo',LPAD(d.n,2,'0'),'@ims.local'),
       CONCAT('09012345',LPAD(d.n,2,'0')),
       IF(MOD(d.n,2)=0,'Đại học Kinh tế Quốc dân','Đại học Bách Khoa Hà Nội'),
       'Công nghệ thông tin',ELT(MOD(d.n-1,3)+1,'Backend Intern','Frontend Intern','QA Intern'),
       CONCAT('/uploads/demo/cv-',LPAD(d.n,2,'0'),'.pdf'),
       ELT(MOD(d.n-1,4)+1,'NEW','INTERVIEWING','ACCEPTED','REJECTED'),
       CONCAT('Ghi chú xét duyệt demo ',d.n,'.'),DATE_SUB(NOW(),INTERVAL d.n DAY)
FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM candidates c WHERE c.email=CONCAT('candidate.demo',LPAD(d.n,2,'0'),'@ims.local'));

-- 10 tài liệu chờ duyệt/đã duyệt/bị từ chối.
INSERT INTO intern_documents(intern_profile_id,document_type,file_name,file_url,file_size_bytes,approval_status,rejection_note,uploaded_at)
SELECT ip.id,ELT(MOD(d.n-1,3)+1,'CV','STUDENT_CARD','TRANSCRIPT'),
       CONCAT('tai-lieu-hr-demo-',LPAD(d.n,2,'0'),'.pdf'),CONCAT('/uploads/demo/hr-document-',LPAD(d.n,2,'0'),'.pdf'),
       500000+d.n*10000,ELT(MOD(d.n-1,3)+1,'PENDING','APPROVED','REJECTED'),
       IF(MOD(d.n-1,3)=2,'Tài liệu chưa rõ, vui lòng tải lại.',NULL),DATE_SUB(NOW(),INTERVAL d.n DAY)
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE NOT EXISTS(SELECT 1 FROM intern_documents doc WHERE doc.file_name=CONCAT('tai-lieu-hr-demo-',LPAD(d.n,2,'0'),'.pdf'));

-- 10 hợp đồng.
INSERT INTO contracts(contract_number,intern_profile_id,effective_date,expiration_date,monthly_allowance,contract_pdf_url,signature_status,signed_at)
SELECT CONCAT('HD-DEMO-',LPAD(d.n,2,'0')),ip.id,DATE_SUB(CURDATE(),INTERVAL 20 DAY),DATE_ADD(CURDATE(),INTERVAL 70 DAY),
       3000000+d.n*100000,CONCAT('/uploads/demo/contract-',LPAD(d.n,2,'0'),'.pdf'),
       ELT(MOD(d.n-1,4)+1,'DRAFT','SENT','SIGNED','EXPIRED'),IF(MOD(d.n-1,4)=2,NOW(),NULL)
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE intern_profile_id=VALUES(intern_profile_id),signature_status=VALUES(signature_status);

-- 10 bản ghi chấm công.
INSERT INTO attendance_records(intern_profile_id,work_date,check_in_time,check_out_time,ip_address,status)
SELECT ip.id,DATE_SUB(CURDATE(),INTERVAL d.n DAY),
       TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL d.n DAY),IF(MOD(d.n,3)=0,'08:20:00','07:55:00')),
       TIMESTAMP(DATE_SUB(CURDATE(),INTERVAL d.n DAY),'17:00:00'),'127.0.0.1',
       IF(MOD(d.n,3)=0,'LATE','ON_TIME')
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE check_in_time=VALUES(check_in_time),check_out_time=VALUES(check_out_time),status=VALUES(status);

-- 10 đơn nghỉ phép.
INSERT INTO leave_requests(intern_profile_id,leave_type,start_date,end_date,total_days,reason,approval_status,approver_notes)
SELECT ip.id,ELT(MOD(d.n-1,3)+1,'SICK_LEAVE','PERSONAL_LEAVE','UNPAID_LEAVE'),
       DATE_ADD(CURDATE(),INTERVAL d.n DAY),DATE_ADD(CURDATE(),INTERVAL d.n DAY),1.0,
       CONCAT('[HR-DEMO-',LPAD(d.n,2,'0'),'] Lý do nghỉ phép mẫu.'),
       ELT(MOD(d.n-1,3)+1,'PENDING','APPROVED','REJECTED'),IF(MOD(d.n-1,3)=0,NULL,'Phản hồi mẫu từ HR.')
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE NOT EXISTS(SELECT 1 FROM leave_requests lr WHERE lr.reason LIKE CONCAT('[HR-DEMO-',LPAD(d.n,2,'0'),']%'));

-- 10 phiếu phụ cấp.
INSERT INTO allowance_payouts(intern_profile_id,pay_period,actual_work_days,base_allowance,bonus_allowance,net_amount,payment_status,paid_at)
SELECT ip.id,DATE_FORMAT(CURDATE(),'%Y-%m'),20+d.n%3,3000000,d.n*50000,3000000+d.n*50000,
       ELT(MOD(d.n-1,4)+1,'PENDING','PROCESSING','PAID','FAILED'),IF(MOD(d.n-1,4)=2,NOW(),NULL)
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE actual_work_days=VALUES(actual_work_days),net_amount=VALUES(net_amount),payment_status=VALUES(payment_status);

-- 10 yêu cầu hỗ trợ.
INSERT INTO support_tickets(ticket_code,intern_profile_id,ticket_category,subject,description,hr_response,status)
SELECT CONCAT('TK-DEMO-',LPAD(d.n,2,'0')),ip.id,
       ELT(MOD(d.n-1,3)+1,'FACILITY','DOCUMENT','ALLOWANCE'),CONCAT('Yêu cầu hỗ trợ demo ',LPAD(d.n,2,'0')),
       'Nội dung yêu cầu hỗ trợ dùng cho buổi demo HR.',IF(d.n<=5,'HR đã tiếp nhận yêu cầu.',NULL),
       ELT(MOD(d.n-1,4)+1,'OPEN','IN_PROGRESS','RESOLVED','CLOSED')
FROM demo_numbers d
JOIN intern_profiles ip ON ip.intern_code=IF(d.n=1,'TTS-DEMO',CONCAT('TTS-DEMO-',LPAD(d.n,2,'0')))
WHERE TRUE
ON DUPLICATE KEY UPDATE status=VALUES(status),hr_response=VALUES(hr_response);

-- 10 thông báo trên dashboard HR.
INSERT INTO notifications(user_id,title,message,target_url,is_read)
SELECT @hr_user_id,CONCAT('Thông báo HR demo ',LPAD(d.n,2,'0')),
       CONCAT('Có cập nhật nghiệp vụ HR số ',d.n,'.'),
       ELT(MOD(d.n-1,4)+1,'/applications','/documents/review','/leave-approvals','/contracts'),d.n<=3
FROM demo_numbers d
WHERE NOT EXISTS(SELECT 1 FROM notifications n WHERE n.user_id=@hr_user_id AND n.title=CONCAT('Thông báo HR demo ',LPAD(d.n,2,'0')));

DROP TEMPORARY TABLE demo_numbers;
SELECT 'HR_DEMO_READY' AS result,@hr_user_id AS hr_user_id;
'@

$tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$tempSqlPath = Join-Path $tempRoot ("ims-hr-demo-{0}.sql" -f [guid]::NewGuid().ToString('N'))
$previousMysqlPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')

try {
    [IO.File]::WriteAllText($tempSqlPath, $demoSql, [Text.UTF8Encoding]::new($false))
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $databasePassword, 'Process')
    Write-Host 'Dang chuan bi du lieu demo cho HR...' -ForegroundColor Cyan
    $exitCode = Invoke-MySqlScript $mysqlPath $databaseHost $databasePort $databaseUser $tempSqlPath
    if ($exitCode -ne 0) {
        throw "Nap du lieu demo HR that bai (ma loi $exitCode). Hay chay setup-database.ps1 truoc."
    }
    Write-Host 'Du lieu demo HR da san sang.' -ForegroundColor Green
    Write-Host 'Dang nhap: hr_demo / 123456'
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousMysqlPassword, 'Process')
    if (Test-Path -LiteralPath $tempSqlPath -PathType Leaf) {
        $resolvedTemp = (Resolve-Path -LiteralPath $tempSqlPath).Path
        if ($resolvedTemp.StartsWith($tempRoot)) { Remove-Item -LiteralPath $resolvedTemp -Force }
    }
}
