$infraDir = "c:\e com\infra"
$ProgressPreference = 'SilentlyContinue'

# Complete BITS jobs if they are in Transferred state
Get-BitsTransfer | Where-Object {$_.JobState -eq "Transferred"} | Complete-BitsTransfer

# Create password file for PostgreSQL
"password" | Out-File -FilePath "$infraDir\password.txt" -Encoding ascii -NoNewline

# Cleanup old directories to prevent lock or merge issues
Write-Host "Cleaning up old directories..."
@( "mongo", "mongo_temp", "confluent", "confluent_temp") | ForEach-Object {
    $path = Join-Path $infraDir $_
    if (Test-Path $path) {
        Remove-Item $path -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# Extract PostgreSQL
Write-Host "Extracting PostgreSQL using tar..."
if (-not (Test-Path "$infraDir\pgsql")) {
    if (Test-Path "$infraDir\postgres.zip") {
        tar.exe -xf "$infraDir\postgres.zip" -C "$infraDir"
    } else {
        Write-Error "postgres.zip not found!"
    }
} else {
    Write-Host "PostgreSQL already extracted."
}

# Extract MongoDB
Write-Host "Extracting MongoDB using Expand-Archive..."
if (-not (Test-Path "$infraDir\mongo")) {
    if (Test-Path "$infraDir\mongo.zip") {
        Expand-Archive -Path "$infraDir\mongo.zip" -DestinationPath "$infraDir\mongo_temp"
        $extractedFolder = Get-ChildItem "$infraDir\mongo_temp" | Select-Object -First 1
        Move-Item $extractedFolder.FullName "$infraDir\mongo"
        Remove-Item "$infraDir\mongo_temp" -Recurse -Force
    } else {
        Write-Error "mongo.zip not found!"
    }
} else {
    Write-Host "MongoDB already extracted."
}

# Extract Confluent Platform
Write-Host "Extracting Confluent Platform using Expand-Archive..."
if (-not (Test-Path "$infraDir\confluent")) {
    if (Test-Path "$infraDir\confluent.zip") {
        Expand-Archive -Path "$infraDir\confluent.zip" -DestinationPath "$infraDir\confluent_temp"
        $extractedFolder = Get-ChildItem "$infraDir\confluent_temp" | Select-Object -First 1
        Move-Item $extractedFolder.FullName "$infraDir\confluent"
        Remove-Item "$infraDir\confluent_temp" -Recurse -Force
    } else {
        Write-Error "confluent.zip not found!"
    }
} else {
    Write-Host "Confluent Platform already extracted."
}

# Setup PostgreSQL Data Directory
Write-Host "Initializing PostgreSQL..."
$postgresDataDir = "$infraDir\postgres-data"
if (-not (Test-Path $postgresDataDir)) {
    & "$infraDir\pgsql\bin\initdb.exe" -D $postgresDataDir -U postgres --pwfile="$infraDir\password.txt" --auth=scram-sha-256
} else {
    Write-Host "PostgreSQL already initialized."
}

# Setup MongoDB Data Directory
Write-Host "Creating MongoDB Data Directory..."
$mongoDataDir = "$infraDir\mongo-data"
if (-not (Test-Path $mongoDataDir)) {
    New-Item -ItemType Directory -Path $mongoDataDir -Force
} else {
    Write-Host "MongoDB data directory already exists."
}

# Configure Confluent Schema Registry Port
Write-Host "Configuring Confluent Schema Registry..."
$schemaRegistryProps = Get-ChildItem -Path "$infraDir\confluent" -Filter "schema-registry.properties" -Recurse | Select-Object -First 1
if ($schemaRegistryProps) {
    $content = Get-Content $schemaRegistryProps.FullName
    $newContent = $content -replace ":8081", ":8089"
    $newContent | Out-File $schemaRegistryProps.FullName -Encoding utf8 -Force
    Write-Host "Configured Schema Registry at $($schemaRegistryProps.FullName)"
} else {
    Write-Warning "Could not find schema-registry.properties!"
}

Write-Host "Configuration Done!"
