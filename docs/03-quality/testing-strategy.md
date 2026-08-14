# Testing Strategy

We test in the shape of the [testing pyramid](https://martinfowler.com/articles/practical-test-pyramid.html):
many fast unit tests, fewer integration tests, a handful of UI/instrumented
tests covering golden paths only.

## Tooling

| Concern                         | Tool                                   |
|----------------------------------|-----------------------------------------|
| Test runner / assertions         | JUnit 5 (Jupiter)                       |
| Mocking                          | MockK                                   |
| Flow testing                     | Turbine                                 |
| Coroutine test scheduling        | `kotlinx-coroutines-test`, `MainDispatcherRule` (`:core:testing`) |
| Fluent assertions (optional)     | Google Truth                            |
| Instrumented / UI tests          | Espresso, Compose UI Test               |
| Hilt test injection              | `hilt-android-testing`, `HiltTestRunner` (`:core:testing`) |

## Layer-by-layer expectations

### `:core:domain` — unit tests, 100% of use cases

Pure Kotlin, no Android dependency, so these run on the JVM in milliseconds.
Every use case gets at least:
- One test for the "happy path" pass-through of repository data.
- One test per branch of business logic the use case adds (if any).

Use `MockK` against the repository *interface* — never against `:core:data`
implementations from this module.

### `:core:data` — unit tests against fakes/mocks, no real network or DB

Repository implementations are tested with MockK'd `ApiService`/`Dao`
collaborators (see `SampleRepositoryImplTest`), asserting on:
- The cache-then-network-refresh sequence.
- Fallback-to-cache behavior when the network call fails.
- Correct DTO ↔ entity ↔ domain mapping.

Do not spin up a real Room database or hit a real network in this layer's
unit tests — that belongs in an instrumented test only if the interaction
itself (e.g. a Room migration) is what's under test.

### `:feature:*` ViewModels — unit tests using `:core:testing` fakes

Each `SampleUiState` branch (`Loading`, `Success`, `Error`) needs a test.
Use `FakeSampleRepository` (in-memory, `:core:testing`) wired through the
real use cases rather than mocking use cases directly — this exercises the
same integration points production code does, without touching Android
APIs.

`viewModelScope` coroutines require `MainDispatcherRule`
(`@RegisterExtension`) so `Dispatchers.Main` resolves in a JVM test.

### `:feature:*` Composables — Compose UI tests, golden paths only

One test per `UiState` variant, asserting the correct content renders
(`SampleScreenTest`). These run against a bare `createComposeRule()` —
no Activity, no Hilt — by passing `uiState` directly into the stateless
`SampleScreen` composable rather than routing through `SampleRoute`'s
`hiltViewModel()`.

### `:app` — a small number of end-to-end instrumented tests

`MainActivityTest` verifies the app launches and the start destination
renders. This layer intentionally has the fewest tests — it exists to catch
wiring mistakes (missing Hilt binding, broken NavHost) that unit tests
can't see, not to re-verify feature logic already covered lower down.

## Coverage SLAs

| Module type                       | Minimum line coverage | Enforced by |
|-------------------------------------|------------------------|-------------|
| `:core:domain`, `:core:common`      | 90%                     | CI review; no automated gate yet — see note below |
| `:core:data`                        | 80%                     | CI review |
| `:feature:*` ViewModels/UseCases     | 80%                     | CI review |
| Composables (`:feature:*`, `:core:ui`, `:core:designsystem`) | Golden-path only, no numeric target | Manual review |

> **Note:** a Kover/Jacoco coverage gate is not wired into CI yet. Until it
> is, coverage is enforced through PR review against this table. Add the
> gate as a follow-up rather than skipping the review step in the meantime.

## What we do not test

- Third-party library internals (Retrofit, Room, Compose runtime itself).
- Trivial data classes / DTOs with no logic.
- Generated code (Hilt components, Room implementations).

## Running tests locally

```bash
# Unit tests, all modules
./gradlew testDebugUnitTest

# Unit tests, single module
./gradlew :feature:sample:testDebugUnitTest

# Instrumented tests (requires a connected device/emulator)
./gradlew connectedDebugAndroidTest

# Static analysis
./gradlew detekt spotlessCheck lintDebug
```
