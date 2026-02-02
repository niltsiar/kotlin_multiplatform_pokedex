# Testing Plan - Agentic Documentation System

## Overview
Comprehensive testing strategy to verify the complete agentic documentation overhaul is working correctly.

---

## Test 1: Skills Structure Validation

### 1.1 Verify All Skills Exist
```bash
# Count skills
ls .claude/skills/*/SKILL.md | wc -l
# Expected: 11

# List all skills
ls .claude/skills/
# Expected: compose-screen, docs-maintainer, kmp-developer, 
#           kmp-mobile-expert, ktor-backend, onboarding, 
#           product-designer, swiftui-screen, testing-strategy, 
#           ui-ux-designer, user-flows
```

### 1.2 Verify Skill Format
```bash
# Check each skill has YAML frontmatter
for skill in .claude/skills/*/SKILL.md; do
    echo "Checking $skill..."
    head -5 "$skill" | grep -q "^---$" && echo "✅ Has frontmatter" || echo "❌ Missing frontmatter"
done

# Check line counts (all should be ≤ 500)
for skill in .claude/skills/*/SKILL.md; do
    lines=$(wc -l < "$skill")
    if [ "$lines" -gt 500 ]; then
        echo "❌ $skill: $lines lines (exceeds 500)"
    else
        echo "✅ $skill: $lines lines"
    fi
done
```

### 1.3 Verify Required Sections
```bash
# Check all skills have required sections
for skill in .claude/skills/*/SKILL.md; do
    echo ""
    echo "=== $skill ==="
    grep -q "^## When to Use" "$skill" && echo "✅ When to Use" || echo "❌ Missing When to Use"
    grep -q "^## Essential Workflows" "$skill" && echo "✅ Essential Workflows" || echo "❌ Missing Workflows"
    grep -q "^## Critical Guardrails" "$skill" && echo "✅ Critical Guardrails" || echo "❌ Missing Guardrails"
    grep -q "^## Quick Reference" "$skill" && echo "✅ Quick Reference" || echo "❌ Missing Quick Reference"
    grep -q "^## Cross-References" "$skill" && echo "✅ Cross-References" || echo "❌ Missing Cross-References"
done
```

---

## Test 2: No Broken References

### 2.1 Check for Deleted File References
```bash
# Search for references to deleted files
echo "Checking for deleted file references..."
rg "copilot-instructions|junie/guidelines|agent-prompts" .claude/skills/ --type md | grep -v "Legacy path check" | grep -v "should return no matches"

# Expected: No output (or only the grep check command)
```

### 2.2 Validate Internal Links
```bash
# Check relative links in skills
for skill in .claude/skills/*/SKILL.md; do
    echo ""
    echo "=== Checking links in $skill ==="
    # Extract all relative links
    grep -oE '\[.*\]\((\.\./|\.\/)[^)]+\)' "$skill" | while read link; do
        # Extract path from markdown link
        path=$(echo "$link" | sed 's/.*](\(.*\))/\1/')
        # Resolve relative to absolute
        skill_dir=$(dirname "$skill")
        full_path="$skill_dir/$path"
        if [ -e "$full_path" ]; then
            echo "✅ $path exists"
        else
            echo "❌ $path NOT FOUND"
        fi
    done
done
```

### 2.3 Check Cross-References to docs/
```bash
# Verify docs/ references exist
for skill in .claude/skills/*/SKILL.md; do
    echo ""
    echo "=== $skill ==="
    grep -oE 'docs/[^)]+\.md' "$skill" | sort -u | while read doc; do
        if [ -f "$doc" ]; then
            echo "✅ $doc exists"
        else
            echo "❌ $doc NOT FOUND"
        fi
    done
done
```

---

## Test 3: AGENTS.md Validation

### 3.1 Structure Check
```bash
# Verify AGENTS.md exists and has correct structure
echo "=== AGENTS.md Validation ==="

# Check file exists
[ -f AGENTS.md ] && echo "✅ AGENTS.md exists" || echo "❌ AGENTS.md missing"

# Check line count
lines=$(wc -l < AGENTS.md)
if [ "$lines" -lt 200 ]; then
    echo "✅ AGENTS.md: $lines lines (under 200)"
else
    echo "❌ AGENTS.md: $lines lines (exceeds 200)"
fi

# Check required sections
grep -q "^## Skills" AGENTS.md && echo "✅ Has Skills section" || echo "❌ Missing Skills section"
grep -q "^## Skill Selection Guide" AGENTS.md && echo "✅ Has Selection Guide" || echo "❌ Missing Selection Guide"
grep -q "^## Critical Patterns" AGENTS.md && echo "✅ Has Critical Patterns" || echo "❌ Missing Critical Patterns"
grep -q "^## Essential Commands" AGENTS.md && echo "✅ Has Commands" || echo "❌ Missing Commands"
```

