# NativeRust

An Android application that uses Rust for native code with Slint-based UI rendering.

## Overview

- **Language**: Kotlin (Android) + Rust (Native layer)
- **UI Toolkit**: [Slint 1.16.1](https://slint.dev/) (Rust-side UI, rendered on Android via the `slint-android-activity` backend)
- **Android Plugin**: [mullvad/rust-android-gradle-plugin](https://mullvad.github.io/rust-android-gradle-plugin/) `0.10.1`
- **Gradle Plugin (AGP)**: `9.2.1`
- **NDK**: `30.0.14904198`
- **Rust Edition**: `2024`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Project Structure

```
NativeRust/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml       # NativeActivity entry point
│   │       └── res/                      # Android resources
│   └── rust/
│       ├── Cargo.toml                    # Rust crate manifest
│       ├── build.rs                      # Build script (slint-build)
│       └── src/
│           └── lib.rs                    # Native entry point (android_main)
├── build.gradle.kts                      # Root build file
├── settings.gradle.kts                   # Project settings
└── gradle/
    └── libs.versions.toml               # Dependency versions
```

---

## Prerequisites

### 1. Android Studio

Install [Android Studio](https://developer.android.com/studio) (Arctic Fox or later).

Set the `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) environment variable, or let Android Studio do it automatically via `local.properties`:

```properties
sdk.dir=<path-to-your-android-sdk>
```

### 2. Rust Toolchain

Install Rust via [rustup](https://rustup.rs/):

```powershell
rustup default stable
```

The project uses **Rust 2024 edition**, which is available in stable as of the current release. Android targets need to be added via the mullvad plugin (handled automatically), but a standard stable Rust toolchain must be installed.

### 3. Android NDK

The project targets NDK **30.0.14904198**. Install it via Android Studio:

- **Android Studio → Settings → Appearance & Behavior → System Settings → Android SDK → SDK Tools → Show Package Details → NDK** and select `30.0.14904198`.

Or install via command line if you prefer:

```powershell
sdkmanager "ndk;30.0.14904198"
```

---

## Build & Run

### From Android Studio

1. Open the project in Android Studio.
2. Connect an Android device (or start an emulator with ABI `arm64-v8a`, `armeabi-v7a`, `x86_64`, or `x86`).
3. Click **Run ▶**. Gradle will invoke Cargo to build the Rust library before packaging the APK.

### From the Command Line

```powershell
# Build debug APK for all ABIs
.\gradlew.bat assembleDebug

# Build release APK
.\gradlew.bat assembleRelease

# Install debug APK on a connected device
.\gradlew.bat installDebug
```

The rust-android Gradle plugin handles Rust compilation automatically when the build is triggered. For a **debug** build, it only compiles for the connected device's ABI; a **release** build compiles all four ABIs.

---

## Supported ABIs

| ABI        | Rust Target |
|------------|-------------|
| arm64-v8a  | `aarch64-linux-android` |
| armeabi-v7a| `armv7-linux-androideabi` |
| x86_64    | `x86_64-linux-android` |
| x86       | `i686-linux-android` |

---

## Architecture

```
Kotlin (MainActivity)
        │
        ▼
NativeActivity (Android's built-in launcher for native binaries)
        │
        ▼
Rust (lib.rs) ──► Slint UI (app-window.slint, meal-chip.slint)
        │
        ▼
Screen (OpenGL / Vulkan via Slint's backend-android-activity)
```

- `AndroidManifest.xml` declares `<activity android.name="android.app.NativeActivity">` with `android.app.lib_name = "rust"`. Android's `NativeActivity` dynamically loads `liblibrust.so` and calls its `android_main` entry point.
- `src/lib.rs:10` — `android_main` is annotated with `#[unsafe(no_mangle)]` so the JVM can find it. It initialises Slint's Android platform backend, creates `AppWindow`, and runs the event loop.
- `app/build.gradle.kts:42–46` — The `merge*JniLibFolders` Gradle task depends on `cargoBuild`, ensuring the compiled `.so` is available before the APK is packaged.

---
