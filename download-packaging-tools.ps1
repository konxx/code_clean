param(
    [switch]$IncludeWix
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ToolsDir = Join-Path $ProjectRoot "tools"
$DownloadsDir = Join-Path $ToolsDir "downloads"
$JdkDir = Join-Path $ToolsDir "jdk"
$WixDir = Join-Path $ToolsDir "wix"
$JdkZip = Join-Path $DownloadsDir "microsoft-jdk-21-windows-x64.zip"
$WixZip = Join-Path $DownloadsDir "wix314-binaries.zip"
$JdkUrl = "https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip"
$WixUrl = "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"

function Download-File {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Output
    )

    if (Test-Path -LiteralPath $Output) {
        Remove-Item -LiteralPath $Output -Force
    }

    curl.exe -L --fail --retry 3 --output $Output $Url
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $Output)) {
        Write-Host "curl 下载失败，尝试使用 Invoke-WebRequest..."
        Invoke-WebRequest -Uri $Url -OutFile $Output
    }

    if (-not (Test-Path -LiteralPath $Output) -or (Get-Item -LiteralPath $Output).Length -le 0) {
        throw "下载失败: $Url"
    }
}

New-Item -ItemType Directory -Force -Path $DownloadsDir | Out-Null

if (-not (Test-Path -LiteralPath (Join-Path $JdkDir "bin\jpackage.exe"))) {
    Write-Host "Downloading JDK with jpackage..."
    Download-File -Url $JdkUrl -Output $JdkZip

    if (Test-Path -LiteralPath $JdkDir) {
        Remove-Item -LiteralPath $JdkDir -Recurse -Force
    }

    $ExtractDir = Join-Path $ToolsDir "jdk-extract"
    if (Test-Path -LiteralPath $ExtractDir) {
        Remove-Item -LiteralPath $ExtractDir -Recurse -Force
    }

    Expand-Archive -LiteralPath $JdkZip -DestinationPath $ExtractDir -Force
    $JdkRoot = Get-ChildItem -LiteralPath $ExtractDir -Directory | Select-Object -First 1
    if (-not $JdkRoot) {
        throw "JDK 解压失败"
    }

    Move-Item -LiteralPath $JdkRoot.FullName -Destination $JdkDir
    Remove-Item -LiteralPath $ExtractDir -Recurse -Force
}

$JPackage = Join-Path $JdkDir "bin\jpackage.exe"
if (-not (Test-Path -LiteralPath $JPackage)) {
    throw "未找到 jpackage.exe: $JPackage"
}

if ($IncludeWix -and (-not (Test-Path -LiteralPath (Join-Path $WixDir "candle.exe")) -or -not (Test-Path -LiteralPath (Join-Path $WixDir "light.exe")))) {
    Write-Host "Downloading WiX Toolset binaries..."
    Download-File -Url $WixUrl -Output $WixZip

    if (Test-Path -LiteralPath $WixDir) {
        Remove-Item -LiteralPath $WixDir -Recurse -Force
    }

    New-Item -ItemType Directory -Force -Path $WixDir | Out-Null
    Expand-Archive -LiteralPath $WixZip -DestinationPath $WixDir -Force
    if (-not (Test-Path -LiteralPath (Join-Path $WixDir "candle.exe")) -or -not (Test-Path -LiteralPath (Join-Path $WixDir "light.exe"))) {
        throw "WiX 解压后未找到 candle.exe/light.exe: $WixDir"
    }
}

Write-Host "jpackage: $JPackage"
if ($IncludeWix) {
    Write-Host "WiX: $WixDir"
}
