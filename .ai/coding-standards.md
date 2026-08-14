# AI Coding Standards — Beader

Explicit, load-bearing constraints for any AI coding agent working in this
repository. These are not style preferences — code that violates them
should be treated as incorrect, not merely non-idiomatic.

## State & reactivity

- **Never use `LiveData`.** Always expose UI state as `StateFlow` (from a
  ViewModel) or `Flow` (from a repository/use case). Convert with
  `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)`.
- **Never expose `MutableStateFlow` publicly.** Backing property is
  `private val _uiState`, public type is `val uiState: StateFlow<T>`.
- **One `UiState` type per screen**, exhaustive `sealed interface`
  (`Loading` / `Success` / `Error` at minimum). Composables `when`-branch
  over it exhaustively — no `else ->` branch that silently swallows a new
  state.

## Architecture boundaries

- **Never put business logic in a Composable.** A Composable reads
  `UiState` and calls lambdas passed in from the ViewModel; it does not
  call a repository, use case, or perform data transformation beyond pure
  presentation formatting (e.g. date formatting for display).
- **Never let `:core:domain` import `android.*`.** It is a
  `org.jetbrains.kotlin.jvm` module. If Android APIs seem necessary, the
  code belongs in `:core:data` or a `:feature:*` module.
- **Never let a DTO (`*Dto`) or Room entity (`*Entity`) type cross out of
  `:core:data`.** Map to the domain model (`:core:domain`'s `model`
  package) inside the repository implementation, before returning.
- **Never add a dependency between two `:feature:*` modules.** Shared
  logic goes in `:core:*`; shared navigation goes through `:app`'s
  `NavHost` composing each feature's `NavGraphBuilder` extension.
- **ViewModels depend on use cases (`:core:domain`), not directly on
  repository implementations (`:core:data`).** A ViewModel constructor
  parameter typed as `SampleRepositoryImpl` (rather than the `SampleRepository`
  interface, or better, a use case) is a defect.

## Compose

- **Use `@Immutable` or `@Stable` on Compose state models** (`UiState`
  classes, data classes passed as Composable parameters) so the compiler
  can skip unnecessary recomposition. Every `data class`/`sealed interface`
  under a `feature/*/SampleUiState.kt`-style file should carry one of these
  annotations.
- **Hoist state.** A Composable below the `*Route` (e.g. `SampleRoute`)
  entry point takes state and lambdas as parameters; it does not call
  `hiltViewModel()` or read a ViewModel itself. Only the `*Route` composable
  touches the ViewModel.
- **Design tokens only from `:core:designsystem`.** No hardcoded hex
  colors, raw `sp`/`dp` magic numbers, or ad hoc `Color(0x...)` calls
  inside `:feature:*` composables — use `MaterialTheme.colorScheme`,
  `MaterialTheme.typography`, and shared spacing constants.
- **Every public composable that isn't a screen root takes a `Modifier`
  parameter** with a default of `Modifier`, and applies it to its outermost
  layout node.

## Dependency injection

- **Constructor injection only** (`@Inject constructor`). No field
  injection, no service locators, no manual `Provides` calls outside a
  `@Module`.
- **Bind interfaces, not implementations**, via `@Binds` in an `abstract
  class` Hilt module (see `core/data/.../di/DataModule.kt`). ViewModels and
  use cases depend on the interface type.
- **Scope deliberately.** Repositories and DAOs are `@Singleton`
  (installed in `SingletonComponent`). ViewModels are `@HiltViewModel`,
  scoped to their own component automatically — never annotate a
  ViewModel `@Singleton`.

## Coroutines

- **Never launch a coroutine with `GlobalScope`.** Use `viewModelScope`,
  a `@Singleton`-scoped `CoroutineScope` injected via Hilt, or a scope
  passed as a parameter for testability.
- **Inject dispatchers**, don't reference `Dispatchers.IO` directly in
  business logic — use the `@Dispatcher(BeaderDispatchers.Io)` qualifier
  from `core:common` so tests can substitute a `TestDispatcher`.
- **Repositories are the layer that decides `Flow` semantics**
  (`SharingStarted`, caching, retry). Use cases and ViewModels consume the
  `Flow` they're given; they don't wrap it in another `stateIn`/`shareIn`
  unless they specifically need a different sharing scope than what's
  already provided.

## Comments and naming

- **Default to no comments.** Only write one when it captures a non-obvious
  *why* (a workaround, a constraint from an external system, a subtle
  invariant) — never to restate what the next line already says.
- **No `// TODO` left in code an agent generates as "done".** Either
  implement it or explicitly flag it to the human as incomplete in your
  response — don't silently commit a stub.
- **Match existing naming**: `*UseCase` (domain), `*Repository`/
  `*RepositoryImpl` (domain interface / data impl), `*Dto` (network),
  `*Entity` (database), `*UiState` (feature), `*Route`/`*Screen`
  (composable entry point vs. internal composable).

## Testing

- **New logic ships with a test in the same change**, not a follow-up.
  ViewModel and use case logic: JUnit 5 + MockK + Turbine. Compose UI:
  `createComposeRule()` against the stateless screen composable, not
  `createAndroidComposeRule` unless Hilt injection is actually required.
- **Use the fakes in `:core:testing`** (e.g. `FakeSampleRepository`)
  for ViewModel tests instead of hand-rolling a new mock of the repository
  interface per test file.

## What "done" means for an AI-generated change

- Compiles (`./gradlew build` would succeed — check for obvious syntax/
  import errors even if you can't run Gradle yourself).
- Respects every constraint above.
- Includes tests for new logic.
- Does not touch unrelated files, formatting, or modules outside the
  requested scope.
