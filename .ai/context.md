# AI Agent Entry Point — Beader

Read this file first. It's the map; the other files in `.ai/`, including
`.ai/docs/`, are the detail.

## What this repo is

An Android app in Kotlin + Jetpack Compose, Clean Architecture, modularized
by Gradle. One fully-worked vertical slice exists — `feature:sample` — copy
its shape for new features rather than inventing a new pattern.

## Read next, in this order

1. `.ai/coding-standards.md` — hard constraints on this codebase. Treat
   violations as bugs, not style nits.
2. `.ai/architecture-rules.json` — machine-readable module map: which
   module may depend on which, naming conventions, DI rules. Validate any
   new `build.gradle.kts` dependency you add against this before writing
   it.
3. `.ai/docs/01-architecture/overview.md` — the narrative explanation and
   Mermaid dependency graph behind the JSON rules.
4. `.ai/docs/04-ai/prompt-engineering-guide.md` — written for the human
   directing you, but useful for understanding what reviewers will check
   in your output.

## The reference slice

To see the full stack for one feature, read these files in this module
graph order:

```
core/domain/.../model/SampleItem.kt              domain model
core/domain/.../repository/SampleRepository.kt   repository interface
core/domain/.../usecase/GetSampleItemsUseCase.kt use case
core/network/.../model/SampleItemDto.kt          wire model
core/network/.../service/SampleApiService.kt     Retrofit service
core/database/.../entity/SampleItemEntity.kt     Room entity
core/database/.../dao/SampleItemDao.kt           Room DAO
core/data/.../repository/SampleRepositoryImpl.kt repository impl + DTO/entity mapping
core/data/.../di/DataModule.kt                   Hilt @Binds
feature/sample/.../SampleUiState.kt              screen state
feature/sample/.../SampleViewModel.kt            ViewModel
feature/sample/.../SampleScreen.kt               Composables
feature/sample/.../navigation/SampleNavigation.kt  NavGraphBuilder wiring
```

New features should produce the same set of files, one directory level
down (`feature/<name>/...`), with matching test files
(`*Test.kt` next to each production file above that has logic worth
testing).

## Fast commands

```bash
./gradlew detekt spotlessCheck testDebugUnitTest   # what CI runs before assembling
./gradlew :feature:sample:testDebugUnitTest         # scoped to one module
```

## If you're about to do one of these, stop and check the rules file first

- Add a dependency between two `:feature:*` modules → not allowed, see `architecture-rules.json`.
- Add `androidx.` or `android.` import inside `:core:domain` or `:core:common` → not allowed.
- Expose `LiveData` from a new ViewModel → not allowed, use `StateFlow`.
- Return a `*Dto` or `*Entity` type from a function outside `:core:data` → not allowed.