### 3.2 Verify All 11 Skills Listed
```bash
# Check all skills are mentioned in AGENTS.md
echo ""
echo "=== Checking skills in AGENTS.md ==="
for skill in kmp-developer kmp-mobile-expert compose-screen swiftui-screen ktor-backend product-designer ui-ux-designer onboarding user-flows testing-strategy docs-maintainer; do
    if grep -q "$skill" AGENTS.md; then
        echo "✅ $skill listed"
    else
        echo "❌ $skill NOT listed"
    fi
done
```

---

## Test 4: Scripts Validation

### 4.1 Syntax Check
```bash
# Validate all scripts have correct syntax
echo "=== Script Syntax Validation ==="
for script in .claude/skills/*/scripts/*.sh; do
    if [ -f "$script" ]; then
        if bash -n "$script" 2>/dev/null; then
            echo "✅ $script - syntax valid"
        else
            echo "❌ $script - syntax error"
        fi
    fi
done
```

### 4.2 Executable Check
```bash
# Verify scripts are executable
echo ""
echo "=== Script Permissions ==="
for script in .claude/skills/*/scripts/*.sh; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script is executable"
        else
            echo "❌ $script not executable"
        fi
    fi
done
```

### 4.3 Test Script Execution (Dry Run)
```bash
# Test scripts with --help or dry-run where possible
echo ""
echo "=== Script Execution Tests ==="

# Test validate-links.sh (should handle missing markdown-link-check gracefully)
if .claude/skills/docs-maintainer/scripts/validate-links.sh 2>&1 | grep -q "not installed"; then
    echo "✅ validate-links.sh handles missing dependency"
else
    echo "⚠️  validate-links.sh may need markdown-link-check installed"
fi

# Test other scripts with --help or usage
for script in .claude/skills/*/scripts/*.sh; do
    if [ -f "$script" ]; then
        # Check if script has usage/help
        if grep -q "Usage:" "$script"; then
            echo "✅ $script has usage documentation"
        fi
    fi
done
```

---

## Test 5: Build Verification

### 5.1 Gradle Build
```bash
# Verify project still builds
echo "=== Build Test ==="
./gradlew :composeApp:assembleDebug --quiet

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
fi
```

### 5.2 Test Execution
```bash
# Run tests
echo ""
echo "=== Test Execution ==="
./gradlew test --continue --quiet

if [ $? -eq 0 ]; then
    echo "✅ Tests pass"
else
    echo "❌ Tests failed"
fi
```

---

## Test 6: Legacy Files Cleanup

### 6.1 Verify Deleted Files Gone
```bash
# Check legacy files are deleted
echo "=== Legacy Files Check ==="
files=(
    ".github/copilot-instructions.md"
    ".junie/guidelines.md"
    "docs/agent-prompts"
    ".opencode"
)

for file in "${files[@]}"; do
    if [ -e "$file" ]; then
        echo "❌ $file still exists (should be deleted)"
    else
        echo "✅ $file deleted"
    fi
done
```

### 6.2 Verify New Structure Exists
```bash
# Check new structure is in place
echo ""
echo "=== New Structure Check ==="
[ -d ".claude/skills" ] && echo "✅ .claude/skills/ exists" || echo "❌ .claude/skills/ missing"
[ -f "AGENTS.md" ] && echo "✅ AGENTS.md exists" || echo "❌ AGENTS.md missing"
[ -f "llms.txt" ] && echo "✅ llms.txt exists" || echo "❌ llms.txt missing"
```

---

## Test 7: Pre-commit Hooks (Optional)

