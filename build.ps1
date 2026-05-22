$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Javac = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
$Classes = Join-Path $ProjectRoot "target\classes"
$LibDir = Join-Path $ProjectRoot "lib"
$SourceDir = Join-Path $ProjectRoot "src\main\java"
$ResourceDir = Join-Path $ProjectRoot "src\main\resources"

New-Item -ItemType Directory -Force -Path $Classes | Out-Null

$Classpath = (Get-ChildItem -LiteralPath $LibDir -File -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$Sources = Get-ChildItem -LiteralPath $SourceDir -Recurse -File -Filter "*.java" | ForEach-Object { $_.FullName }

& $Javac --release 17 -proc:none -encoding UTF-8 -cp $Classpath -d $Classes $Sources
Copy-Item -Path (Join-Path $ResourceDir "*") -Destination $Classes -Recurse -Force

Write-Host "Build complete: $Classes"
