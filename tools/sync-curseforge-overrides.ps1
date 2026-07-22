[CmdletBinding()]
param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$overridesRoot = Join-Path $root 'overrides'
$sourceRoots = @('config', 'defaultconfigs', 'kubejs')
$releaseOnlyFiles = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
[void]$releaseOnlyFiles.Add('overrides/config/iris.properties')
[void]$releaseOnlyFiles.Add('overrides/kubejs/config/web_server.json')

# Generated caches, machine-local state, and credentials must not be distributed.
$excludedFiles = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
@(
    'config/ars_nouveau/doc_data.json',
    'config/jamlib/known_suspicious_jars.txt',
    'config/sodium-fingerprint.json',
    'config/spark/config.json',
    'config/spark/tmp-client/about.txt',
    'config/spark/tmp/about.txt',
    'kubejs/config/web_server.json'
) | ForEach-Object { [void]$excludedFiles.Add($_) }
$excludedPrefixes = @('config/ars_nouveau/search_index/')

function Normalize-RelativePath {
    param([string]$Path)
    return $Path.Replace('\', '/').TrimStart('/')
}

function Is-Excluded {
    param([string]$RelativePath)
    if ($excludedFiles.Contains($RelativePath)) {
        return $true
    }
    foreach ($prefix in $excludedPrefixes) {
        if ($RelativePath.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

function Files-AreEqual {
    param(
        [string]$Source,
        [string]$Destination
    )
    if (-not (Test-Path -LiteralPath $Destination -PathType Leaf)) {
        return $false
    }
    $sourceInfo = Get-Item -LiteralPath $Source
    $destinationInfo = Get-Item -LiteralPath $Destination
    if ($sourceInfo.Length -ne $destinationInfo.Length) {
        return $false
    }
    return (Get-FileHash -LiteralPath $Source -Algorithm SHA256).Hash -eq
        (Get-FileHash -LiteralPath $Destination -Algorithm SHA256).Hash
}

$trackedOutput = & git -C $root ls-files -- @sourceRoots
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to enumerate version-controlled runtime files.'
}

$sources = [System.Collections.Generic.List[string]]::new()
$expectedDestinations = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($entry in $trackedOutput) {
    $relativePath = Normalize-RelativePath $entry
    if ([string]::IsNullOrWhiteSpace($relativePath) -or (Is-Excluded $relativePath)) {
        continue
    }
    $source = Join-Path $root $relativePath
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        throw "Tracked runtime file is missing: $relativePath"
    }
    $sources.Add($relativePath)
    [void]$expectedDestinations.Add("overrides/$relativePath")
}

$differences = [System.Collections.Generic.List[string]]::new()
foreach ($relativePath in $sources) {
    $source = Join-Path $root $relativePath
    $destination = Join-Path $overridesRoot $relativePath
    if (Files-AreEqual -Source $source -Destination $destination) {
        continue
    }
    if ($Check) {
        $kind = if (Test-Path -LiteralPath $destination) { 'out of date' } else { 'missing' }
        $differences.Add("${kind}: overrides/$relativePath")
        continue
    }
    $destinationDirectory = Split-Path -Parent $destination
    New-Item -ItemType Directory -Path $destinationDirectory -Force | Out-Null
    [System.IO.File]::Copy($source, $destination, $true)
}

foreach ($sourceRoot in $sourceRoots) {
    $mirroredRoot = Join-Path $overridesRoot $sourceRoot
    if (-not (Test-Path -LiteralPath $mirroredRoot -PathType Container)) {
        continue
    }
    foreach ($file in Get-ChildItem -LiteralPath $mirroredRoot -Recurse -File) {
        $relative = Normalize-RelativePath $file.FullName.Substring($root.Length)
        if ($expectedDestinations.Contains($relative) -or $releaseOnlyFiles.Contains($relative)) {
            continue
        }
        if ($Check) {
            $differences.Add("stale: $relative")
        }
        else {
            Remove-Item -LiteralPath $file.FullName -Force
        }
    }
}

if ($Check -and $differences.Count -gt 0) {
    Write-Output "CurseForge override validation failed with $($differences.Count) difference(s):"
    $differences | Sort-Object | ForEach-Object { Write-Output "  - $_" }
    exit 1
}

$verb = if ($Check) { 'Verified' } else { 'Synchronized' }
Write-Output "$verb $($sources.Count) tracked runtime files under CurseForge overrides."
Write-Output "Excluded $($excludedFiles.Count) generated or sensitive files plus $($excludedPrefixes.Count) generated path prefix(es)."
