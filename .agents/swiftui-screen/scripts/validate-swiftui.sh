#!/bin/bash
# Validates SwiftUI screen integrates properly with KMP
# Usage: ./validate-swiftui.sh <feature-name>

FEATURE=$1
[ -z "$FEATURE" ] && echo "Usage: ./validate-swiftui.sh <feature-name>" && exit 1

ERRORS=0
grep -r "shared\." "iosApp/iosApp/Features/$FEATURE/" 2>/dev/null || echo "❌ No shared framework usage" && ERRORS=$((ERRORS + 1))
grep -r "@StateObject" "iosApp/iosApp/Features/$FEATURE/" 2>/dev/null || echo "⚠️  No @StateObject"
[ $ERRORS -eq 0 ] && echo "✅ SwiftUI valid" && exit 0 || exit 1
