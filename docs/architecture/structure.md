# Project Structure

## Root Modules

- `app`
- `core`
- `feature-nutrition`
- `feature-workout`
- `feature-study`

## Module Responsibilities

### `app`

- Application entry point.
- Hilt `Application` bootstrap lives here.
- Material3 `Scaffold` with bottom navigation for the three top-level feature modules.
- Default top-level tab is `feature-nutrition`.
- Navigator between the three feature modules.
- App-level Hilt setup.
- No feature business logic.
- Android namespace and application ID use `sungbinland.app`.
- App Kotlin sources also live under the `sungbinland.app` package.

### `core`

- Room-based app database foundation.
- Notification logic foundation.
- No feature-specific business logic.
- No UI or navigation contracts.

### `feature-nutrition`

- Daily calorie, carb, and protein tracking.
- Goal-oriented reminders driven by current bodyweight rules.
- Exposes the nutrition route key, entry registration DSL, and nutrition screen to `app`.

### `feature-workout`

- Workout routine logging.
- 90-second rest timer support.
- Supplement intake logging and reminders.
- Exposes the workout route key, entry registration DSL, and workout screen to `app`.

### `feature-study`

- Gym term study.
- Muscle group study.
- Exposes the study route key, entry registration DSL, and study screen to `app`.

## Dependency Boundaries

- `app` depends on every feature module and `core`.
- Every feature module depends only on `core`.
- `core` does not depend on any feature module.

## Build Configuration

- Shared Android SDK defaults are configured once in `settings.gradle.kts` through the Android settings plugin.
- Module build scripts keep only module-specific Android configuration such as `namespace`, unique `defaultConfig`, and feature toggles.
- UI modules depend on Compose Material3 as the default component library.

## Navigation Structure

- `app` owns the root `Scaffold`.
- `app` uses bottom navigation to switch between `feature-nutrition`, `feature-workout`, and `feature-study`.
- `feature-nutrition` is the default selected tab.
- The three bottom-navigation tabs share one Navigation 3 back stack.
- `app` owns the root navigation state between `feature-nutrition`, `feature-workout`, and `feature-study`.
- `core` is not involved in navigation concerns.

## Data Structure

- Local persistence is centered in `core`.
- Room database setup belongs only in `core`.
- Notification logic belongs only in `core`.
- Feature modules consume persistence and notification foundations through `core`.

## Device Scope

- The maintained runtime target is a Galaxy S25 FE phone form factor.
- Foldable, tablet, and other adaptive large-screen support is intentionally excluded.
