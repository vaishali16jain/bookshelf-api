param(
    [string]$Path
)

# Post-tool-use formatter: routes the created/edited file to the right formatter by extension.
if (-not $Path) {
    $Path = $env:createdFilePath
}

if (-not $Path -or -not (Test-Path $Path)) {
    exit 0
}

$ext = [System.IO.Path]::GetExtension($Path).ToLowerInvariant()

switch ($ext) {
    '.java' {
        mvn -q -f (Join-Path $PSScriptRoot '..\..\..\pom.xml') com.diffplug.spotless:spotless-maven-plugin:apply
    }
    { $_ -in '.js', '.html', '.css', '.md', '.json' } {
        npx prettier --write $Path
    }
    default {
        # no formatter for this file type
    }
}

exit 0
