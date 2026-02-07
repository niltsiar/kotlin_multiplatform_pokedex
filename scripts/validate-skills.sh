#!/usr/bin/env bash
set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Expected skills (27 total)
EXPECTED_SKILLS=(
  # Architecture & Core (2)
  "kmp-architecture"
  "kmp-critical-patterns"
  # Layer Implementation (5)
  "kmp-presentation"
  "kmp-data-layer"
  "kmp-domain"
  "kmp-api-services"
  "kmp-di"
  # Platform & UI (5)
  "kmp-ios"
  "swiftui-screen"
  "compose-screen"
  "kmp-navigation"
  "kmp-desktop"
  # Design & Testing (5)
  "kmp-design-systems"
  "kmp-compose-unstyled"
  "ui-ux-designer"
  "kmp-testing-strategy"
  "kmp-testing-patterns"
  # Build & Commands (2)
  "kmp-gradle"
  "kmp-commands"
  # Development & Quality (7)
  "kmp-developer"
  "kmp-mobile-expert"
  "ktor-backend"
  "product-designer"
  "user-flows"
  "onboarding"
  "docs-maintainer"
)

SKILLS_DIR=".claude/skills"
ERRORS=0

echo "🔍 Validating skill inventory..."
echo ""

# Check 1: All expected skills exist
echo "1️⃣  Checking skill existence..."
for skill in "${EXPECTED_SKILLS[@]}"; do
  skill_file="$SKILLS_DIR/$skill/SKILL.md"
  if [ ! -f "$skill_file" ]; then
    echo -e "${RED}❌ Missing skill: $skill${NC}"
    ((ERRORS++))
  else
    echo -e "${GREEN}✓${NC} $skill"
  fi
done
echo ""

# Check 2: YAML frontmatter validation
echo "2️⃣  Validating YAML frontmatter..."
for skill in "${EXPECTED_SKILLS[@]}"; do
  skill_file="$SKILLS_DIR/$skill/SKILL.md"
  if [ -f "$skill_file" ]; then
    # Check for opening ---
    if ! head -n 1 "$skill_file" | grep -q "^---$"; then
      echo -e "${RED}❌ $skill: Missing opening --- in frontmatter${NC}"
      ((ERRORS++))
      continue
    fi
    
    # Check for name field
    if ! head -n 10 "$skill_file" | grep -q "^name:"; then
      echo -e "${RED}❌ $skill: Missing 'name' field in frontmatter${NC}"
      ((ERRORS++))
    fi
    
    # Check for description field
    if ! head -n 10 "$skill_file" | grep -q "^description:"; then
      echo -e "${RED}❌ $skill: Missing 'description' field in frontmatter${NC}"
      ((ERRORS++))
    fi
    
    # Check for closing --- (within first 10 lines, after opening)
    if ! head -n 10 "$skill_file" | tail -n 9 | grep -q "^---$"; then
      echo -e "${YELLOW}⚠️  $skill: Possible missing closing --- in frontmatter${NC}"
    fi
  fi
done
echo ""

# Check 3: Required sections
echo "3️⃣  Validating required sections..."
REQUIRED_SECTIONS=("When to Use" "Essential Workflows" "Critical Guardrails" "Quick Reference" "Cross-References")
for skill in "${EXPECTED_SKILLS[@]}"; do
  skill_file="$SKILLS_DIR/$skill/SKILL.md"
  if [ -f "$skill_file" ]; then
    for section in "${REQUIRED_SECTIONS[@]}"; do
      if ! grep -q "## $section" "$skill_file"; then
        echo -e "${RED}❌ $skill: Missing section '## $section'${NC}"
        ((ERRORS++))
      fi
    done
  fi
done
echo ""

# Check 4: File size validation (convexskills pattern: <300 lines target, <500 max)
echo "4️⃣  Checking file sizes (target <300 lines, max <500)..."
for skill in "${EXPECTED_SKILLS[@]}"; do
  skill_file="$SKILLS_DIR/$skill/SKILL.md"
  if [ -f "$skill_file" ]; then
    lines=$(wc -l < "$skill_file")
    if [ "$lines" -gt 500 ]; then
      echo -e "${RED}❌ $skill: $lines lines (exceeds 500 line maximum)${NC}"
      ((ERRORS++))
    elif [ "$lines" -gt 300 ]; then
      echo -e "${YELLOW}⚠️  $skill: $lines lines (exceeds 300 line target but within 500 max)${NC}"
    else
      echo -e "${GREEN}✓${NC} $skill: $lines lines"
    fi
  fi
done
echo ""

# Check 5: Cross-reference link validation
echo "5️⃣  Validating cross-reference links..."
for skill in "${EXPECTED_SKILLS[@]}"; do
  skill_file="$SKILLS_DIR/$skill/SKILL.md"
  if [ -f "$skill_file" ]; then
    # Find all @skill-name references
    grep -oE '@[a-z-]+' "$skill_file" 2>/dev/null | sort -u | while read -r ref; do
      ref_skill="${ref#@}"  # Remove @ prefix
      ref_file="$SKILLS_DIR/$ref_skill/SKILL.md"
      if [ ! -f "$ref_file" ]; then
        echo -e "${RED}❌ $skill: Broken reference to $ref (file not found: $ref_file)${NC}"
        ((ERRORS++))
      fi
    done
    
    # Find all relative ../skill-name/SKILL.md links
    grep -oE '\.\./[a-z-]+/SKILL\.md' "$skill_file" 2>/dev/null | while read -r link; do
      # Extract skill name from ../skill-name/SKILL.md
      link_skill=$(echo "$link" | sed 's|\.\./\([^/]*\)/SKILL\.md|\1|')
      link_file="$SKILLS_DIR/$link_skill/SKILL.md"
      if [ ! -f "$link_file" ]; then
        echo -e "${RED}❌ $skill: Broken link to $link (file not found: $link_file)${NC}"
        ((ERRORS++))
      fi
    done
  fi
done
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $ERRORS -eq 0 ]; then
  echo -e "${GREEN}✅ All validations passed!${NC}"
  echo "   - 27 skills exist"
  echo "   - All frontmatter valid"
  echo "   - All required sections present"
  echo "   - File sizes within limits"
  echo "   - All cross-references valid"
  exit 0
else
  echo -e "${RED}❌ Validation failed with $ERRORS error(s)${NC}"
  echo "   Fix the errors above and run again."
  exit 1
fi
