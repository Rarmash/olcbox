# Turnbox

Kotlin Multiplatform app with shared Compose UI.

Android uses `VpnService` with bundled `hev-socks5-tunnel` and `olcrtc`.
Desktop uses proxy mode: local `olcrtc` SOCKS5 plus OS PAC proxy settings.

## Prerequisites

- JDK 17 or newer.
- Android Studio, Android SDK and NDK for Android builds.
- Go for desktop builds, because `desktopApp` builds bundled `olcrtc` binaries from source.
- The `olcrtc` fork must be available locally.

By default Gradle expects the fork here:

```bash
/Users/nigga/Personal/Projects/olcrtc
```

Use `OLCRTC_REPO` if the fork is somewhere else:

```bash
OLCRTC_REPO=/path/to/olcrtc ./gradlew :desktopApp:run
```

On Windows PowerShell:

```powershell
$env:OLCRTC_REPO="C:\path\to\olcrtc"
.\gradlew.bat :desktopApp:run
```

## Android

Debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

Output:

```text
androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Install debug APK to a connected device:

```bash
./gradlew :androidApp:installDebug
```

Release APK requires `keystore.properties` in the repository root:

```properties
storeFile=/absolute/path/to/release.keystore
storePassword=...
keyAlias=...
keyPassword=...
```

Build release APK:

```bash
./gradlew :androidApp:assembleRelease
```

Output:

```text
androidApp/build/outputs/apk/release/androidApp-release.apk
```

Android native libraries are built by Gradle/NDK from `androidApp/src/main/jni`.
Configured ABIs are `arm64-v8a` and `x86_64`.

## Desktop Development

Run desktop app:

```bash
./gradlew :desktopApp:run
```

Run desktop app with hot reload:

```bash
./gradlew :desktopApp:hotRun --auto
```

Build bundled desktop `olcrtc` binaries only:

```bash
./gradlew :desktopApp:buildDesktopNativeAssets
```

Outputs:

```text
desktopApp/build/generated/desktopNativeResources/native/olcrtc-darwin-arm64
desktopApp/build/generated/desktopNativeResources/native/olcrtc-windows-amd64.exe
```

## macOS Release

Run on macOS:

```bash
./gradlew :desktopApp:packageReleaseDmg
```

Output:

```text
desktopApp/build/compose/binaries/main-release/dmg/Turnbox-1.0.0.dmg
```

The generated DMG is not notarized unless Apple signing/notarization is configured separately.

## Windows Release

Windows installers must be built on Windows. Compose/JPackage does not cross-package Windows `.exe` or `.msi` from macOS/Linux.

Build EXE on Windows:

```powershell
$env:OLCRTC_REPO="C:\path\to\olcrtc"
.\gradlew.bat :desktopApp:packageReleaseExe
```

Output:

```text
desktopApp\build\compose\binaries\main-release\exe\Turnbox-1.0.0.exe
```

Build MSI on Windows:

```powershell
$env:OLCRTC_REPO="C:\path\to\olcrtc"
.\gradlew.bat :desktopApp:packageReleaseMsi
```

Output:

```text
desktopApp\build\compose\binaries\main-release\msi\Turnbox-1.0.0.msi
```

If JPackage reports missing Windows packaging tools, install a JDK with `jpackage` support and WiX Toolset, then make sure WiX is available in `PATH`.

## Linux Release

Run on Linux:

```bash
./gradlew :desktopApp:packageReleaseDeb
```

Output:

```text
desktopApp/build/compose/binaries/main-release/deb/turnbox_1.0.0_amd64.deb
```

Linux packaging also must be run on Linux.

## Current OS Release Shortcut

Package for the current host OS:

```bash
./gradlew :desktopApp:packageReleaseDistributionForCurrentOS
```

This does not cross-package other operating systems.

## Checks

Run the main verification set:

```bash
./gradlew :sharedUI:jvmTest :desktopApp:compileKotlin :androidApp:assembleDebug
```

Run desktop JVM tests only:

```bash
./gradlew :sharedUI:jvmTest
```
