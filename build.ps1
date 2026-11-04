param([ValidateSet('demo', 'extension', 'test', 'compile')][string]$Task = 'demo')
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force build/classes | Out-Null
    $sources = @(Get-ChildItem src/main/java, src/test/java -Recurse -Filter '*.java' |
        ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
    if ($Task -eq 'compile') {
        Write-Output 'Compilation successful. Classes are in build/classes.'
        return
    }
    $mainClass = switch ($Task) {
        'test' { 'filesystem.VisitorTest' }
        'extension' { 'filesystem.ExtensionDemo' }
        default { 'filesystem.Main' }
    }
    & java -cp build/classes $mainClass
    if ($LASTEXITCODE -ne 0) { throw "Java $Task failed." }
} finally {
    Pop-Location
}
