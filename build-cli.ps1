$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Javac = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
$Jar = "C:\Program Files\Android\Android Studio\jbr\bin\jar.exe"
$Classes = Join-Path $ProjectRoot "target\classes"
$LibDir = Join-Path $ProjectRoot "lib"
$Manifest = Join-Path $ProjectRoot "target\cli-manifest.mf"
$JarFile = Join-Path $ProjectRoot "target\codeclean.jar"
$DistRoot = Join-Path $ProjectRoot "dist"
$DistDir = Join-Path $DistRoot "codeclean"
$LauncherSource = Join-Path $ProjectRoot "src\launcher\CodeCleanLauncher.cs"
$LauncherExe = Join-Path $DistDir "codeclean.exe"

& (Join-Path $ProjectRoot "build.ps1")

if (-not (Test-Path -LiteralPath $Jar)) {
    throw "未找到 jar.exe: $Jar"
}

$manifestContent = @(
    "Manifest-Version: 1.0"
    "Main-Class: com.source.CodeCleanCli"
    ""
)
Set-Content -LiteralPath $Manifest -Value $manifestContent -Encoding ASCII
& $Jar cfm $JarFile $Manifest -C $Classes .

$resolvedDistRoot = if (Test-Path -LiteralPath $DistRoot) {
    (Resolve-Path -LiteralPath $DistRoot).Path
} else {
    New-Item -ItemType Directory -Force -Path $DistRoot | Out-Null
    (Resolve-Path -LiteralPath $DistRoot).Path
}

if (Test-Path -LiteralPath $DistDir) {
    $resolvedDistDir = (Resolve-Path -LiteralPath $DistDir).Path
    if (-not $resolvedDistDir.StartsWith($resolvedDistRoot)) {
        throw "拒绝清理非 dist 子目录: $resolvedDistDir"
    }

    Remove-Item -LiteralPath $resolvedDistDir -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
Copy-Item -LiteralPath $JarFile -Destination (Join-Path $DistDir "codeclean.jar") -Force
Copy-Item -LiteralPath $LibDir -Destination (Join-Path $DistDir "lib") -Recurse -Force
Copy-Item -LiteralPath (Join-Path $ProjectRoot "codeclean.cmd") -Destination (Join-Path $DistDir "codeclean.cmd") -Force

$CscCandidates = @(
    (Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"),
    (Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe")
)
$Csc = $CscCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $Csc) {
    throw "未找到 csc.exe，无法生成 codeclean.exe"
}

& $Csc /nologo /target:exe /platform:anycpu /out:$LauncherExe $LauncherSource
if (-not (Test-Path -LiteralPath $LauncherExe)) {
    throw "codeclean.exe 生成失败"
}

Write-Host "CLI build complete: $DistDir"
Write-Host "Executable: $LauncherExe"
Write-Host "Add this directory to PATH, then run: codeclean `"软件名称`" `"项目路径`""
