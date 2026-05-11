# Sync with upstream script for racecoordinator_ai
$ErrorActionPreference = "Stop"

Write-Host "Fetching from upstream..." -ForegroundColor Cyan
git fetch upstream

$behindCount = (git log --oneline HEAD..upstream/main | Measure-Object).Count
if ($behindCount -eq 0) {
    Write-Host "Already up to date with upstream/main." -ForegroundColor Green
    exit 0
}

Write-Host "Your branch is behind by $behindCount commits. Starting rebase..." -ForegroundColor Yellow
git rebase upstream/main

# Check if rebase is still in progress (conflicts)
if (Test-Path ".git/rebase-merge") {
    Write-Host "Conflicts detected! Please resolve them manually and run 'git rebase --continue'." -ForegroundColor Red
    exit 1
}

Write-Host "Rebase successful. Regenerating client protos..." -ForegroundColor Cyan
Set-Location client
powershell -ExecutionPolicy Bypass -File gen_protos_client.ps1
Set-Location ..

Write-Host "Cleaning server proto duplicates..." -ForegroundColor Cyan
Remove-Item -Recurse -Force "server/target_dist/generated-sources/protobuf/java" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "server/target_dist/generated-sources/protobuf/java" | Out-Null
Copy-Item -Recurse -Force "server/generated-sources/protobuf/java/*" "server/target_dist/generated-sources/protobuf/java/"

Write-Host "Sync complete. Running a quick server compile to verify..." -ForegroundColor Cyan
$env:JAVA_HOME = "$PWD/tools/jdk/jdk-21.0.3+9"
$env:Path = "$PWD/tools/jdk/jdk-21.0.3+9/bin;$env:Path"
.\tools\maven\apache-maven-3.9.6\bin\mvn.cmd compile -Dcheckstyle.skip=true -DskipProtobuf=true

Write-Host "SUCCESS: Fork is in sync and verified." -ForegroundColor Green
