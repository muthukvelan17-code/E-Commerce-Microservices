# ============================================================
# E-Commerce Microservices — Health Check Script
# Verifies all services are running and responding
# ============================================================

param (
    [switch]$Verbose
)

$services = @(
    @{ Name = "Eureka Server";       Port = 8761; Path = "/actuator/health" },
    @{ Name = "Config Server";       Port = 8888; Path = "/actuator/health" },
    @{ Name = "API Gateway";         Port = 8080; Path = "/actuator/health" },
    @{ Name = "User Service";        Port = 8081; Path = "/actuator/health" },
    @{ Name = "Product Service";     Port = 8082; Path = "/actuator/health" },
    @{ Name = "Inventory Service";   Port = 8083; Path = "/actuator/health" },
    @{ Name = "Order Service";       Port = 8084; Path = "/actuator/health" },
    @{ Name = "Payment Service";     Port = 8085; Path = "/actuator/health" },
    @{ Name = "Notification Service";Port = 8086; Path = "/actuator/health" }
)

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  E-Commerce Platform Health Check" -ForegroundColor Cyan
Write-Host "  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$healthy = 0
$unhealthy = 0

foreach ($svc in $services) {
    $url = "http://localhost:$($svc.Port)$($svc.Path)"
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        $statusCode = $response.StatusCode

        if ($statusCode -eq 200) {
            Write-Host "  [UP]   $($svc.Name) (port $($svc.Port))" -ForegroundColor Green
            $healthy++
            if ($Verbose) {
                $body = $response.Content | ConvertFrom-Json
                Write-Host "         Status: $($body.status)" -ForegroundColor DarkGray
            }
        } else {
            Write-Host "  [WARN] $($svc.Name) (port $($svc.Port)) — HTTP $statusCode" -ForegroundColor Yellow
            $unhealthy++
        }
    } catch {
        Write-Host "  [DOWN] $($svc.Name) (port $($svc.Port))" -ForegroundColor Red
        $unhealthy++
    }
}

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  Results: $healthy UP / $unhealthy DOWN (of $($services.Count) services)" -ForegroundColor $(if ($unhealthy -eq 0) { "Green" } else { "Yellow" })
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Exit with error code if any service is down
if ($unhealthy -gt 0) {
    exit 1
}
