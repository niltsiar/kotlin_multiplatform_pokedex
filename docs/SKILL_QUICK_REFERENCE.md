# Skill Quick Reference Card

Last Updated: February 2, 2026

## 🚀 One-Page Cheat Sheet for KMP Skills

### When to Use Which Skill

| Task | Skill | Command |
|------|-------|---------|
| **Implement a feature** | kmp-developer | `@kmp-developer help me implement...` |
| **Fix a bug** | kmp-developer | `@kmp-developer fix bug in...` |
| **Create ViewModel** | kmp-mobile-expert | `@kmp-mobile-expert create ViewModel for...` |
| **Build repository** | kmp-mobile-expert | `@kmp-mobile-expert implement repository...` |
| **iOS export question** | kmp-mobile-expert | `@kmp-mobile-expert how to export to iOS...` |
| **Compose screen** | compose-screen | `@compose-screen implement Pokemon detail screen` |
| **Add @Preview** | compose-screen | `@compose-screen add preview to...` |

### Quick Validation

```bash
# Always run before finishing
./gradlew :composeApp:assembleDebug test --continue
```

### Critical Patterns (Never Forget)

1. **ViewModel**: Pass scope, NO work in init
2. **Repository**: Return `Either<RepoError, T>`
3. **Compose**: Always add @Preview
4. **iOS**: Only export `:api` and `:presentation`

### File Locations

- **Skills**: `.claude/skills/<skill-name>/SKILL.md`
- **Validation**: `./.claude/skills/docs-maintainer/scripts/validate-links.sh`
- **Token Check**: `python3 scripts/check-tokens.py`
- **Full Guide**: `docs/SKILL_USAGE.md`

### Getting Help

1. **Quick help**: Ask the skill directly
2. **Full guide**: Read `docs/SKILL_USAGE.md`
3. **Patterns**: Check `docs/tech/critical_patterns_compact.md`
4. **Architecture**: See `docs/tech/conventions.md`

---

**Print this page and keep it handy!** 📋
