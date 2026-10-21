[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

function Get-DatabaseSetting {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)]$Config
    )

    $environmentValue = [Environment]::GetEnvironmentVariable($Name)
    if (-not [string]::IsNullOrWhiteSpace($environmentValue)) {
        return $environmentValue
    }

    $property = $Config.PSObject.Properties[$Name]
    if ($null -ne $property -and -not [string]::IsNullOrWhiteSpace([string]$property.Value)) {
        return [string]$property.Value
    }

    throw "Thiếu cấu hình $Name. Hãy khai báo biến môi trường hoặc tạo src/main/resources/db-config.json."
}

function Find-MySqlClient {
    $command = Get-Command mysql -ErrorAction SilentlyContinue
    if ($null -ne $command) { return $command.Source }

    $fixedCandidates = @(
        'C:\xampp\mysql\bin\mysql.exe',
        'C:\laragon\bin\mysql\mysql.exe'
    )
    foreach ($candidate in $fixedCandidates) {
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

$repositoryRoot = $PSScriptRoot
$sqlPath = Join-Path $repositoryRoot 'src\main\resources\database.sql'
$configPath = Join-Path $repositoryRoot 'src\main\resources\db-config.json'
$exampleConfigPath = Join-Path $repositoryRoot 'src\main\resources\db-config.example.json'

if (-not (Test-Path -LiteralPath $sqlPath -PathType Leaf)) {
    throw "Không tìm thấy file dữ liệu: $sqlPath"
}

$config = [pscustomobject]@{}
if (Test-Path -LiteralPath $configPath -PathType Leaf) {
    $config = Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json
} elseif (Test-Path -LiteralPath $exampleConfigPath -PathType Leaf) {
    Write-Host "Chưa có db-config.json; script sẽ ưu tiên các biến môi trường IMS_DB_* nếu đã khai báo." -ForegroundColor Yellow
}

$jdbcUrl = Get-DatabaseSetting -Name 'IMS_DB_URL' -Config $config
$databaseUser = Get-DatabaseSetting -Name 'IMS_DB_USER' -Config $config
$databasePassword = Get-DatabaseSetting -Name 'IMS_DB_PASSWORD' -Config $config

if ($databasePassword -eq 'your_mysql_password') {
    throw 'Mật khẩu MySQL vẫn đang là giá trị mẫu. Hãy cập nhật cấu hình trước khi chạy.'
}

$urlMatch = [regex]::Match($jdbcUrl, '^jdbc:mysql://(?<host>[^:/?]+)(?::(?<port>\d+))?/(?<database>[^?]+)')
if (-not $urlMatch.Success) {
    throw "IMS_DB_URL không đúng định dạng JDBC MySQL: $jdbcUrl"
}

$databaseHost = $urlMatch.Groups['host'].Value
$databasePort = if ($urlMatch.Groups['port'].Success) { $urlMatch.Groups['port'].Value } else { '3306' }
$databaseName = $urlMatch.Groups['database'].Value

if ($databaseName -notmatch '^[A-Za-z0-9_]+$') {
    throw "Tên cơ sở dữ liệu không hợp lệ: $databaseName"
}

if ($databaseName -ne 'internship_management') {
    throw "database.sql khởi tạo cơ sở dữ liệu internship_management nhưng IMS_DB_URL đang trỏ tới $databaseName."
}

$mysqlPath = Find-MySqlClient
if ([string]::IsNullOrWhiteSpace($mysqlPath)) {
    throw 'Không tìm thấy lệnh mysql. Hãy cài MySQL 8 Client và thêm thư mục bin của MySQL vào PATH.'
}

$previousMysqlPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')
[Environment]::SetEnvironmentVariable('MYSQL_PWD', $databasePassword, 'Process')

try {
    Write-Host "Đang nạp cơ sở dữ liệu '$databaseName' tại ${databaseHost}:$databasePort..." -ForegroundColor Cyan

    $exitCode = Invoke-MySqlScript $mysqlPath $databaseHost $databasePort $databaseUser $sqlPath
    if ($exitCode -ne 0) {
        throw "Nạp dữ liệu thất bại (mã lỗi $exitCode). Hãy kiểm tra MySQL và cấu hình kết nối."
    }

    Write-Host 'Nạp dữ liệu thành công.' -ForegroundColor Green
    Write-Host 'Tài khoản mẫu: admin_demo, hr_demo, mentor_demo, intern_demo. Mật khẩu: 123456.'
} finally {
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousMysqlPassword, 'Process')
}
