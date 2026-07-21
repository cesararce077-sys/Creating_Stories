[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$modsRoot = Join-Path $root 'mods'
$sourcePath = Join-Path $PSScriptRoot 'immersive-enchanting-costs.json'
$generatedRoot = Join-Path $root 'kubejs\data\immersiveenchanting\enchantment_costs'
$source = Get-Content -LiteralPath $sourcePath -Raw | ConvertFrom-Json
$errors = [System.Collections.Generic.List[string]]::new()
$enchantments = @{}
$baseCosts = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
$items = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
$tags = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

function Read-ZipEntryText {
    param([System.IO.Compression.ZipArchiveEntry]$Entry)
    $stream = $Entry.Open()
    $reader = [System.IO.StreamReader]::new($stream)
    try {
        return $reader.ReadToEnd()
    }
    finally {
        $reader.Dispose()
        $stream.Dispose()
    }
}

foreach ($jar in Get-ChildItem -LiteralPath $modsRoot -Filter '*.jar' -File) {
    $archive = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try {
        foreach ($entry in $archive.Entries) {
            $name = $entry.FullName.Replace('\', '/')

            if ($name -match '^data/([^/]+)/enchantment/(.+)\.json$') {
                $id = "$($Matches[1]):$($Matches[2])"
                try {
                    $definition = Read-ZipEntryText -Entry $entry | ConvertFrom-Json
                    $maxLevel = if ($null -eq $definition.max_level) { 1 } else { [int]$definition.max_level }
                    $enchantments[$id] = [ordered]@{ maxLevel = $maxLevel; jar = $jar.Name }
                }
                catch {
                    $errors.Add("Cannot parse enchantment $id in $($jar.Name): $($_.Exception.Message)")
                }
                continue
            }

            if ($name -match '^data/immersiveenchanting/enchantment_costs/([^/]+)/(.+)\.json$') {
                [void]$baseCosts.Add("$($Matches[1]):$($Matches[2])")
                continue
            }

            if ($name -match '^assets/([^/]+)/models/item/(.+)\.json$') {
                [void]$items.Add("$($Matches[1]):$($Matches[2])")
                continue
            }

            if ($name -match '^data/([^/]+)/tags/item/(.+)\.json$') {
                [void]$tags.Add("$($Matches[1]):$($Matches[2])")
            }
        }
    }
    finally {
        $archive.Dispose()
    }
}

$sourceIds = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
$expectedFiles = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

foreach ($property in $source.costs.PSObject.Properties) {
    $id = $property.Name
    [void]$sourceIds.Add($id)
    $parts = $id.Split(':', 2)
    if ($parts.Count -ne 2) {
        $errors.Add("Invalid source enchantment id: $id")
        continue
    }

    $relativePath = (Join-Path $parts[0] ($parts[1] + '.json'))
    [void]$expectedFiles.Add($relativePath)
    $levels = @($property.Value)

    if (-not $enchantments.ContainsKey($id)) {
        $errors.Add("Source map references an enchantment not found in installed JAR data: $id")
    }
    elseif ($levels.Count -ne $enchantments[$id].maxLevel) {
        $errors.Add("$id maps $($levels.Count) levels, but $($enchantments[$id].jar) declares max_level $($enchantments[$id].maxLevel)")
    }

    for ($index = 0; $index -lt $levels.Count; $index++) {
        $cost = $levels[$index]
        $item = [string]$cost.item
        $amount = [int]$cost.amount
        $level = $index + 1

        if ([string]::IsNullOrWhiteSpace($item)) {
            $errors.Add("$id level $level has an empty item id")
            continue
        }
        if ($amount -lt 1 -or $amount -gt 64) {
            $errors.Add("$id level $level has invalid amount $amount")
        }

        if ($item.StartsWith('#')) {
            $tagId = $item.Substring(1)
            if (-not $tags.Contains($tagId) -and -not $tagId.StartsWith('minecraft:')) {
                $errors.Add("$id level $level references missing item tag $item")
            }
        }
        elseif (-not $items.Contains($item) -and -not $item.StartsWith('minecraft:')) {
            $errors.Add("$id level $level references an item with no installed item model: $item")
        }
    }

    $generatedPath = Join-Path $generatedRoot $relativePath
    if (-not (Test-Path -LiteralPath $generatedPath)) {
        $errors.Add("Missing generated cost file for $id")
        continue
    }

    try {
        $generated = Get-Content -LiteralPath $generatedPath -Raw | ConvertFrom-Json
        $generatedLevels = @($generated.levels.PSObject.Properties)
        if ($generatedLevels.Count -ne $levels.Count) {
            $errors.Add("Generated $id has $($generatedLevels.Count) level keys; expected $($levels.Count)")
        }
        for ($index = 0; $index -lt $levels.Count; $index++) {
            $levelKey = [string]($index + 1)
            $generatedLevel = $generated.levels.PSObject.Properties[$levelKey]
            if ($null -eq $generatedLevel) {
                $errors.Add("Generated $id is missing level $levelKey")
                continue
            }
            $entries = @($generatedLevel.Value)
            if ($entries.Count -ne 1) {
                $errors.Add("Generated $id level $levelKey must contain exactly one material option, found $($entries.Count)")
                continue
            }
            if ([string]$entries[0].item -ne [string]$levels[$index].item -or [int]$entries[0].amount -ne [int]$levels[$index].amount) {
                $errors.Add("Generated $id level $levelKey does not match the source map")
            }
        }
    }
    catch {
        $errors.Add("Cannot parse generated cost file for ${id}: $($_.Exception.Message)")
    }
}

$ignoredImmersiveActions = @(
    'immersiveenchanting:enchanting_fuels',
    'immersiveenchanting:replicate',
    'immersiveenchanting:transmute'
)
foreach ($id in $enchantments.Keys) {
    if (-not $baseCosts.Contains($id) -and -not $sourceIds.Contains($id) -and $ignoredImmersiveActions -notcontains $id) {
        $errors.Add("Installed enchantment has neither a base nor Creating Stories cost definition: $id")
    }
}

if (Test-Path -LiteralPath $generatedRoot) {
    foreach ($file in Get-ChildItem -LiteralPath $generatedRoot -Recurse -Filter '*.json' -File) {
        $relativePath = $file.FullName.Substring($generatedRoot.Length).TrimStart('\', '/')
        if (-not $expectedFiles.Contains($relativePath)) {
            $errors.Add("Generated cost file is not represented in the source map: $relativePath")
        }
    }
}

if ($errors.Count -gt 0) {
    Write-Output "Immersive Enchanting cost validation failed with $($errors.Count) error(s):"
    $errors | Sort-Object | ForEach-Object { Write-Output "  - $_" }
    exit 1
}

$definitionCount = @($source.costs.PSObject.Properties).Count
$levelCount = 0
foreach ($property in $source.costs.PSObject.Properties) {
    $levelCount += @($property.Value).Count
}
Write-Output "Validated $definitionCount custom enchantments and $levelCount paid levels."
Write-Output "Every installed data-driven enchantment has an Immersive Enchanting cost definition."
