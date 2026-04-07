$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$hooksPath = Join-Path $repoRoot ".githooks"

if (-not (Test-Path $hooksPath)) {
    throw "Hooks path not found: $hooksPath"
}

$safeDirectory = $repoRoot.Replace("\", "/")

git -C $repoRoot -c "safe.directory=$safeDirectory" config core.hooksPath .githooks

Write-Host "Configured local Git hooks path to .githooks"
Write-Host "Current core.hooksPath:"
git -C $repoRoot -c "safe.directory=$safeDirectory" config --get core.hooksPath
