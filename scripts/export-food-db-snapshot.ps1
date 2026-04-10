param(
    [string]$OutputPath = (Join-Path (Split-Path -Parent $PSScriptRoot) "food-db-snapshot.md")
)

$ErrorActionPreference = "Stop"

$mysqlJar = Join-Path $env:USERPROFILE ".m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar"

& javac -encoding UTF-8 -cp $mysqlJar scripts\ExportFoodDbSnapshot.java
if ($LASTEXITCODE -ne 0) {
    throw "Failed to compile ExportFoodDbSnapshot.java"
}

& java -cp "scripts;$mysqlJar" ExportFoodDbSnapshot $OutputPath
if ($LASTEXITCODE -ne 0) {
    throw "Failed to run ExportFoodDbSnapshot"
}
