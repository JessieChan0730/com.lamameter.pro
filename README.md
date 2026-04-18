# LumaMeter

[English](README.md) | [简体中文](README.zh-CN.md)

## What is LumaMeter

LumaMeter is an Android light meter app built with Jetpack Compose and CameraX. It uses camera-frame luminance sampling to estimate scene brightness and provides exposure guidance including EV, aperture, shutter speed, and ISO.

The app follows a Clean Architecture + MVVM design and is centered around a single main screen for a fast metering workflow.

### Core Features

- Real-time and single-shot metering via CameraX Y-plane luminance sampling
- Three metering modes: Average, Center Weighted, Spot
- Two exposure priority modes: Aperture Priority, Shutter Priority
- AE Lock, exposure compensation (±3 EV), calibration offset (±5 EV)
- ND filter selector with metering compensation
- Calibration presets for common scenarios
- ISO presets from 50 to 6400
- Custom aperture and shutter value libraries
- Zoom controls with preset buttons and slider
- Live histogram display
- English and Simplified Chinese localization
- Material 3 UI with system / light / dark theme support

## Project Structure

```text
LumaMeter/
├── app/src/
│   ├── main/java/.../                # Application source code
│   │   ├── data/camera/              # CameraX Y-channel luminance extraction
│   │   ├── domain/exposure/          # Pure Kotlin exposure models & calculation
│   │   ├── ui/components/            # Histogram, reticle overlays, shared controls
│   │   ├── ui/meter/                 # Main metering screen & camera preview
│   │   ├── ui/theme/                 # Material 3 theme configuration
│   │   └── viewmodel/               # State aggregation via StateFlow
│   ├── main/res/                     # Resources (icons, strings, themes, etc.)
│   ├── test/                         # JVM unit tests
│   └── androidTest/                  # Instrumented tests
├── gradle/                           # Gradle Wrapper & version catalog
├── scripts/                          # Helper scripts (Git Hooks install, etc.)
├── .githooks/                        # Commit message format check
└── .github/workflows/                # CI workflows
```

## How to Run

### Requirements

- Android Studio
- JDK 17 or newer
- Android SDK and build tools
- An Android device or emulator with camera support

### Run from Android Studio

1. Open the project in Android Studio
2. Wait for Gradle sync to finish
3. Run the `app` module
4. Grant camera permission on first launch

### Build from Command Line

macOS / Linux:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

### Run Tests

```bash
# JVM unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint
```

## Roadmap

- [x] **Basic metering** — real-time and single-shot metering, spot / center-weighted / average modes, aperture / shutter priority, AE lock, exposure compensation, calibration, ND filter support, histogram, zoom, custom value libraries, theme settings, bilingual localization
- [ ] **White balance detection**
- [ ] **Focus distance estimation**
- [ ] **Zone system metering**
- [ ] **Bug fixes**
- [ ] **Other features**

## Thanks for Your Support

[https://linux.do](https://linux.do)
