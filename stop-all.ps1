# Ports for all services
$ports = @(8761, 8888, 8080, 8081, 8082, 8083, 8084, 8085, 8086)

Write-Host "Stopping all E-Commerce Microservices..." -ForegroundColor Cyan

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        foreach ($conn in $connections) {
            $pid = $conn.OwningProcess
            if ($pid) {
                $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($process) {
                    Write-Host "Stopping $($process.Name) (PID: $pid) on port $port..." -ForegroundColor Yellow
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                }
            }
        }
    }
}

# Also stop infrastructure if running
Write-Host "Checking for local databases/infrastructure to stop..." -ForegroundColor Cyan

# Stop PostgreSQL
$infraDir = "c:\e com\infra"
if (Test-Path "$infraDir\pgsql\bin\pg_ctl.exe") {
    Write-Host "Stopping PostgreSQL..." -ForegroundColor Yellow
    & "$infraDir\pgsql\bin\pg_ctl.exe" -D "$infraDir\postgres-data" stop -m fast 2>$null
}

# Stop MongoDB
$mongoProcess = Get-Process -Name "mongod" -ErrorAction SilentlyContinue
if ($mongoProcess) {
    Write-Host "Stopping MongoDB..." -ForegroundColor Yellow
    Stop-Process -Name "mongod" -Force -ErrorAction SilentlyContinue
}

# Stop Zookeeper/Kafka/Schema Registry
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
foreach ($jp in $javaProcesses) {
    # Check if this java process is kafka/zookeeper/schema-registry
    $cmd = Get-WmiObject Win32_Process -Filter "ProcessId = $($jp.Id)" | Select-Object -ExpandProperty CommandLine
    if ($cmd -like "*kafka*" -or $cmd -like "*zookeeper*" -or $cmd -like "*schema-registry*") {
        Write-Host "Stopping Java process (PID: $($jp.Id)) running $($jp.Description)..." -ForegroundColor Yellow
        Stop-Process -Id $jp.Id -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "All stopped!" -ForegroundColor Green
