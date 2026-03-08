# Dependency Policy

## Policy

- All dependency versions are explicit.
- Single-use versions are written inline in `gradle/libs.versions.toml`.
- Shared versions use `version.ref`.
- Latest published releases are preferred even when they are alpha, beta, or RC.
- Gradle wrapper is the source of truth for the Gradle version.
- JDK should track the latest available release.

## Notes

- Libraries in the version catalog should use `module = "group:name"` notation.
- Version catalog names should use the same prefix rule across versions, libraries, and plugins.
- Use `android-*` for Android Gradle Plugin entries, `androidx-*` for AndroidX, `kotlinx-*` for Kotlinx, and `compose-*` for Compose.
- Keep Google ecosystem aliases as existing names such as `ksp`; do not add a `google-*` prefix.
- Sort version catalog entries by artifact group and then alphabetically.
- Separate version catalog prefix blocks with blank lines.
- In Gradle `dependencies` blocks, keep project dependencies in their own block and order external dependencies with the same prefix-block and alphabetical rules.
- The version catalog is the single source of truth for dependency and plugin versions except `com.android.settings`, which is declared directly in `settings.gradle.kts`.
- `kotlin("plugin.compose")` stays inline in Gradle, but its version is sourced from the version catalog.
- Every module enables Kotlin explicit API mode with `kotlin { explicitApi() }`.
- Kotlin compiler warnings are treated as errors for every module.
- Jetpack Compose UI artifacts should track the latest published release.
- Compose Material3 is the standard UI component library for the project.
- The Compose Gradle plugin should also track the latest published release.
- `androidx.compose.ui:ui-util` and `androidx.compose.runtime:runtime-annotation` should be managed as standalone artifacts rather than being treated as implicit parts of `ui` or `runtime`.
- `core-database` uses Room, Kotlin Coroutines, and Kotlinx Immutable Collections for database-related logic.
- `core-alarm` is a dedicated module for notification logic.
- AGP built-in Kotlin is enabled, so the Kotlin Android plugin is not applied.
- KSP is preferred over KAPT.
