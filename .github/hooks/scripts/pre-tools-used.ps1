# Pre-tool-use safety gate: blocks destructive commands and obvious secret leakage.
# Reads the pending tool invocation from stdin (JSON with a "command"/"tool_input" field,
# or raw text) and exits non-zero to block the tool call.

$raw = ($input | Out-String)
if ([string]::IsNullOrWhiteSpace($raw) -and [Console]::IsInputRedirected) {
    $raw = [Console]::In.ReadToEnd()
}
if ([string]::IsNullOrWhiteSpace($raw)) {
    $raw = $args -join ' '
}

$command = $raw
try {
    $json = $raw | ConvertFrom-Json -ErrorAction Stop
    $command = @($json.command, $json.tool_input, $json.input) -join ' '
} catch {
    # not JSON, treat $raw as the literal command text
}

$destructivePatterns = @(
    'rm\s+-rf',
    'git\s+push\s+.*--force',
    'git\s+reset\s+--hard',
    'drop\s+table',
    'drop\s+database',
    'truncate\s+table',
    '--no-verify',
    'Remove-Item\s+.*-Recurse\s+.*-Force'
)

$secretPatterns = @(
    '(?i)api[_-]?key\s*[:=]\s*[''"][^''"]{8,}',
    '(?i)secret\s*[:=]\s*[''"][^''"]{8,}',
    '(?i)aws_(access_key_id|secret_access_key)\s*[:=]',
    '-----BEGIN (RSA|EC|OPENSSH|PRIVATE) KEY-----',
    '\bcat\s+.*\.env\b',
    '\btype\s+.*\.env\b'
)

foreach ($pattern in $destructivePatterns) {
    if ($command -match $pattern) {
        Write-Error "BLOCKED: command matches destructive pattern '$pattern'."
        exit 1
    }
}

foreach ($pattern in $secretPatterns) {
    if ($command -match $pattern) {
        Write-Error "BLOCKED: command appears to expose a secret (pattern '$pattern')."
        exit 1
    }
}

exit 0
