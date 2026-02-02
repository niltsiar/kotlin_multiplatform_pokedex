#!/bin/bash
# Validates all markdown links in documentation
# Usage: ./validate-links.sh

if ! command -v markdown-link-check &> /dev/null; then
    echo "⚠️  markdown-link-check not installed"
    echo "Install with: npm install -g markdown-link-check"
    exit 0
fi

echo "🔍 Validating documentation links..."

# Check all markdown files in docs/, skills, and root-level docs
markdown-link-check docs/**/*.md AGENTS.md llms.txt .claude/skills/**/*.md || exit 1

echo "✅ All links validated successfully!"
