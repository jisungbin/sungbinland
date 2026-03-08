# Project Rules

## Product Scope

- This project is a super Android app for Sungbin only.
- Multi-user support is out of scope.
- Backward compatibility is out of scope.
- If requirements are ambiguous, stop and ask instead of inferring.

## Delivery Rules

- Record every project rule and instruction under `docs/` with clear categories.
- Prefer the latest published release for every dependency and build tool.
- Beta, alpha, and RC versions are allowed and should be preferred when they are the latest published release.
- JDK should also track the latest available release.

## App Feature Scope

- `feature-nutrition`: daily calorie, carb, and protein tracking plus repeating reminders based on bodyweight-driven goal rules.
- `feature-workout`: workout routine logging plus a 90-second rest timer and supplement intake logging with reminders.
- `feature-study`: gym term study plus muscle group study.
- Future expansion is expected.

## Storage Rules

- The app is local-only.
- No server integration is planned.
- Use Room for local persistence.

## Build Rules

- `compileSdk`, `targetSdk`, and `minSdk` should always track the latest Android release.
- Shared Android SDK defaults should be configured through the Android settings plugin in `settings.gradle.kts`.
- Use explicit dependency versions.
- In the version catalog, use `version.ref` only when the same version is shared in more than one place.
- If a version is used only once, inline it with `version = "..."`.
- In the version catalog, declare libraries with `module = "group:name"`.
- In the version catalog, use the same prefix rule across versions, libraries, and plugins.
- Use `android-*` for Android Gradle Plugin entries, `androidx-*` for AndroidX, `kotlinx-*` for Kotlinx, and keep Compose entries under `compose-*`.
- Keep Google ecosystem aliases as existing names such as `hilt-*` and `ksp`; do not add a `google-*` prefix.
- Sort version catalog entries by artifact group and then alphabetically.
- Separate version catalog prefix blocks with blank lines.
- In Gradle `dependencies` blocks, keep project dependencies in their own block and sort external dependencies with the same prefix-block and alphabetical rules used by the version catalog.
- Treat Kotlin warnings as errors in every module.
- Use AGP built-in Kotlin support.
- Do not apply the `org.jetbrains.kotlin.android` plugin.
- Apply the Compose plugin with `kotlin("plugin.compose")`.
- Keep the Compose plugin declaration inline, but source its version from the version catalog.
- Compose plugin versions should also track the latest published release.
- Enable Kotlin explicit API mode with `kotlin { explicitApi() }` in every module.
- Use KSP instead of KAPT.

## Module Rules

- `app` is for navigation wiring only.
- `app` provides a Material3 `Scaffold` and bottom navigation for the three feature modules.
- The default selected top-level tab is `feature-nutrition`.
- Real app features live only in root-level feature modules.
- There should be no parent feature module.
- There are exactly three root feature modules: `feature-nutrition`, `feature-workout`, and `feature-study`.
- Shared core logic belongs only in `core`.
- Feature modules depend on `core`.

## Core Rules

- `core` is limited to Room-based app database setup and notification logic.
- Core must not contain `@Composable` APIs or Navigation 3 contracts.
- Core non-UI logic uses Kotlin Coroutines and Kotlinx Immutable Collections.
- Prefer extensions outside classes for sugar or convenience APIs.
- Avoid interface-implementation pairs unless multiple implementations are genuinely needed.
- Core classes should remain testable when real logic is added.
- Non-core UI and framework glue do not require tests.

## UI and State Rules

- Use Jetpack Compose for all UI.
- Use Compose Material3 for all UI components.
- Compose UI dependencies should track the latest published release.
- When iterating lists in Compose-related code, use the fast collection APIs from `androidx.compose.ui:ui-util`.
- Do not rely on `androidx.compose.ui:ui` for `ui-util` APIs. Depend on `androidx.compose.ui:ui-util` directly.
- If only Compose annotations are needed, depend on `androidx.compose.runtime:runtime-annotation` directly instead of relying on `androidx.compose.runtime:runtime`.
- Every UI `@Composable` function definition must declare `modifier: Modifier = Modifier`.
- Do not add `@Preview`.
- Use AndroidX Navigation 3.
- Navigation UI contracts belong outside `core`.
- The three bottom-navigation tabs should share one Navigation 3 back stack.
- The app targets a Galaxy S25 FE phone form factor only.
- Foldables, tablets, and other adaptive large-screen layouts are out of scope.
- Use Hilt for DI.
- Do not use ViewModel.
- Use Compose Retained API with Molecule when real state logic is introduced.

## Test Rules

- Use JUnit 6 for test execution.
- Use `assertk` for unit tests.
- Use `mockk` only when mocking is unavoidable.

## Kotlin Style Rules

- Prefer `private` and `internal` visibility.
- Keep `public` API to the minimum necessary cross-module surface.
- Remove avoidable warnings such as redundant `Unit` return types and redundant explicit type arguments.
- Put every annotation on the same line as its declaration.
- Use 2-space indentation.
- Reflect the linked Kotlin style guide in future implementation work.

## Package and Source Layout

- App module base path: `app/src/main/kotlin/sungbinland/app`
- App module Android namespace and application ID: `sungbinland.app`
- Core module base path: `core/src/main/kotlin/sungbinland/core`
- Feature module base paths:
- `feature-nutrition/src/main/kotlin/sungbinland/nutrition`
- `feature-workout/src/main/kotlin/sungbinland/workout`
- `feature-study/src/main/kotlin/sungbinland/study`
