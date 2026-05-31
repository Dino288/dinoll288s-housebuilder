param(
    [string]$JavaHome = "C:\Users\luke4\curseforge\minecraft\Install\runtime\java-runtime-gamma\windows-x64\java-runtime-gamma"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$distDir = Join-Path $repoRoot "dist"

$env:JAVA_HOME = $JavaHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_OPTS = "-Dnet.minecraftforge.gradle.check.certs=false"

Push-Location $repoRoot
try {
    & .\gradlew.bat --offline clean build
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }

    New-Item -ItemType Directory -Force -Path $distDir | Out-Null
    Copy-Item -LiteralPath (Join-Path $repoRoot "build\libs\dinoll288s_housebuilder-0.1.0.jar") `
        -Destination (Join-Path $distDir "dinoll288s_housebuilder-0.1.0-forge-1.20.1.jar") `
        -Force
    Write-Host "Built dist\dinoll288s_housebuilder-0.1.0-forge-1.20.1.jar"
}
finally {
    Pop-Location
}
