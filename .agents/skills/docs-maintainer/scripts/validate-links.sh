#!/bin/bash
# Validates all markdown links in documentation
# Usage: ./validate-links.sh
#
# Note: Uses 'find' instead of glob patterns for cross-shell compatibility.
# Bash 3.2 (macOS default) doesn't support '**' globstar, while zsh does.
# Using find ensures consistent behavior across bash/zsh versions.

if ! command -v markdown-link-check &> /dev/null; then
    echo "⚠️  markdown-link-check not installed"
    echo "Install with: npm install -g markdown-link-check"
    exit 0
fi

echo "🔍 Validating documentation links..."

# Find all markdown files recursively (cross-shell compatible)
# Includes: docs/, .agents/skills/, and root-level AGENTS.md + llms.txt
FILES=$(find docs .agents -name "*.md" -type f 2>/dev/null; echo "AGENTS.md"; echo "llms.txt")

# Validate all files
echo "$FILES" | xargs markdown-link-check || exit 1

echo "✅ All links validated successfully!"
