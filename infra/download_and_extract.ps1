$infraDir = "c:\e com\infra"
$ProgressPreference = 'SilentlyContinue'

# URLs
$postgresUrl = "https://get.enterprisedb.com/postgresql/postgresql-15.3-1-windows-x64-binaries.zip"
$mongoUrl = "https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-6.0.8.zip"
$confluentUrl = "https://packages.confluent.io/archive/7.4/confluent-community-7.4.0.zip"

# Files
$postgresZip = Join-Path $infraDir "postgres.zip"
$mongoZip = Join-Path $infraDir "mongo.zip"
$confluentZip = Join-Path $infraDir "confluent.zip"

Write-Host "Downloading PostgreSQL..."
if (-not (Test-Path $postgresZip)) {
    curl.exe -L -o $postgresZip $postgresUrl
}

Write-Host "Downloading MongoDB..."
if (-not (Test-Path $mongoZip)) {
    curl.exe -L -o $mongoZip $mongoUrl
}

Write-Host "Downloading Confluent Community..."
if (-not (Test-Path $confluentZip)) {
    curl.exe -L -o $confluentZip $confluentUrl
}

Write-Host "Extracting PostgreSQL..."
if (-not (Test-Path (Join-Path $infraDir "pgsql"))) {
    Expand-Archive -Path $postgresZip -DestinationPath $infraDir
}

Write-Host "Extracting MongoDB..."
if (-not (Test-Path (Join-Path $infraDir "mongo"))) {
    Expand-Archive -Path $mongoZip -DestinationPath (Join-Path $infraDir "mongo_temp")
    $extractedFolder = Get-ChildItem (Join-Path $infraDir "mongo_temp") | Select-Object -First 1
    Move-Item $extractedFolder.FullName (Join-Path $infraDir "mongo")
    Remove-Item (Join-Path $infraDir "mongo_temp") -Recurse -Force
}

Write-Host "Extracting Confluent..."
if (-not (Test-Path (Join-Path $infraDir "confluent"))) {
    Expand-Archive -Path $confluentZip -DestinationPath (Join-Path $infraDir "confluent_temp")
    $extractedFolder = Get-ChildItem (Join-Path $infraDir "confluent_temp") | Select-Object -First 1
    Move-Item $extractedFolder.FullName (Join-Path $infraDir "confluent")
    Remove-Item (Join-Path $infraDir "confluent_temp") -Recurse -Force
}

Write-Host "Infrastructure Download and Extraction Complete!"
