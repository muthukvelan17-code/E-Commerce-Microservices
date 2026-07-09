$infraDir = "c:\e com\infra"
$env:PGPASSWORD = "password"

Write-Host "Starting PostgreSQL..." -ForegroundColor Green
& "$infraDir\pgsql\bin\pg_ctl.exe" -D "$infraDir\postgres-data" -l "$infraDir\postgres.log" start
Start-Sleep -Seconds 3

Write-Host "Creating PostgreSQL databases if they do not exist..." -ForegroundColor Green
& "$infraDir\pgsql\bin\psql.exe" -U postgres -d postgres -c "CREATE DATABASE user_db;" 2>$null
& "$infraDir\pgsql\bin\psql.exe" -U postgres -d postgres -c "CREATE DATABASE inventory_db;" 2>$null
& "$infraDir\pgsql\bin\psql.exe" -U postgres -d postgres -c "CREATE DATABASE order_db;" 2>$null
& "$infraDir\pgsql\bin\psql.exe" -U postgres -d postgres -c "CREATE DATABASE payment_db;" 2>$null

Write-Host "Starting MongoDB..." -ForegroundColor Green
Start-Process -FilePath "$infraDir\mongo\bin\mongod.exe" -ArgumentList "--dbpath `"$infraDir\mongo-data`" --port 27017" -WindowStyle Hidden
Start-Sleep -Seconds 2

Write-Host "Starting ZooKeeper..." -ForegroundColor Green
Start-Process -FilePath "$infraDir\confluent\bin\windows\zookeeper-server-start.bat" -ArgumentList "`"$infraDir\confluent\etc\kafka\zookeeper.properties`"" -WindowStyle Hidden
Start-Sleep -Seconds 5

Write-Host "Starting Kafka..." -ForegroundColor Green
Start-Process -FilePath "$infraDir\confluent\bin\windows\kafka-server-start.bat" -ArgumentList "`"$infraDir\confluent\etc\kafka\server.properties`"" -WindowStyle Hidden
Start-Sleep -Seconds 20

Write-Host "Starting Schema Registry..." -ForegroundColor Green
$srCp = "$infraDir\confluent\share\java\schema-registry\*;$infraDir\confluent\share\java\rest-utils\*;$infraDir\confluent\share\java\common-utils\*;$infraDir\confluent\share\java\kafka\*"
Start-Process -FilePath "java" -ArgumentList "-cp `"$srCp`" io.confluent.kafka.schemaregistry.rest.SchemaRegistryMain `"$infraDir\confluent\etc\schema-registry\schema-registry.properties`"" -WindowStyle Hidden
Start-Sleep -Seconds 10

Write-Host "All infrastructure components started successfully!" -ForegroundColor Cyan
