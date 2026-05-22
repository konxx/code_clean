$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BundledJava = "C:\Users\Administrator\Desktop\11\软著源代码生成工具window_v1.1\jdk\bin\java.exe"
$FallbackJava = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
$Java = if (Test-Path -LiteralPath $BundledJava) { $BundledJava } else { $FallbackJava }
$Classes = Join-Path $ProjectRoot "target\classes"
$LibDir = Join-Path $ProjectRoot "lib"

& (Join-Path $ProjectRoot "build.ps1")

$LibClasspath = (Get-ChildItem -LiteralPath $LibDir -File -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"
$Classpath = "$Classes;$LibClasspath"

& $Java -cp $Classpath com.source.SourceDocxApplication
