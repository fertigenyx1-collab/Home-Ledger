# LifeRemind — Personal Life & Payment Reminder Manager

LifeRemind is a personal life and payment reminder manager built with modern Kotlin, Jetpack Compose, Material 3, and Room local persistence.

---

## How to Build APK

### 1. Build Automatically with GitHub Actions

#### To Generate a Debug or Release APK (Artifact):
1. Push your project to GitHub:
   ```bash
   git add .
   git commit -m "feat: Prepare project for APK builds"
   git push origin main
   ```
2. Open your repository on **GitHub** → Click on the **Actions** tab.
3. In the left sidebar, select **Build Android APK**.
4. Click **Run workflow** → Select branch (`main`) → Click **Run workflow**.
5. Wait 1–2 minutes for the build to finish with a green checkmark.
6. Click on the successful workflow run.
7. Scroll down to the **Artifacts** section at the bottom:
   - Download **`app-debug`** (contains `app-debug.apk`)
   - Download **`app-release`** (contains `app-release.apk`)

---

### 2. How to Create a GitHub Release with the APK

When you are ready to publish a versioned release:

1. Create a version tag such as `v1.0.0`:
   ```bash
   git tag v1.0.0
   ```
2. Push the tag to GitHub:
   ```bash
   git push origin v1.0.0
   ```
3. **GitHub Actions automatically runs the `Release Android APK` workflow**:
   - Builds the production release APK.
   - Automatically creates a new **GitHub Release** titled `LifeRemind v1.0.0`.
   - Generates release notes automatically from your commit log.
   - Attaches `app-release.apk` and `LifeRemind-v1.0.0.apk` directly to the release.
4. Go to **GitHub Repository → Releases → Latest Release (`v1.0.0`)**.
5. Under **Assets**, click on `app-release.apk` or `LifeRemind-v1.0.0.apk` to download and install on any Android phone or tablet.

---

### 3. Build APK Locally with Gradle

If you have Android Studio, JDK 17, or the Android command-line tools installed locally:

```bash
# Make gradlew executable (macOS / Linux)
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk

# Build Release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

On Windows (Command Prompt / PowerShell):
```cmd
gradlew.bat assembleDebug
gradlew.bat assembleRelease
```

---

### 4. (Optional) Production Release Signing Secrets

By default, the project signs release builds using standard configured Android keys. If you wish to use your own custom production keystore on GitHub Actions:

1. Convert your keystore file to Base64:
   ```bash
   # On macOS / Linux:
   base64 -i my-release-key.jks | tr -d '\n' > keystore_base64.txt

   # On Windows (PowerShell):
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) | Set-Content keystore_base64.txt
   ```
2. Go to **GitHub Repository** → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**.
3. Add the following repository secrets:
   - `KEYSTORE_BASE64`: Paste the content of `keystore_base64.txt`
   - `STORE_PASSWORD`: Keystore password
   - `KEY_ALIAS`: Key alias (e.g., `upload` or `mykey`)
   - `KEY_PASSWORD`: Key password
