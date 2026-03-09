# Project Status

## Snapshot

- Last updated: 2026-03-09
- Scope owner: Sungbin personal super app
- Runtime target: Galaxy S25 FE phone form factor

## Purpose

- Build a single-user Android super app for Sungbin.
- Integrate nutrition tracking, workout tracking, and study notes in one app.
- Keep all data local on device without server dependency.

## Roles

- `app`: root navigation host, shared back stack, bottom tab shell, feature dependency wiring.
- `core-database`: Room schema and DAO access for nutrition/workout/study domains.
- `core-alarm`: alarm/notification foundation (currently skeleton only).
- `uikit`: reusable Compose UI tokens and shared components.
- `feature-nutrition`: nutrition dashboard and bodyweight-linked target UI flow.
- `feature-workout`: workout dashboard, supplement checklist, timer summary UI flow.
- `feature-study`: searchable/category-based study list UI flow.

## Feature Scope

- Nutrition: calorie/carbohydrate/protein tracking and bodyweight-based target guidance.
- Workout: routine summary, main exercise and max weight tracking, supplement intake tracking, timer-start trend summary.
- Study: gym term and muscle-related note browsing by category and search.

## Implementation Progress

### App Shell

- Done: single Navigation 3 back stack across three tabs.
- Done: custom bottom tab bar and per-tab floating button metadata wiring.
- In progress: floating button actions are placeholders (`onClick = {}`) in feature entries.

### Core Database

- Done: `core` module migration to `core-database`.
- Done: Room databases and DAOs for nutrition/workout/study.
- Done: workout timer record table stores timer start events (`startedAt` only) for first/last start time computation.
- In progress: migration policy is destructive fallback; explicit non-destructive migration set is not maintained.

### Core Alarm

- In progress: module skeleton created, no runtime alarm logic yet.

### Nutrition Feature

- Done: daily summary card, progress, 7-day trend, macro cards, eaten-food timeline.
- Done: body weight card supports inline input and save on keyboard dismiss.
- Done: trend list default scroll aligns to right edge.
- In progress: graph/detail button actions are placeholders.

### Workout Feature

- Done: routine summary, inline edit for main exercise and max weight, save on keyboard dismiss.
- Done: timer summary panel (first start, last start, workout duration with fire badge rule).
- Done: supplement checklist toggle and daily intake persistence.
- Done: trend list default scroll aligns to right edge.
- In progress: routine detail/trend detail/supplement management button actions are placeholders.
- In progress: timer start record write path is not yet connected to a running timer interaction.

### Study Feature

- Done: search field, category chips, grouped section list rendering.
- Done: DAO-backed filtering by category and keyword.
- In progress: item detail screen and edit flow are not implemented.

## Immediate Next Work

- Connect FAB actions to real feature flows.
- Implement alarm logic in `core-alarm` and connect reminder scheduling.
- Add detail screens for nutrition/workout trends and study entries.
- Add timer interaction flow that writes timer start records.
