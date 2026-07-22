[CmdletBinding()]
param(
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $root 'dist'
}
$manifestPath = Join-Path $root 'manifest.json'
$manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
$version = [string]$manifest.version
if ([string]::IsNullOrWhiteSpace($version)) {
    throw 'manifest.json has no pack version.'
}

& (Join-Path $PSScriptRoot 'sync-curseforge-overrides.ps1') -Check
if ($LASTEXITCODE -ne 0) {
    throw 'CurseForge overrides are stale. Run tools/sync-curseforge-overrides.ps1 first.'
}

$updateConfig = Get-Content -LiteralPath (Join-Path $root 'config\modpack-update-checker\config.json') -Raw | ConvertFrom-Json
$updateMeta = Get-Content -LiteralPath (Join-Path $root 'update\meta.json') -Raw | ConvertFrom-Json
if ([string]$updateConfig.currentVersion -ne $version -or [string]$updateMeta.latestVersion -ne $version) {
    throw "Release version mismatch: manifest=$version, checker=$($updateConfig.currentVersion), latest=$($updateMeta.latestVersion)"
}

$requiredPayload = @(
    'overrides/config/modpack-update-checker/config.json',
    'overrides/config/EndRemastered-NeoForge/endrem.json',
    'overrides/kubejs/data/immersiveenchanting/enchantment_costs',
    'overrides/kubejs/data/creating_stories/loot_table/end_eyes',
    'overrides/kubejs/server_scripts/end_eye_progression.js',
    'overrides/kubejs/config/web_server.json'
)
foreach ($relativePath in $requiredPayload) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $relativePath))) {
        throw "Required release payload is missing: $relativePath"
    }
}
$webConfig = Get-Content -LiteralPath (Join-Path $root 'overrides\kubejs\config\web_server.json') -Raw | ConvertFrom-Json
if ($webConfig.enabled -ne $false -or $null -ne $webConfig.PSObject.Properties['auth']) {
    throw 'Packaged KubeJS web-server configuration must be disabled and must not contain an auth credential.'
}

$outputDirectoryPath = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $outputDirectoryPath -Force | Out-Null
$outputPath = Join-Path $outputDirectoryPath "Creating-Stories-$version-CurseForge.zip"
if (Test-Path -LiteralPath $outputPath) {
    Remove-Item -LiteralPath $outputPath -Force
}

Push-Location $root
try {
    Compress-Archive -LiteralPath 'manifest.json', 'overrides' -DestinationPath $outputPath -CompressionLevel Optimal
}
finally {
    Pop-Location
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($outputPath)
try {
    $entries = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    foreach ($entry in $archive.Entries) {
        [void]$entries.Add($entry.FullName.Replace('\', '/'))
    }
    foreach ($requiredEntry in @(
        'manifest.json',
        'overrides/config/modpack-update-checker/config.json',
        'overrides/kubejs/server_scripts/end_eye_progression.js',
        'overrides/kubejs/config/web_server.json'
    )) {
        if (-not $entries.Contains($requiredEntry)) {
            throw "Built ZIP is missing required entry: $requiredEntry"
        }
    }
    $entryCount = $entries.Count
}
finally {
    $archive.Dispose()
}

$hash = (Get-FileHash -LiteralPath $outputPath -Algorithm SHA256).Hash
Write-Output "Built $outputPath"
Write-Output "Entries: $entryCount"
Write-Output "SHA256: $hash"
