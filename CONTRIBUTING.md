# Contributing to LumaMeter

Thanks for contributing to LumaMeter. This document describes the expected workflow for code changes, commit messages, tests, and pull requests.

## Before You Start

- Use JDK 17 or newer
- Use Android Studio with a working Android SDK
- Make sure you can build the `app` module locally
- Keep user-facing text in Android resources, not in Kotlin source

Recommended first steps after cloning:

1. Install the repository Git hooks
2. Build the project once
3. Run the unit tests before opening a pull request

## Development Setup

### Build

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS / Linux:

```bash
./gradlew assembleDebug
```

If you are working in a restricted environment, keep Gradle state inside the repository:

```powershell
.\gradlew.bat -g .gradle-home assembleDebug
```

### Tests

Run JVM unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Run Android instrumentation tests on a connected device or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Run lint when available in your environment:

```powershell
.\gradlew.bat lint
```

## Project Structure

The repository contains a single Android app module: `app/`.

- `app/src/main/java/com/yourbrand/lumameter/pro/ui/`
  Compose screens, components, and theme setup
- `app/src/main/java/com/yourbrand/lumameter/pro/viewmodel/`
  UI state and interaction handling
- `app/src/main/java/com/yourbrand/lumameter/pro/domain/exposure/`
  Pure Kotlin exposure logic
- `app/src/main/java/com/yourbrand/lumameter/pro/data/camera/`
  CameraX-related luminance analysis
- `app/src/main/res/`
  Android resources and localized strings
- `app/src/test/`
  JVM unit tests
- `app/src/androidTest/`
  Instrumented tests

## Coding Guidelines

- Follow official Kotlin style with 4-space indentation
- Use trailing commas where they improve diffs
- Use `PascalCase` for classes, enums, and Composables
- Use `camelCase` for functions and properties
- Use `snake_case` for Android resource names
- Keep business logic out of Compose UI
- Put user-facing strings in `strings.xml`

For localized features, keep `values/` and `values-zh/` consistent.

## Testing Expectations

Prefer tests for pure logic and state transitions.

- Add or update tests in `domain/` when changing exposure calculation behavior
- Add or update tests in `viewmodel/` when changing UI state logic
- Use descriptive JUnit 4 test names, for example ``fun `aperture priority solves shutter from ev`()```

At minimum, contributors should run:

```powershell
.\gradlew.bat testDebugUnitTest
```

When a change affects device behavior, CameraX flow, lifecycle handling, or permissions, also run instrumented tests if your environment supports them.

## Commit Message Rules

This repository uses a `commit-msg` Git hook to validate the first line of each commit message.

### Install the Hook

Windows:

```powershell
.\scripts\install-git-hooks.ps1
```

macOS / Linux:

```bash
git config core.hooksPath .githooks
```

### Required Format

The commit subject must follow:

```text
<type>(optional-scope): <description>
```

Examples:

- `feat: add AE lock toggle`
- `fix(camera): stop preview crash on resume`
- `docs: explain commit message rules`
- `refactor(viewmodel)!: simplify exposure state`

Allowed types:

- `feat`
- `fix`
- `chore`
- `docs`
- `style`
- `refactor`
- `perf`
- `test`
- `build`
- `ci`
- `revert`

Notes:

- Only the first line is validated
- The second `-m` value, if provided, is treated as the commit body and is not restricted
- Git-generated merge commits and `git revert` messages are allowed
- The description should start with a lowercase word, for example `feat(settings): add preview reference grid`

Valid example:

```bash
git commit -m "feat: add commit message validation" -m "Document the workflow in CONTRIBUTING.md."
```

Rejected examples:

- `Stop tracking local tool folders`
- `update readme`
- `feature: add AE lock`
- `feat: Stop tracking local tool folders`
- `fix add camera state`

## Pull Request Guidelines

Keep pull requests focused. A pull request should usually address one feature, one bug, or one cohesive refactor.

Please include:

- A short summary of the change
- Build and test status
- Screenshots for UI changes
- Notes for localization changes when `values/` and `values-zh/` are updated

Before opening a pull request:

1. Rebase or merge from the latest target branch as needed
2. Run relevant tests
3. Confirm commit messages follow the required format
4. Review for accidental machine-specific or generated files

## What Not to Commit

Do not commit:

- Machine-specific changes in `local.properties`
- IDE-only changes under `.idea/` unless they are intentionally shared project settings
- Temporary Gradle caches
- Unrelated generated files

Keep Camera permissions, resource strings, and localized behavior aligned with current app behavior unless the change intentionally updates them.
