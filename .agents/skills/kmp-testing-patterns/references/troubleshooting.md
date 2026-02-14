# Troubleshooting: Testing Issues

Common test execution and validation problems.

---

## Tests Pass But Build Shows Failures

**Symptom:**
```
> Task :features:pokemonlist:wiring-ui-unstyled:compileDebugKotlinAndroid FAILED
BUILD SUCCESSFUL in 1m 23s
All 114 tests PASSED
```

**Cause:** `--continue` flag allows tests to run despite task failures.

**Interpretation:**
- Task failures shown are from earlier in build
- Tests actually passed (verify with explicit test run)
- Subsequent clean build resolves stale task states

**Solution:** Run explicit test verification:
```bash
./gradlew test --rerun-tasks
```

---

## Before Implementing Features

1. **Verify compilation early:** Build immediately after scaffolding
2. **Check domain classes:** Verify constructor parameters first
3. **Copy working patterns:** Use existing implementations as reference
4. **Test incrementally:** Build after each component
