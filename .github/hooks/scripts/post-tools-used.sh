#!/usr/bin/env bash
# Post-tool-use formatter: routes the created/edited file to the right formatter by extension.
set -euo pipefail

path="${1:-${createdFilePath:-}}"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z "$path" ] || [ ! -f "$path" ]; then
  exit 0
fi

case "$path" in
  *.java)
    mvn -q -f "$script_dir/../../../pom.xml" com.diffplug.spotless:spotless-maven-plugin:apply
    ;;
  *.js|*.html|*.css|*.md|*.json)
    npx prettier --write "$path"
    ;;
  *)
    ;; # no formatter for this file type
esac

exit 0
