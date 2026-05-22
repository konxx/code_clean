$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$CliDir = Join-Path $ProjectRoot "dist\codeclean"

if (-not (Test-Path -LiteralPath (Join-Path $CliDir "codeclean.cmd"))) {
    throw "未找到 CLI 程序，请先执行 .\build-cli.ps1"
}

$resolvedCliDir = (Resolve-Path -LiteralPath $CliDir).Path
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$pathItems = @()
if (-not [string]::IsNullOrWhiteSpace($currentPath)) {
    $pathItems = $currentPath -split ";"
}

$alreadyInstalled = $pathItems | Where-Object {
    -not [string]::IsNullOrWhiteSpace($_) -and
    ([System.IO.Path]::GetFullPath($_.TrimEnd('\')) -ieq $resolvedCliDir.TrimEnd('\'))
}

if ($alreadyInstalled) {
    Write-Host "PATH 已包含: $resolvedCliDir"
} else {
    $newPath = if ([string]::IsNullOrWhiteSpace($currentPath)) {
        $resolvedCliDir
    } else {
        "$currentPath;$resolvedCliDir"
    }
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "已加入用户 PATH: $resolvedCliDir"
}

Write-Host "请重新打开 PowerShell 后执行: codeclean `"软件名称`" `"项目路径`""
