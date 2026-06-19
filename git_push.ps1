# Auto-commit and Push Script for VaadinERP
# Usage: 
#   .\git_push.ps1
#   .\git_push.ps1 "My custom commit message"

$message = "Auto-commit on $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
if ($args.Count -gt 0) {
    $message = $args[0]
}

# Ensure Git is initialized
if (!(Test-Path .git)) {
    Write-Host "Error: Git is not initialized in this folder. Please run 'git init' first." -ForegroundColor Red
    exit 1
}

# Check if remote origin is set
$remote = git remote get-url origin 2>$null
if ([string]::IsNullOrEmpty($remote)) {
    Write-Host "Warning: Remote 'origin' is not set. The script will commit locally but cannot push." -ForegroundColor Yellow
}

Write-Host "Step 1: Staging all changes..." -ForegroundColor Cyan
git add .

Write-Host "Step 2: Committing changes with message: '$message'..." -ForegroundColor Cyan
git commit -m $message

if (![string]::IsNullOrEmpty($remote)) {
    Write-Host "Step 3: Pushing to GitHub (origin main)..." -ForegroundColor Cyan
    # Get current branch name
    $branch = git branch --show-current
    if ([string]::IsNullOrEmpty($branch)) {
        $branch = "main"
    }
    git push origin $branch
}

Write-Host "Done!" -ForegroundColor Green
