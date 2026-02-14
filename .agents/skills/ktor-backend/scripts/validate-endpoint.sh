#!/bin/bash
# Validates Ktor endpoint follows API conventions
# Usage: ./validate-endpoint.sh <path-to-routing-file>

FILE=$1
[ -z "$FILE" ] || [ ! -f "$FILE" ] && echo "Usage: ./validate-endpoint.sh <file>" && exit 1

ERRORS=0
if ! grep -q "route(" "$FILE"; then
    echo "❌ No route grouping"
    ERRORS=$((ERRORS + 1))
fi
if ! grep -q "call.respond\|respondText\|respondJson" "$FILE"; then
    echo "❌ No response handling"
    ERRORS=$((ERRORS + 1))
fi
[ $ERRORS -eq 0 ] && echo "✅ Endpoint valid" && exit 0 || exit 1
