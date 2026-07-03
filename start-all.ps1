# Start Infrastructure
Write-Host "Starting Infrastructure (Kafka, Postgres, MongoDB, Schema Registry)..." -ForegroundColor Cyan
& "$PSScriptRoot\infra\start_infra.ps1"

# Wait for infrastructure to be ready
Write-Host "Waiting for infrastructure to initialize (5 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Function to start a Spring Boot jar in a new window
function Start-Microservice {
    param (
        [string]$ServiceName,
        [int]$WaitSeconds = 5
    )
    Write-Host "Starting $ServiceName..." -ForegroundColor Green
    $JarPath = ".\$ServiceName\target\$ServiceName-1.0.0.jar"
    
    if (Test-Path $JarPath) {
        Start-Process "java" -ArgumentList "-jar $JarPath" -WindowStyle Normal
        Start-Sleep -Seconds $WaitSeconds
    } else {
        Write-Host "Error: Cannot find $JarPath. Make sure you ran 'mvn clean install'." -ForegroundColor Red
    }
}

# Start Core Services
Start-Microservice -ServiceName "eureka-server" -WaitSeconds 15
Start-Microservice -ServiceName "config-server" -WaitSeconds 15
Start-Microservice -ServiceName "api-gateway" -WaitSeconds 10

# Start Business Services
Start-Microservice -ServiceName "user-service"
Start-Microservice -ServiceName "product-service"
Start-Microservice -ServiceName "inventory-service"
Start-Microservice -ServiceName "order-service"
Start-Microservice -ServiceName "payment-service"
Start-Microservice -ServiceName "notification-service"

Write-Host "All services have been initiated!" -ForegroundColor Cyan
Write-Host "Eureka Dashboard: http://localhost:8761"
Write-Host "API Gateway: http://localhost:8080"
