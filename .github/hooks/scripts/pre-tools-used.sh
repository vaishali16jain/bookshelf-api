#!/usr/bin/env bash
# Pre-tool-use safety gate: blocks destructive commands and obvious secret leakage.
# Reads the pending tool invocation from stdin (JSON with a "command"/"tool_input" field,
# or raw text) and exits non-zero to block the tool call.
set -euo pipefail

raw="$(cat || true)"
if [ -z "$raw" ]; then
  raw="$*"
fi

command_text="$raw"
if command -v jq >/dev/null 2>&1 && echo "$raw" | jq -e . >/dev/null 2>&1; then
  command_text="$(echo "$raw" | jq -r '[.command, .tool_input, .input] | map(select(. != null)) | join(" ")')"
fi

destructive_patterns=(
  'rm +-rf'
  'git +push +.*--force'
  'git +reset +--hard'
  'drop +table'
  'drop +database'
  'truncate +table'
  '\-\-no-verify'
)

secret_patterns=(
  '(api[_-]?key|secret)[[:space:]]*[:=][[:space:]]*["'\''][^"'\'']{8,}'
  'aws_(access_key_id|secret_access_key)[[:space:]]*[:=]'
  '-----BEGIN (RSA|EC|OPENSSH|PRIVATE) KEY-----'
  'cat +.*\.env'
)

for pattern in "${destructive_patterns[@]}"; do
  if echo "$command_text" | grep -Eiq "$pattern"; then
    echo "BLOCKED: command matches destructive pattern '$pattern'." >&2
    exit 1
  fi
done

for pattern in "${secret_patterns[@]}"; do
  if echo "$command_text" | grep -Eiq "$pattern"; then
    echo "BLOCKED: command appears to expose a secret (pattern '$pattern')." >&2
    exit 1
  fi
done

exit 0
