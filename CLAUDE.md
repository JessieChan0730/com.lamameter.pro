# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

LumaMeter is an Android light meter app that uses CameraX camera-frame luminance sampling to estimate scene brightness and provide exposure guidance (EV, aperture, shutter speed, ISO). Single-module Kotlin project using Jetpack Compose and Material 3.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run JVM unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest

# Lint
./gradlew lint

# Restricted environment (keeps Gradle state in-repo)
./gradlew -g .gradle-home assembleDebug
```

On Windows use `.\gradlew.bat` instead of `./gradlew`.

## Architecture

Clean Architecture + MVVM, structured as a real-time Flow pipeline (not a CRUD app):

```
Camera Frame → Y-Plane Sampling → Metering Mode Calc → EV Estimation → Exposure Result → Compose UI
```

### Layers (under `app/src/main/java/com/yourbrand/lumameter/pro/`)

- **`data/camera/`** - `LuminanceAnalyzer`: CameraX ImageAnalysis callback, extracts luminance from Y channel. Applies metering mode (average/center-weighted/spot) to compute `LuminanceReading`.
- **`domain/exposure/`** - Pure Kotlin, no Android dependencies. `ExposureCalculator` converts luma→EV100→exposure params. `ExposureModels.kt` defines `ExposureResult`, `LuminanceReading`, `MeteringMode`, `ExposureMode`, `MeteringPoint`, `ViewfinderRect`.
- **`viewmodel/`** - `MeterViewModel`: aggregates state via `MutableStateFlow<MeterUiState>`. Handles EV smoothing (0.82/0.18 EMA), AE lock, compensation, calibration offset, zoom presets, custom aperture/shutter libraries, and live vs single-shot metering.
- **`ui/meter/`** - `MeterScreen` (main screen), `MeterCameraPreview` (CameraX preview + overlay). Single-screen-first design.
- **`ui/components/`** - `HistogramChart`, `MeterComponents` (reticle overlays, controls).
- **`ui/theme/`** - Material 3 theming with system/light/dark modes (`AppThemeMode`).

### Key Design Decisions

- No dependency injection framework (manual constructor injection in ViewModel)
- No navigation library - single Activity with one main screen and settings as overlay/sheet
- `ExposureCalculator` is injected into `MeterViewModel` for testability
- EV smoothing uses exponential moving average to reduce metering jitter
- `domain/exposure/` must stay pure Kotlin with no Android imports (unit testable)

## Commit Message Convention

Enforced by `.githooks/commit-msg` hook. Format: `<type>(optional-scope): <description>`

- Types: `feat`, `fix`, `chore`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `revert`
- Description must start with a lowercase letter or digit
- Install hooks: `git config core.hooksPath .githooks` (or `.\scripts\install-git-hooks.ps1` on Windows)

## Coding Conventions

- 4-space indentation, trailing commas where they improve diffs
- `PascalCase` for classes/enums/Composables, `camelCase` for functions/properties, `snake_case` for Android resource names
- User-facing strings go in `strings.xml`, not Kotlin source
- Business logic stays out of Compose UI
- Localization: `values/strings.xml` (English) and `values-zh/strings.xml` (Chinese) must stay in sync

## Testing

JUnit 4 tests mirror the source structure under `app/src/test/`:
- `ExposureCalculatorTest`, `ExposureModelsTest` - domain logic
- `MeterViewModelTest` - state transitions and exposure calculations
- `LuminanceAnalyzerTest` - luminance extraction logic
- `MeterUiLogicTest` - UI logic helpers

Use backtick-quoted descriptive names: `` fun `aperture priority solves shutter from ev`() ``

## Key Technical Details

- **Min SDK**: 24, **Target SDK**: 36, **Compile SDK**: 36
- **Namespace**: `com.yourbrand.lumameter.pro`
- **Camera permission** required; `android.hardware.camera.any` feature not required
- Luminance analysis operates on the Y channel only (not full Bitmap) for performance
- Frame analysis is throttled to reduce metering jitter

## Display Format Conventions

### Shutter Speed (`formatShutter` in `MeterScreen.kt`)

- **>= 1s**: decimal seconds — `1s`, `1.1s`, `2s`, `30s`
- **< 1s**: fraction `1/N` — `1/2`, `1/60`, `1/125`, `1/4000`
- Never use decimal form below 1 second (e.g. `0.1s` is wrong, use `1/10`)
- Edge case: values just below 1s whose reciprocal rounds to 1 display as `1s`
- Tests in `MeterUiLogicTest` enforce this convention
