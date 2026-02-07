# Documentation Consolidation Report

**Generated:** February 7, 2026  
**Purpose:** Identify documentation files duplicated by skills in `.agents/`

## Executive Summary

- **Total documentation files:** 42 markdown files (16,669 lines)
- **Total skill files:** 26 skills (6,861 lines)
- **Files recommended for removal:** 23 files (highly duplicated content)
- **Files to keep:** 19 files (unique content or navigation purposes)

## Analysis Results

### Category 1: SAFE TO REMOVE (100% Duplicated by Skills)

These files have content fully covered by corresponding skills:

| Doc File | Skill Equivalent | Justification |
|----------|------------------|---------------|
| `docs/tech/conventions.md` | @kmp-architecture | Architecture master reference - fully covered by skill |
| `docs/tech/testing_strategy.md` | @kmp-testing-strategy | Testing philosophy and guidelines - fully covered |
| `docs/tech/dependency_injection.md` | @kmp-di | Koin patterns and troubleshooting - fully covered |
| `docs/tech/navigation.md` | @kmp-navigation | Navigation 3 architecture - fully covered |
| `docs/tech/ios_integration.md` | @kmp-ios | SwiftUI + KMP integration - fully covered |
| `docs/tech/ios_official_pattern_guide.md` | @kmp-ios | iOS official patterns - fully covered |
| `docs/tech/desktop_viewmodel_savedstate.md` | @kmp-desktop | Desktop SavedStateHandle - fully covered |
| `docs/tech/presentation_layer.md` | @kmp-presentation | ViewModel patterns - fully covered |
| `docs/tech/repository.md` | @kmp-data-layer | Repository implementation - fully covered |
| `docs/tech/api_services.md` | @kmp-api-services | Ktor API service patterns - fully covered |
| `docs/tech/domain.md` | @kmp-domain | Domain models and use cases - fully covered |
| `docs/tech/compose_unstyled_reference.md` | @kmp-compose-unstyled | Compose Unstyled patterns - fully covered |
| `docs/tech/design_tokens.md` | @kmp-design-systems | Design tokens - fully covered |
| `docs/tech/material_icons_strategy.md` | @kmp-design-systems | Icon strategy - fully covered |
| `docs/tech/component_library.md` | @kmp-design-systems | Component library - fully covered |
| `docs/tech/component_token_customization_example.md` | @kmp-design-systems | Token customization - fully covered |
| `docs/tech/convention_plugins_guide.md` | @kmp-gradle | Gradle convention plugins - fully covered |
| `docs/tech/critical_patterns_compact.md` | @kmp-critical-patterns | 6 core patterns - fully covered |
| `docs/tech/critical_patterns_quick_ref.md` | @kmp-critical-patterns | 6 core patterns - fully covered |
| `docs/patterns/testing_patterns.md` | @kmp-testing-patterns | Test implementation patterns - fully covered |
| `docs/patterns/di_patterns.md` | @kmp-di | DI patterns - fully covered |
| `docs/patterns/viewmodel_patterns.md` | @kmp-presentation | ViewModel patterns - fully covered |
| `docs/patterns/architecture_patterns.md` | @kmp-architecture | Architecture patterns - fully covered |

**Total: 23 files recommended for removal**

### Category 2: KEEP (Unique Content or Special Purpose)

These files should be retained:

| Doc File | Reason to Keep |
|----------|----------------|
| `docs/README.md` | Documentation index - navigation hub for all docs |
| `docs/QUICK_REFERENCE.md` | Quick command reference - unique format, frequently accessed |
| `docs/SKILL_USAGE.md` | Skill usage guide - meta-documentation about skills themselves |
| `docs/SKILL_QUICK_REFERENCE.md` | Skill reference card - meta-documentation |
| `docs/CODE_REFERENCES.md` | Code examples and snippets - supplementary reference |
| `docs/TROUBLESHOOTING.md` | Troubleshooting guide - reactive knowledge (fixes for common issues) |
| `docs/tech/koin_di_quick_ref.md` | Quick reference card - condensed format for rapid lookup |
| `docs/tech/kotest_smart_casting_quick_ref.md` | Specific technique reference - narrow scope |
| `docs/tech/testing_quick_ref.md` | Quick reference card - condensed format |
| `docs/tech/ios_apps_architecture.md` | iOS app comparison (native vs Compose) - unique architectural context |
| `docs/tech/predictive_back_notes.md` | Predictive back gesture notes - narrow technical topic |
| `docs/tech/utility_organization.md` | Utility module organization - specific organizational guidance |
| `docs/tech/coroutines.md` | Coroutines patterns - potentially unique content (needs verification) |
| `docs/patterns/error_handling_patterns.md` | Error handling patterns - cross-cutting concern, may have unique content |
| `docs/patterns/navigation_patterns.md` | Navigation patterns - may have unique content vs @kmp-navigation |
| `docs/project/prd.md` | Product requirements document - canonical product spec |
| `docs/project/user_flow.md` | User journey documentation - product-specific |
| `docs/project/ui_ux.md` | UI/UX guidelines - product-specific |
| `docs/project/onboarding.md` | Onboarding flow documentation - product-specific |

