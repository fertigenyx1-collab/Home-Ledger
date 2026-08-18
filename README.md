# LifeRemind — Personal Life & Payment Reminder Manager

LifeRemind is a personal life and payment reminder manager built with modern Kotlin, Jetpack Compose, Material 3, and Room local persistence.

---

## 🚀 Automated APK Generation with GitHub Actions

This repository is configured with a GitHub Actions workflow (`.github/workflows/android-build.yml`) that automatically builds, tests, signs, packages, and releases your Android APK.

---

## 1. Pushing the Project to GitHub

If you haven't pushed your code to GitHub yet, run the following commands in your terminal:

```bash
# Initialize git (if not already initialized)
git init

# Add all files to staging
git add .

# Create initial commit
git commit -m "feat: Initial commit with LifeRemind and GitHub Actions workflow"

# Rename branch to main
git branch -M main

# Add your GitHub repository remote
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git

# Push to GitHub
git push -u origin main
```

---

## 2. (Optional) Configuring Production Signing Secrets

By default, the GitHub Actions workflow will automatically generate a valid signing key on CI so your APK builds and runs immediately.

If you have your own production keystore file (`my-upload-key.jks`), you can add it securely as GitHub Secrets:

1. Convert your keystore file to Base64 in your terminal:
   ```bash
   # On macOS / Linux:
   base64 -i my-upload-key.jks | tr -d '\n' > keystore_base64.txt

   # On Windows (PowerShell):
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks")) | Set-Content keystore_base64.txt
   ```
2. In your GitHub repository, go to **Settings** > **Secrets and variables** > **Actions** > **New repository secret**.
3. Add the following secrets:
   - `KEYSTORE_BASE64`: The full base64 string from `keystore_base64.txt`
   - `STORE_PASSWORD`: Your keystore password
   - `KEY_ALIAS`: Your key alias (e.g. `upload`)
   - `KEY_PASSWORD`: Your key password

> 🔒 **Security note:** Never commit keystore files or passwords directly into git. All sensitive credentials are kept in `.gitignore`.

---

## 3. How to Trigger APK Builds

### Option A: Automatic Build on Git Push (Artifact)
Every time you push commits to `main` or open a Pull Request:
1. GitHub Actions automatically checks out the repository, sets up Java 17 and Gradle.
2. It compiles `./gradlew assembleRelease`.
3. It renames the APK to `LifeRemind-v<version>.apk`.
4. It attaches the APK under the workflow **Artifacts** tab.

### Option B: Manual Trigger (`workflow_dispatch`)
1. Go to your GitHub repository in your browser.
2. Click the **Actions** tab at the top.
3. Select **Android Build & Release APK** from the left sidebar.
4. Click **Run workflow** > select branch `main` > click **Run workflow**.

### Option C: Tagged Release (`git tag v1.0.0`)
When you are ready to publish a new release:

```bash
# Create a release version tag
git tag v1.0.0

# Push the tag to GitHub
git push origin v1.0.0
```

**What happens automatically:**
- GitHub Actions triggers on the tag push.
- Compiles the release APK `LifeRemind-v1.0.0.apk`.
- Creates a new **GitHub Release `v1.0.0`** with release notes.
- Attaches `LifeRemind-v1.0.0.apk` directly to the release page.

---

## 4. Downloading the Generated APK

### From GitHub Actions Artifacts:
1. Navigate to the **Actions** tab on your GitHub repository.
2. Click on the latest completed workflow run.
3. Scroll down to the **Artifacts** section at the bottom.
4. Click on `LifeRemind-...apk` to download the zip file containing your APK.

### From GitHub Releases:
1. On the main page of your repository, click **Releases** (on the right sidebar).
2. Click on the latest release tag (e.g. `v1.0.0`).
3. Under **Assets**, click `LifeRemind-v1.0.0.apk` to download and install on any Android device.

---

## 🛠 Local Build Commands

```bash
# Build Debug APK locally
./gradlew assembleDebug

# Build Release APK locally
./gradlew assembleRelease
```
