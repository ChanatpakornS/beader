# Incident Record: detekt compose-rules 0.4.22 → 0.6.4 bump (PR #3)

**Date:** 2026-08-14
**PR:** #3 — Dependabot bump of `io.nlopez.compose.rules:detekt` from `0.4.22` to `0.6.4`
**CI run checked:** [run #45 / 31794728971](https://github.com/ChanatpakornS/beader/actions/runs/31794728971), commit `5755b226` (main HEAD after PRs #2, #3, #4, #5, #9, #10, #13 merged)
**Outcome:** No revert needed. The historical concern does not reproduce in practice — CI's `Static analysis` job passed clean, `detekt` task `BUILD SUCCESSFUL in 28s`, 0 code smells across every module.

## Background

The initial scaffold session (see the "chore: bump dependencies to latest
patch/minor within pinned majors" commit on 2026-08-14) deliberately pinned
`detekt-rules-compose` at `0.4.22` with this reasoning:

> detekt-rules-compose (compose lint) stays pinned at 0.4.22: every version
> after it (0.4.23+) requires kotlin-stdlib 2.1.20/2.2.0 at runtime, which
> broke loading the plugin (NoClassDefFoundError) under our Kotlin 2.0.21
> pin - confirmed by checking each version's own POM.

That reasoning was based on reading each version's POM in isolation, not
from an actual failing CI run. Dependabot's PR #3 later bumped past that
pin to `0.6.4`, which — per the note above — should have broken `detekt`
with a `NoClassDefFoundError` once the plugin tried to load against our
still-pinned Kotlin `2.0.21` (`gradle/libs.versions.toml`: `kotlin =
"2.0.21"`, unchanged by PR #3).

## What actually happened

PR #3 merged and CI ran clean on main at `5755b226`:

- `Static analysis` job: **success** (Spotless, Detekt, Android Lint all green).
- `detekt` Gradle task specifically: `BUILD SUCCESSFUL in 28s`, 14 actionable
  tasks executed, **0 code smells** in every module's Complexity Report
  (`core:database`, `core:common`, `app`, `core:data`, `core:datastore`,
  `core:designsystem`, `core:network`, `core:pdf`, `core:ui`, `core:testing`,
  `core:domain`, `feature:library`, `feature:pdfreader`, `feature:sample`).
- No `NoClassDefFoundError`, no plugin-loading failure, anywhere in the job log.

So `compose-rules:detekt` `0.6.4` loads and runs fine against our pinned
`detekt` `1.23.8` / Kotlin `2.0.21` combination. **The 0.4.23+ pin note was
wrong, or at least no longer applies at 0.6.4.**

## Why the original concern doesn't hold

`detekt-rules-compose` is added via the `detektPlugins` Gradle configuration
(`build.gradle.kts`, root `subprojects {}` block:
`dependencies { add("detektPlugins", ...) }`), not `implementation` or any
configuration on the main compile/runtime classpath. `detektPlugins` is
detekt's own isolated tool classpath — the detekt Gradle plugin resolves it
as a **separate dependency graph** from the project's Kotlin compiler/stdlib
classpath.

Concretely: our project's Kotlin plugin version (`2.0.21`) governs what
`kotlinCompilerClasspath` / `implementation` resolve to for our own code. It
does **not** constrain what `detektPlugins` resolves for `compose-rules`.
When Gradle resolves `detektPlugins`, `compose-rules:detekt:0.6.4`'s own POM
pulls in whatever `kotlin-stdlib` version *it* declares as a transitive
dependency, isolated in that configuration's classpath — so even if
`compose-rules` 0.6.4 depends on a newer `kotlin-stdlib` than 2.0.21, that
newer stdlib gets resolved and loaded within detekt's own plugin
classloader, never touching (and never being constrained by) the app
module's Kotlin 2.0.21 compiler pin.

This matches the general pattern already noted in this repo's commit
history for `detekt`/`ktlint`: both "run through Gradle's own isolated tool
classloaders ... never the project's main compile classpath." That note was
previously applied to justify *not* worrying about Kotlin-metadata
compatibility for `detekt`/`ktlint` version bumps themselves — it turns out
the same isolation also protects `detektPlugins` dependencies like
`compose-rules` from the app's Kotlin pin, contrary to the earlier
POM-reading-only conclusion.

## Takeaways

- **A dependency's declared POM requirement is not sufficient evidence of
  breakage on its own** when that dependency is consumed through an isolated
  tool classpath (`detektPlugins`, `ktlintRuleset`, similar Gradle
  buildscript-style configurations) rather than the main compile/runtime
  classpath. Verify against a real CI run before pinning a dependency below
  latest for a classpath-isolation reason — the POM alone doesn't tell you
  which classpath actually matters.
- The `detektComposeRules` version pin in `gradle/libs.versions.toml` can
  track Dependabot bumps normally going forward; no re-pin to `0.4.22` (or
  any ceiling) is needed on Kotlin-compatibility grounds alone.
- If a future `compose-rules` bump *does* break `detekt` in CI, the failure
  mode to look for is a `NoClassDefFoundError`/`NoSuchMethodError` in the
  `Detekt` step of the `Static analysis` job (`> Task :<module>:detekt`) —
  not a compile error elsewhere, since `detektPlugins` is isolated from the
  app's own compilation.