### 7.1 Verify Pre-commit Config
```bash
# Check pre-commit config exists and is valid
echo "=== Pre-commit Configuration ==="
if [ -f ".pre-commit-config.yaml" ]; then
    echo "✅ .pre-commit-config.yaml exists"
    
    # Basic YAML validation (check for syntax errors)
    if python3 -c "import yaml; yaml.safe_load(open('.pre-commit-config.yaml'))" 2>/dev/null; then
        echo "✅ YAML syntax valid"
    else
        echo "❌ YAML syntax error"
    fi
else
    echo "⚠️  .pre-commit-config.yaml not found (optional)"
fi
```

---

## Test 8: Integration Test - Skill Usage Simulation

### 8.1 Simulate Skill Invocation
```bash
# Test that skills can be found and read
echo "=== Skill Integration Test ==="

# Simulate what Claude Code would do - find and read a skill
skill=".claude/skills/kmp-developer/SKILL.md"
if [ -f "$skill" ]; then
    echo "✅ Can access skill: $skill"
    
    # Extract and display key info (simulating what AI would see)
    echo ""
    echo "Skill metadata:"
    head -10 "$skill"
    
    echo ""
    echo "When to use:"
    grep -A 5 "^## When to Use" "$skill" | head -10
else
    echo "❌ Cannot access skill: $skill"
fi
```

---

## Automated Test Script

Create this as `test-agentic-system.sh`:

```bash
#!/bin/bash
set -e

echo "=========================================="
echo "Agentic Documentation System Test Suite"
echo "=========================================="
echo ""

# Test 1: Skills count
echo "Test 1: Skills Count"
count=$(ls .claude/skills/*/SKILL.md 2>/dev/null | wc -l)
if [ "$count" -eq 11 ]; then
    echo "✅ PASS: Found $count skills"
else
    echo "❌ FAIL: Expected 11 skills, found $count"
    exit 1
fi

# Test 2: AGENTS.md exists
echo ""
echo "Test 2: AGENTS.md"
if [ -f AGENTS.md ]; then
    lines=$(wc -l < AGENTS.md)
    if [ "$lines" -lt 200 ]; then
        echo "✅ PASS: AGENTS.md exists with $lines lines"
    else
        echo "❌ FAIL: AGENTS.md has $lines lines (exceeds 200)"
        exit 1
    fi
else
    echo "❌ FAIL: AGENTS.md not found"
    exit 1
fi

# Test 3: No legacy files
echo ""
echo "Test 3: Legacy Files Deleted"
legacy_found=false
for file in .github/copilot-instructions.md .junie/guidelines.md docs/agent-prompts .opencode; do
    if [ -e "$file" ]; then
        echo "❌ FAIL: $file still exists"
        legacy_found=true
    fi
done

if [ "$legacy_found" = false ]; then
    echo "✅ PASS: All legacy files deleted"
else
    exit 1
fi

# Test 4: Build test
echo ""
echo "Test 4: Build"
if ./gradlew :composeApp:assembleDebug --quiet 2>/dev/null; then
    echo "✅ PASS: Build successful"
else
    echo "❌ FAIL: Build failed"
    exit 1
fi

# Test 5: No broken references
echo ""
echo "Test 5: Broken References"
broken=$(grep -r "copilot-instructions\|junie/guidelines\|agent-prompts" .claude/skills/ --type md 2>/dev/null | grep -v "Legacy path check" | grep -v "should return no matches" | wc -l)
if [ "$broken" -eq 0 ]; then
    echo "✅ PASS: No broken references found"
else
    echo "❌ FAIL: Found $broken broken references"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ ALL TESTS PASSED"
echo "=========================================="
```

---

## How to Run Tests

### Quick Test (5 minutes)
```bash
# Run basic validation
ls .claude/skills/*/SKILL.md | wc -l  # Should be 11
wc -l AGENTS.md  # Should be < 200
./gradlew :composeApp:assembleDebug test --continue  # Should pass
```

### Full Test Suite (10-15 minutes)
```bash
# Make test script executable and run
chmod +x test-agentic-system.sh
./test-agentic-system.sh
```

### Manual Testing
```bash
# Test individual components
bash -n .claude/skills/*/scripts/*.sh  # Syntax check scripts
markdown-link-check AGENTS.md  # Check links (if installed)
rg "copilot-instructions" .claude/skills/  # Should find nothing
```

---

## Success Criteria

✅ **All tests pass**:
- 11 skills exist and are properly formatted
- AGENTS.md is under 200 lines
- No references to deleted files
- Build passes
- Tests pass
- All scripts are valid

**If any test fails**, the system needs fixes before it's production-ready.
