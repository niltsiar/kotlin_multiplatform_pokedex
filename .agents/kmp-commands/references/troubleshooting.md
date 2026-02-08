# Troubleshooting: Command and Git Issues

Common CLI command, git commit, and workflow problems.

---

## Commit Message Too Long for PTY

**Symptom:**
```
pty is gonna break
```

**Cause:** Verbose commit message with detailed explanations exceeds terminal buffer.

**Solution:** Use concise commit format:
```bash
git commit -m "feat(unstyled): complete Step 7 with navigation fixes

- Fixed 15 compilation errors in 5 waves
- Added .clickable() modifier for navigation
- Enhanced hover effects (brightness 1.15, border 0.5, scale 1.02)

Result: BUILD SUCCESSFUL, 84 tests passing"
```

**Guidelines:**
- Subject line: 72 chars max
- Body: Bulleted summary, not prose
- Omit implementation details (keep in code comments)

---

## Quick Diagnosis Checklist

**Import errors on standard libs?** → Clean build

**"Unresolved reference" on custom type?** → Check naming convention

**Component not clickable?** → Add `.clickable()` modifier

**Hover effect not visible?** → Increase effect values (1.15, 0.5, 1.02)

**Constructor errors?** → Verify domain class in `:api` module

**Theme token not found?** → Check import source (custom vs platform)

**Tests pass but tasks fail?** → Run explicit test verification
