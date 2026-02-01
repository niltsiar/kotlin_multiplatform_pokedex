#!/bin/bash
# Delta File Validation Script
# Non-blocking validation - reports only

echo "Delta File Validation"
echo "===================="
echo ""

errors=0
for file in docs/agent-prompts/*DELTA.md; do
    if [ -f "$file" ]; then
        if grep -q "Include Base Agent Prompt" "$file"; then
            echo "✅ $file"
        else
            echo "⚠️  $file missing required header"
            errors=$((errors + 1))
        fi
    fi
done

echo ""
if [ $errors -eq 0 ]; then
    echo "✅ All DELTA files validated successfully"
    exit 0
else
    echo "⚠️  $errors file(s) missing headers (non-blocking)"
    exit 0  # Non-blocking
fi
