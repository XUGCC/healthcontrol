param(
    [string]$OutputRoot = (Join-Path $PSScriptRoot "..\HealthControl.springboot\external-resources\test-fixtures\foods"),
    [string]$SqlOutput = (Join-Path (Split-Path -Parent $PSScriptRoot) "food-realistic-image-updates.sql")
)

$ErrorActionPreference = "Stop"

& javac -encoding UTF-8 scripts\GenerateFoodFixtureImages.java
if ($LASTEXITCODE -ne 0) {
    throw "Failed to compile GenerateFoodFixtureImages.java"
}

& java -cp "scripts" GenerateFoodFixtureImages $OutputRoot $SqlOutput
if ($LASTEXITCODE -ne 0) {
    throw "Failed to run GenerateFoodFixtureImages"
}
