# Project Structure

## Root Modules

- `app`
- `core-alarm`
- `core-database`
- `feature-nutrition`
- `feature-workout`
- `feature-study`
- `uikit`

## Module Responsibilities

### `app`

- Application entry point.
- Material3 `Scaffold` with bottom navigation for the three top-level feature modules.
- Default top-level tab is `feature-nutrition`.
- Navigator between the three feature modules.
- App-level dependency wiring without a DI framework.
- No feature business logic.
- Android namespace and application ID use `sungbinland.app`.
- App Kotlin sources also live under the `sungbinland.app` package.

### `core-alarm`

- Notification logic foundation.
- No feature-specific business logic.
- No UI or navigation contracts.

### `core-database`

- Room-based app database foundation.
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

### `uikit`

- Shared Compose UI toolkit for feature modules.
- Owns reusable design tokens, reusable cards, input controls, chart widgets, and common FAB patterns.
- Must stay UI-only and should not depend on Room DAOs, app navigation state, or feature-specific data models.

## Dependency Boundaries

- `app` depends on every feature module and the core modules (`core-alarm`, `core-database`).
- Every feature module depends only on core modules and `uikit`.
- `uikit` does not depend on feature modules or core modules.
- `core-alarm` does not depend on any feature module.
- `core-database` does not depend on any feature module.

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
- Core modules are not involved in navigation concerns.

## Data Structure

- Local persistence is centered in `core-database`.
- Room database setup belongs only in `core-database`.
- Notification logic belongs only in `core-alarm`.
- Feature modules consume persistence and notification foundations through core modules.
- `core-database/workout` stores workout routine (`WorkoutRoutine`), workout exercise (`WorkoutExercise`), supplement (`Supplement`), workout session summary (`WorkoutSession`), and supplement intake (`SupplementIntake` + `SupplementIntakeItem`) entities.
- `WorkoutRoutine` and `WorkoutExercise` are connected by relation mapping (`Routine -> Exercises`).
- `SupplementIntakeItem.supplementName` references `Supplement.name` through a foreign key.
- `core-database/study` stores `StudyEntry` entities with composite primary key (`category`, `name`), text content, and optional image link.

## Device Scope

- The maintained runtime target is a Galaxy S25 FE phone form factor.
- Foldable, tablet, and other adaptive large-screen support is intentionally excluded.
