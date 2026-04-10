param(
    [string]$DoctorOutputRoot = (Join-Path $PSScriptRoot "..\\HealthControl.springboot\\external-resources\\test-fixtures\\doctors"),
    [string]$ScienceOutputRoot = (Join-Path $PSScriptRoot "..\\HealthControl.springboot\\external-resources\\test-fixtures\\science"),
    [string]$LaryngoscopeOutputRoot = (Join-Path $PSScriptRoot "..\\HealthControl.springboot\\external-resources\\test-fixtures\\laryngoscope"),
    [string]$SqlOutput = (Join-Path (Split-Path -Parent $PSScriptRoot) "image-table-image-updates.sql")
)

$ErrorActionPreference = "Stop"

& javac -encoding UTF-8 scripts\GenerateImageTableFixtureImages.java
if ($LASTEXITCODE -ne 0) {
    throw "Failed to compile GenerateImageTableFixtureImages.java"
}

& java -cp "scripts" GenerateImageTableFixtureImages $DoctorOutputRoot $ScienceOutputRoot $LaryngoscopeOutputRoot $SqlOutput
if ($LASTEXITCODE -ne 0) {
    throw "Failed to run GenerateImageTableFixtureImages"
}
