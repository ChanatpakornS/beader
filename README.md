# Beader

A modern Android app built with Kotlin, Jetpack Compose, and Clean
Architecture — modularized so the module graph enforces the architecture
rather than relying on convention alone.

## Stack

- **Language:** Kotlin (Coroutines, Flow)
- **UI:** Jetpack Compose, single-Activity
- **DI:** Hilt
- **Architecture:** Clean Architecture + Unidirectional Data Flow (MVVM)
- **Build:** Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`)
- **Persistence / networking:** Room, DataStore, Retrofit + OkHttp + kotlinx.serialization
- **Testing:** JUnit 5, MockK, Turbine, Espresso, Compose UI Test

## Module graph

```
:app
 ├── :feature:sample
 ├── :core:data
 ├── :core:designsystem
 └── :core:ui

:feature:sample ──► :core:domain, :core:designsystem, :core:ui

:core:data ──► :core:domain, :core:network, :core:database, :core:datastore

:core:domain ──► :core:common          (pure Kotlin/JVM, no Android dependency)
:core:network, :core:database, :core:datastore ──► :core:common
:core:ui ──► :core:designsystem
```

See [`docs/01-architecture/overview.md`](docs/01-architecture/overview.md)
for the full rationale, a rendered dependency diagram, and the rules that
govern what may depend on what.

## Getting started

```bash
git clone <this-repo>
cd beader
cp local.properties.example local.properties   # point sdk.dir at your Android SDK
./gradlew assembleDebug
```

Open the project in the latest stable Android Studio and let it sync, or
work entirely from the CLI with the commands below.

## Common commands

```bash
./gradlew assembleDebug                 # build a debug APK
./gradlew testDebugUnitTest             # unit tests, all modules
./gradlew connectedDebugAndroidTest     # instrumented tests (needs a device/emulator)
./gradlew detekt spotlessCheck lintDebug  # static analysis
./gradlew spotlessApply                 # auto-format
```

## Repository layout

| Path            | Purpose |
|------------------|---------|
| `app/`            | Application entry point, NavHost, DI graph root. |
| `core/`            | Shared, feature-agnostic modules (`common`, `domain`, `data`, `network`, `database`, `datastore`, `designsystem`, `ui`, `testing`). |
| `feature/`         | One module per user-facing feature. `feature/sample` is the fully-worked reference slice — copy its shape for new features. |
| `docs/`            | Architecture, SDLC, and quality documentation. |
| `.ai/`             | Machine-readable context and rules for AI coding agents — see below. |
| `.github/`         | CI workflow, PR/issue templates, CODEOWNERS, Dependabot config. |
| `config/detekt/`   | Static analysis rule configuration. |

## Working with AI coding agents

This repo ships a `.ai/` workspace so Cursor, Windsurf, Copilot, Claude
Code, and similar tools can navigate it without re-deriving the
architecture from scratch:

- [`.ai/context.md`](.ai/context.md) — entry point; read this first.
- [`.ai/coding-standards.md`](.ai/coding-standards.md) — hard constraints (state management, DI, layering, testing).
- [`.ai/architecture-rules.json`](.ai/architecture-rules.json) — machine-readable module dependency graph and naming conventions.
- [`docs/04-ai/prompt-engineering-guide.md`](docs/04-ai/prompt-engineering-guide.md) — guidance for the human directing an agent on this repo.

## Contributing

See [`docs/02-sdlc/git-workflow.md`](docs/02-sdlc/git-workflow.md) for
branching, commit message, and PR conventions, and
[`docs/03-quality/testing-strategy.md`](docs/03-quality/testing-strategy.md)
for what test coverage is expected at each layer.