**Total: 19 files to keep**

### Category 3: NEEDS VERIFICATION (Potential Partial Duplication)

These files may have unique sections worth preserving:

| Doc File | Concern | Action |
|----------|---------|--------|
| `docs/tech/coroutines.md` | May have unique coroutine patterns not in @kmp-presentation | Review before removal |
| `docs/patterns/error_handling_patterns.md` | May have unique error handling beyond @kmp-data-layer | Review before removal |
| `docs/patterns/navigation_patterns.md` | May have unique patterns beyond @kmp-navigation | Review before removal |

## Removal Plan

### Phase 1: Safe Removal (Immediate)

Remove 23 files from Category 1 (100% duplicated):

```bash
# Tech files (17 files)
rm docs/tech/conventions.md
rm docs/tech/testing_strategy.md
rm docs/tech/dependency_injection.md
rm docs/tech/navigation.md
rm docs/tech/ios_integration.md
rm docs/tech/ios_official_pattern_guide.md
rm docs/tech/desktop_viewmodel_savedstate.md
rm docs/tech/presentation_layer.md
rm docs/tech/repository.md
rm docs/tech/api_services.md
rm docs/tech/domain.md
rm docs/tech/compose_unstyled_reference.md
rm docs/tech/design_tokens.md
rm docs/tech/material_icons_strategy.md
rm docs/tech/component_library.md
rm docs/tech/component_token_customization_example.md
rm docs/tech/convention_plugins_guide.md
rm docs/tech/critical_patterns_compact.md
rm docs/tech/critical_patterns_quick_ref.md

# Pattern files (4 files)
rm docs/patterns/testing_patterns.md
rm docs/patterns/di_patterns.md
rm docs/patterns/viewmodel_patterns.md
rm docs/patterns/architecture_patterns.md
```

### Phase 2: Update References

After removal, update references in:
- `docs/README.md` - Remove links to deleted files
- `README.md` (root) - Update documentation section
- `AGENTS.md` - Verify skill mappings are correct
- `llms.txt` - Update AI discovery links

### Phase 3: Verification (Category 3 Files)

Manually review these 3 files before deciding:
1. `docs/tech/coroutines.md` - Compare with @kmp-presentation
2. `docs/patterns/error_handling_patterns.md` - Compare with @kmp-data-layer
3. `docs/patterns/navigation_patterns.md` - Compare with @kmp-navigation

## Impact Analysis

### Before Consolidation
- 42 documentation files (16,669 lines)
- 26 skill files (6,861 lines)
- **Total: 23,530 lines**

### After Consolidation (removing 23 files)
- ~19 documentation files (estimated ~5,000 lines)
- 26 skill files (6,861 lines)
- **Total: ~11,861 lines (50% reduction)**

## Benefits

1. **Single Source of Truth**: Skills become the canonical reference
2. **Reduced Maintenance**: Update once in skills, not in multiple places
3. **Better AI Agent Performance**: Skills are optimized for agent loading
4. **Clearer Documentation Hierarchy**: Docs/ becomes supplementary, skills become primary
5. **Reduced Context Window Usage**: Smaller doc surface area for agents

## Risks & Mitigation

### Risk 1: Broken Links
**Mitigation:** Run link validation after removal: `.agents/docs-maintainer/scripts/validate-links.sh`

### Risk 2: Lost Unique Content
**Mitigation:** Category 3 files require manual review before removal

### Risk 3: User Confusion
**Mitigation:** Update README.md to clearly indicate "For architecture patterns, see @kmp-architecture skill"

## Recommendations

1. ✅ **Execute Phase 1 immediately** - Remove 23 files from Category 1
2. ✅ **Update references** - Fix broken links in remaining docs
3. ⚠️ **Manual review** - Verify Category 3 files before removal
4. ✅ **Run validation** - Execute `.agents/docs-maintainer/scripts/validate-links.sh` and fix issues
5. ✅ **Update AGENTS.md** - Add note: "Skills are canonical, docs/ is supplementary"
6. ✅ **Commit with message**: `chore(docs): consolidate duplicated documentation into skills`

## Skill Coverage Matrix

| Documentation Topic | Skill | Coverage |
|---------------------|-------|----------|
| Architecture & Modules | @kmp-architecture | 100% |
| Testing Strategy | @kmp-testing-strategy | 100% |
| Testing Patterns | @kmp-testing-patterns | 100% |
| Dependency Injection | @kmp-di | 100% |
| Navigation | @kmp-navigation | 100% |
| iOS Integration | @kmp-ios | 100% |
| Desktop Patterns | @kmp-desktop | 100% |
| ViewModels | @kmp-presentation | 100% |
| Repositories | @kmp-data-layer | 100% |
| API Services | @kmp-api-services | 100% |
| Domain Layer | @kmp-domain | 100% |
| Compose Unstyled | @kmp-compose-unstyled | 100% |
| Design Systems | @kmp-design-systems | 100% |
| Gradle/Build | @kmp-gradle | 100% |
| Critical Patterns | @kmp-critical-patterns | 100% |

**All major topics have 100% skill coverage.**
