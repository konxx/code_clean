param(
    [switch]$BuildInstaller
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ToolsDir = Join-Path $ProjectRoot "tools"
$JPackage = Join-Path $ToolsDir "jdk\bin\jpackage.exe"
$WixDir = Join-Path $ToolsDir "wix"
$DistCodeclean = Join-Path $ProjectRoot "dist\codeclean"
$NativeRoot = Join-Path $ProjectRoot "dist\native"
$InputDir = Join-Path $ProjectRoot "target\jpackage-input"
$AppImageDest = Join-Path $NativeRoot "app-image"
$InstallerDest = Join-Path $NativeRoot "installer"
$SingleFileDir = Join-Path $NativeRoot "single-file"
$SingleFileZip = Join-Path $ProjectRoot "target\codeclean-app.zip"
$SingleFileSource = Join-Path $ProjectRoot "src\launcher\CodeCleanSingleFileLauncher.cs"
$SingleFileExe = Join-Path $SingleFileDir "codeclean.exe"
$AppName = "codeclean"
$AppVersion = "1.0.0"

if ($BuildInstaller) {
    & (Join-Path $ProjectRoot "download-packaging-tools.ps1") -IncludeWix
} else {
    & (Join-Path $ProjectRoot "download-packaging-tools.ps1")
}
& (Join-Path $ProjectRoot "build-cli.ps1")

if (Test-Path -LiteralPath $InputDir) {
    Remove-Item -LiteralPath $InputDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null

Copy-Item -LiteralPath (Join-Path $DistCodeclean "codeclean.jar") -Destination $InputDir -Force
Copy-Item -Path (Join-Path $DistCodeclean "lib\*.jar") -Destination $InputDir -Force

if (Test-Path -LiteralPath $AppImageDest) {
    Remove-Item -LiteralPath $AppImageDest -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $AppImageDest | Out-Null

& $JPackage `
    --type app-image `
    --name $AppName `
    --dest $AppImageDest `
    --input $InputDir `
    --main-jar "codeclean.jar" `
    --main-class "com.source.CodeCleanCli" `
    --app-version $AppVersion `
    --vendor "Taoshen" `
    --win-console `
    --java-options "-Dfile.encoding=UTF-8" `
    --java-options "-Dsun.stdout.encoding=UTF-8" `
    --java-options "-Dsun.stderr.encoding=UTF-8"

$AppExe = Join-Path $AppImageDest "$AppName\$AppName.exe"
if (-not (Test-Path -LiteralPath $AppExe)) {
    throw "jpackage app-image 生成失败: $AppExe"
}

Write-Host "Standalone app image: $AppExe"

if (Test-Path -LiteralPath $SingleFileDir) {
    Remove-Item -LiteralPath $SingleFileDir -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $SingleFileDir | Out-Null
if (Test-Path -LiteralPath $SingleFileZip) {
    Remove-Item -LiteralPath $SingleFileZip -Force
}

Compress-Archive -LiteralPath (Join-Path $AppImageDest $AppName) -DestinationPath $SingleFileZip -Force

$CscCandidates = @(
    (Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"),
    (Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe")
)
$Csc = $CscCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $Csc) {
    throw "未找到 csc.exe，无法生成单文件 codeclean.exe"
}

$CompressionDll = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\System.IO.Compression.dll"
$CompressionFsDll = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\System.IO.Compression.FileSystem.dll"
if (-not (Test-Path -LiteralPath $CompressionDll)) {
    $CompressionDll = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\System.IO.Compression.dll"
}
if (-not (Test-Path -LiteralPath $CompressionFsDll)) {
    $CompressionFsDll = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\System.IO.Compression.FileSystem.dll"
}

& $Csc /nologo /target:exe /platform:anycpu /out:$SingleFileExe /resource:"$SingleFileZip,codeclean-app.zip" /reference:$CompressionDll /reference:$CompressionFsDll $SingleFileSource
if (-not (Test-Path -LiteralPath $SingleFileExe)) {
    throw "单文件 codeclean.exe 生成失败"
}

Write-Host "Single-file standalone exe: $SingleFileExe"

if ($BuildInstaller -and (Test-Path -LiteralPath (Join-Path $WixDir "candle.exe"))) {
    if (Test-Path -LiteralPath $InstallerDest) {
        Remove-Item -LiteralPath $InstallerDest -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $InstallerDest | Out-Null

    $previousPath = $env:PATH
    try {
        $env:PATH = "$WixDir;$env:PATH"
        & $JPackage `
            --type exe `
            --name $AppName `
            --dest $InstallerDest `
            --input $InputDir `
            --main-jar "codeclean.jar" `
            --main-class "com.source.CodeCleanCli" `
            --app-version $AppVersion `
            --vendor "Taoshen" `
            --win-console `
            --java-options "-Dfile.encoding=UTF-8" `
            --java-options "-Dsun.stdout.encoding=UTF-8" `
            --java-options "-Dsun.stderr.encoding=UTF-8"
    } finally {
        $env:PATH = $previousPath
    }

    $Installer = Get-ChildItem -LiteralPath $InstallerDest -Filter "*.exe" | Select-Object -First 1
    if ($Installer) {
        Write-Host "Standalone installer: $($Installer.FullName)"
    } else {
        throw "未生成 installer exe，请查看 jpackage 输出"
    }
}
