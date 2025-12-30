# Download Icons - Step by Step

**Style:** Rounded Filled (default weight)

## Steps

1. Go to: https://fonts.google.com/icons

2. Click "Customize" → Select **"Rounded"** style

3. Download these 5 icons (search → select → Android tab → Download):
   - `arrow_back` → rename to `ic_arrow_back.xml`
   - `error` → rename to `ic_error_outline.xml`
   - `refresh` → rename to `ic_refresh.xml`
   - `settings` → rename to `ic_settings.xml`
   - `info` → rename to `ic_info.xml`

4. Edit `ic_arrow_back.xml` - add this attribute:
   ```xml
   <vector ... android:autoMirrored="true">
   ```

5. Move all 5 files to:
   ```
   core/designsystem-core/src/commonMain/composeResources/drawable/
   ```

## Verify

```bash
ls core/designsystem-core/src/commonMain/composeResources/drawable/ic_*.xml
```

Should show 5 files.
