$gatewayUrl = "http://localhost:8080"
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "   Starting ApexCommerce E2E API Verification" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Sign up as CUSTOMER
Write-Host "`n[1] Registering Customer..." -ForegroundColor Green
$customerEmail = "customer_" + (Get-Random) + "@example.com"
$signupBody = @{
    name = "E2E Customer"
    email = $customerEmail
    password = "password123"
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $res = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/auth/signup" -Method Post -Body $signupBody -ContentType "application/json"
    Write-Host "Registration Successful: $customerEmail" -ForegroundColor Green
} catch {
    Write-Host "Registration Failed: $_" -ForegroundColor Red
    exit 1
}

# 2. Log in as CUSTOMER to get JWT
Write-Host "`n[2] Logging in as Customer..." -ForegroundColor Green
$loginBody = @{
    email = $customerEmail
    password = "password123"
} | ConvertTo-Json

try {
    $loginRes = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $customerToken = $loginRes.token
    Write-Host "Login Successful. JWT Token obtained." -ForegroundColor Green
} catch {
    Write-Host "Login Failed: $_" -ForegroundColor Red
    exit 1
}

# 3. Create or Get Category as ADMIN
Write-Host "`n[3] Registering Admin and obtaining category..." -ForegroundColor Green
$adminEmail = "admin_" + (Get-Random) + "@example.com"
$adminSignup = @{
    name = "E2E Admin"
    email = $adminEmail
    password = "password123"
    role = "ADMIN"
} | ConvertTo-Json

try {
    $null = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/auth/signup" -Method Post -Body $adminSignup -ContentType "application/json"
    $adminLoginRes = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    # Wait, admin login needs admin credentials:
    $adminLoginBody = @{
        email = $adminEmail
        password = "password123"
    } | ConvertTo-Json
    $adminLoginRes = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
    $adminToken = $adminLoginRes.token
} catch {
    Write-Host "Admin Registration/Login Failed: $_" -ForegroundColor Red
    exit 1
}

# Create Category
$categoryBody = @{
    name = "E2E Electronics"
    description = "Test electronics category"
} | ConvertTo-Json

$headers = @{
    Authorization = "Bearer $adminToken"
}

try {
    $category = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/categories" -Method Post -Headers $headers -Body $categoryBody -ContentType "application/json"
    $catId = $category.id
    Write-Host "Category created successfully. ID: $catId" -ForegroundColor Green
} catch {
    Write-Host "Category creation failed, looking up existing categories..." -ForegroundColor Yellow
    try {
        $categories = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/categories" -Method Get
        if ($categories.Count -gt 0) {
            $catId = $categories[0].id
            Write-Host "Using existing Category ID: $catId" -ForegroundColor Green
        } else {
            Write-Host "No categories found and could not create one. Exiting." -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "Failed to list categories: $_" -ForegroundColor Red
        exit 1
    }
}

# 4. Create Product
Write-Host "`n[4] Creating Product as Admin..." -ForegroundColor Green
$sku = "SKU-E2E-" + (Get-Random)
$productBody = @{
    name = "Apex E2E Headset"
    sku = $sku
    price = 199.99
    description = "Premium quality sound headset for E2E testing"
    categoryId = $catId
} | ConvertTo-Json

try {
    $product = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/products" -Method Post -Headers $headers -Body $productBody -ContentType "application/json"
    $prodId = $product.id
    Write-Host "Product Created. ID: $prodId, SKU: $sku" -ForegroundColor Green
} catch {
    Write-Host "Product creation failed: $_" -ForegroundColor Red
    exit 1
}

# 5. Set Stock in Inventory Service
Write-Host "`n[5] Adding Stock to Inventory..." -ForegroundColor Green
$inventoryBody = @{
    productId = $prodId
    quantity = 100
} | ConvertTo-Json

try {
    $inventory = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/inventory" -Method Post -Headers $headers -Body $inventoryBody -ContentType "application/json"
    Write-Host "Stock initialized: 100 units." -ForegroundColor Green
} catch {
    Write-Host "Failed to add stock: $_" -ForegroundColor Red
    exit 1
}

# 6. Place Order as CUSTOMER
Write-Host "`n[6] Placing Order as Customer..." -ForegroundColor Green
$orderItems = @(
    @{
        productId = $prodId
        quantity = 2
        price = 199.99
    }
)
$orderBody = @{
    customerId = "E2E-Cust-Id"
    items = $orderItems
} | ConvertTo-Json

$custHeaders = @{
    Authorization = "Bearer $customerToken"
}

try {
    $order = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/orders" -Method Post -Headers $custHeaders -Body $orderBody -ContentType "application/json"
    $orderId = $order.id
    Write-Host "Order Placed successfully. ID: $orderId. Status: $($order.status)" -ForegroundColor Green
} catch {
    Write-Host "Order Placement failed: $_" -ForegroundColor Red
    exit 1
}

# 7. Monitor Saga Status
Write-Host "`n[7] Monitoring Saga Choreography Workflow..." -ForegroundColor Green
for ($i = 1; $i -le 5; $i++) {
    Start-Sleep -Seconds 2
    try {
        $orders = Invoke-RestMethod -Uri "$gatewayUrl/api/v1/orders" -Method Get -Headers $custHeaders
        $activeOrder = $orders | Where-Object { $_.id -eq $orderId }
        Write-Host "Checking status (Attempt $i): $($activeOrder.status)" -ForegroundColor Yellow
        if ($activeOrder.status -eq "CONFIRMED" -or $activeOrder.status -eq "CANCELLED") {
            Write-Host "`nSaga Finished!" -ForegroundColor Green
            Write-Host "Final Order Status: $($activeOrder.status)" -ForegroundColor Cyan
            break
        }
    } catch {
        Write-Host "Error polling order: $_" -ForegroundColor Red
    }
}

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "       E2E Verification Complete!" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
