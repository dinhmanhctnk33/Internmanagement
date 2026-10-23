function Get-DemoDatabaseSetting {
    param([string]$Name, $Config)
    $value = [Environment]::GetEnvironmentVariable($Name)
    if (-not [string]::IsNullOrWhiteSpace($value)) { return $value }
    $property = $Config.PSObject.Properties[$Name]
    if ($null -ne $property -and -not [string]::IsNullOrWhiteSpace([string]$property.Value)) {
        return [string]$property.Value
    }
    throw "Missing $Name. Configure the environment variable or src/main/resources/db-config.json."
}

function Find-DemoMySqlClient {
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

function Invoke-DemoSql {
    param([Parameter(Mandatory)][string]$Sql, [Parameter(Mandatory)][string]$Label)
    $configPath = Join-Path $PSScriptRoot 'src\main\resources\db-config.json'
    $config = if (Test-Path -LiteralPath $configPath -PathType Leaf) {
        Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json
    } else { [pscustomobject]@{} }

    $jdbcUrl = Get-DemoDatabaseSetting 'IMS_DB_URL' $config
    $databaseUser = Get-DemoDatabaseSetting 'IMS_DB_USER' $config
    $databasePassword = Get-DemoDatabaseSetting 'IMS_DB_PASSWORD' $config
    $urlMatch = [regex]::Match($jdbcUrl, '^jdbc:mysql://(?<host>[^:/?]+)(?::(?<port>\d+))?/(?<database>[^?]+)')
    if (-not $urlMatch.Success -or $urlMatch.Groups['database'].Value -ne 'internship_management') {
        throw "Invalid demo database URL: $jdbcUrl"
    }
    $mysqlPath = Find-DemoMySqlClient
    if ([string]::IsNullOrWhiteSpace($mysqlPath)) { throw 'MySQL 8 Client was not found.' }

    $tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
    $tempSqlPath = Join-Path $tempRoot ("ims-demo-{0}.sql" -f [guid]::NewGuid().ToString('N'))
    $previousPassword = [Environment]::GetEnvironmentVariable('MYSQL_PWD', 'Process')
    try {
        [IO.File]::WriteAllText($tempSqlPath, $Sql, [Text.UTF8Encoding]::new($false))
        [Environment]::SetEnvironmentVariable('MYSQL_PWD', $databasePassword, 'Process')
        $startInfo = [Diagnostics.ProcessStartInfo]::new()
        $startInfo.FileName = $mysqlPath
        $port = if ($urlMatch.Groups['port'].Success) { $urlMatch.Groups['port'].Value } else { '3306' }
        $startInfo.Arguments = "--host=$($urlMatch.Groups['host'].Value) --port=$port --user=$databaseUser --default-character-set=utf8mb4"
        $startInfo.UseShellExecute = $false
        $startInfo.RedirectStandardInput = $true
        $process = [Diagnostics.Process]::new()
        $process.StartInfo = $startInfo
        [void]$process.Start()
        $bytes = [Text.Encoding]::UTF8.GetBytes($Sql)
        $process.StandardInput.BaseStream.Write($bytes, 0, $bytes.Length)
        $process.StandardInput.BaseStream.Close()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) { throw "$Label demo data failed (exit code $($process.ExitCode))." }
    } finally {
        [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousPassword, 'Process')
        if (Test-Path -LiteralPath $tempSqlPath -PathType Leaf) { Remove-Item -LiteralPath $tempSqlPath -Force }
    }
}
