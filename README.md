# WaterShare Android prototype

Kotlin + Jetpack Compose prototype. It is not yet validated for financial use: compare its calculations with the supplied Excel workbooks before relying on bill amounts.

## Build in GitHub Actions (no local Android Studio required)
1. Create a GitHub repository and upload the contents of this folder.
2. Open the repository's **Actions** tab.
3. Run **Build Android APK** (or push to `main`).
4. Open the completed workflow run, scroll to **Artifacts**, and download `WaterShare-debug-apk`.
5. Extract the ZIP artifact; it contains `app-debug.apk`.

The workflow builds a debug APK. It is suitable for testing, not a Play Store release.
