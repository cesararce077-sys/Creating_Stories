[CmdletBinding()]
param(
    [switch]$Check
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$sourcePath = Join-Path $PSScriptRoot 'immersive-enchanting-costs.json'
$outputRoot = Join-Path $root 'kubejs\data\immersiveenchanting\enchantment_costs'
$source = Get-Content -LiteralPath $sourcePath -Raw | ConvertFrom-Json
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$differences = [System.Collections.Generic.List[string]]::new()

if ($source.format -ne 1) {
    throw "Unsupported cost-map format: $($source.format)"
}

foreach ($property in $source.costs.PSObject.Properties) {
    $enchantmentId = $property.Name
    $parts = $enchantmentId.Split(':', 2)
    if ($parts.Count -ne 2 -or [string]::IsNullOrWhiteSpace($parts[0]) -or [string]::IsNullOrWhiteSpace($parts[1])) {
        throw "Invalid enchantment id: $enchantmentId"
    }

    $costs = @($property.Value)
    if ($costs.Count -eq 0) {
        throw "No level costs declared for $enchantmentId"
    }

    $lines = [System.Collections.Generic.List[string]]::new()
    $lines.Add('{')
    $lines.Add('  "levels": {')
    for ($index = 0; $index -lt $costs.Count; $index++) {
        $cost = $costs[$index]
        $itemJson = ([string]$cost.item | ConvertTo-Json -Compress)
        $levelSuffix = if ($index -lt $costs.Count - 1) { ',' } else { '' }
        $lines.Add("    `"$($index + 1)`": [")
        $lines.Add('      {')
        $lines.Add("        `"item`": $itemJson,")
        $lines.Add("        `"amount`": $([int]$cost.amount)")
        $lines.Add('      }')
        $lines.Add("    ]$levelSuffix")
    }
    $lines.Add('  }')
    $lines.Add('}')
    $json = [string]::Join([Environment]::NewLine, $lines) + [Environment]::NewLine
    $destination = Join-Path $outputRoot (Join-Path $parts[0] ($parts[1] + '.json'))

    if ($Check) {
        if (-not (Test-Path -LiteralPath $destination)) {
            $differences.Add("missing: $enchantmentId")
            continue
        }

        $actual = Get-Content -LiteralPath $destination -Raw
        if ($actual -ne $json) {
            $differences.Add("out of date: $enchantmentId")
        }
        continue
    }

    $directory = Split-Path -Parent $destination
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
    [System.IO.File]::WriteAllText($destination, $json, $utf8NoBom)
}

if ($Check -and $differences.Count -gt 0) {
    $differences | ForEach-Object { Write-Error $_ }
    exit 1
}

$definitionCount = @($source.costs.PSObject.Properties).Count
$verb = if ($Check) { 'Verified' } else { 'Generated' }
Write-Output "$verb $definitionCount Immersive Enchanting cost definitions."
