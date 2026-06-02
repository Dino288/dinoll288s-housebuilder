param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [Parameter(Mandatory = $false)]
    [string]$Token = $env:CURSEFORGE_TOKEN,

    [string]$FilePath = "dist\dinoll288s_housebuilder-0.1.0-forge-1.20.1.jar",
    [string]$DisplayName = "DinoLL288s Housebuilder 0.1.0 - Forge 1.20.1",
    [string]$ReleaseType = "beta",
    [string[]]$GameVersionNames = @("1.20.1", "Forge", "Client"),
    [string]$ChangelogPath = "RELEASE_NOTES.md",
    [int]$TimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Token)) {
    throw "Set CURSEFORGE_TOKEN or pass -Token. CurseForge requires an author API token for uploads."
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$resolvedFile = Resolve-Path (Join-Path $repoRoot $FilePath)
$resolvedChangelog = Resolve-Path (Join-Path $repoRoot $ChangelogPath)

$metadata = @{
    changelog = Get-Content -LiteralPath $resolvedChangelog -Raw
    changelogType = "markdown"
    displayName = $DisplayName
    gameVersionNames = $GameVersionNames
    releaseType = $ReleaseType
} | ConvertTo-Json -Depth 10

$uri = "https://minecraft.curseforge.com/api/projects/$ProjectId/upload-file"
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds($TimeoutSeconds)
$client.DefaultRequestHeaders.Add("X-Api-Token", $Token)
$form = [System.Net.Http.MultipartFormDataContent]::new()

try {
    $form.Add([System.Net.Http.StringContent]::new($metadata, [System.Text.Encoding]::UTF8, "application/json"), "metadata")

    $stream = [System.IO.File]::OpenRead($resolvedFile)
    try {
        $fileContent = [System.Net.Http.StreamContent]::new($stream)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/java-archive")
        $form.Add($fileContent, "file", [System.IO.Path]::GetFileName($resolvedFile))

        $response = $client.PostAsync($uri, $form).GetAwaiter().GetResult()
        $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        if (-not $response.IsSuccessStatusCode) {
            throw "CurseForge upload failed with HTTP $([int]$response.StatusCode): $body"
        }

        Write-Host $body
    }
    finally {
        $stream.Dispose()
    }
}
finally {
    $form.Dispose()
    $client.Dispose()
}
