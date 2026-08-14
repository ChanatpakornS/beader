# Incident Record: Dependabot Batch Merge on `main`

**Date:** 2026-08-14
**Commits:** `6f633ee`..`5755b22` on `main` (7 merges)
**Outcome:** All 7 targeted PRs merged; CI fully green on the final
result. One merge conflict resolved manually. Seven other open Dependabot
PRs deliberately left unmerged.

## What was merged

Seven Dependabot PRs, chosen as the lower-risk subset of the open batch
(CI-tooling version bumps and test-library patch bumps, no Kotlin/AGP/
coroutines involvement):

| PR | Bump | Merge commit |
|---|---|---|
| #2 | `actions/upload-artifact` 4 → 7 | `6f633ee` |
| #3 | `io.nlopez.compose.rules:detekt` 0.4.22 → 0.6.4 | `c00219b` |
| #4 | `androidx.test:runner` 1.6.2 → 1.7.0 | `baede7e` |
| #5 | `actions/checkout` 4 → 7 | `ab76e56` |
| #9 | `gradle/actions` 4 → 6 | `46697ca` |
| #10 | `actions/setup-java` 4 → 5 | `9263e45` |
| #13 | `androidx.test.ext:junit` 1.2.1 → 1.3.0 | `5755b22` |

Final CI run on `main` (commit `5755b22`, run `31794728971`): Static
analysis, Unit tests, Assemble debug APK, and both Instrumented tests
(API 29 and API 34) all completed with `success`.

## Correction to the PR #1 incident record: compose-rules 0.6.4 works fine

The original scaffolding incident record (`2026-08-14-pr16-pdfreader-unit-tests.md`
and the earlier PR #1 history) pinned `io.nlopez.compose.rules:detekt` at
`0.4.22` specifically because every version tried after it
(`0.4.23`+) failed to load — `NoClassDefFoundError` — under this
project's pinned Kotlin compiler (`2.0.21`), since those releases
declared a dependency on a newer `kotlin-stdlib` than `2.0.21` can read.

PR #3 bumped straight to `0.6.4` — five minor versions past the version
that broke things the first time — and **Static analysis (the Detekt
job) passed cleanly** in the pre-merge PR check and again in the
post-merge run against the full current `main` (including everything the
PDF reader feature added since the original incident).

**Working theory, not confirmed by inspecting the resolved classpath
directly:** `detekt`'s plugin classpath (the `detektPlugins`
configuration) resolves independently of the app's own Kotlin/AGP
version pins. It's plausible that whatever `kotlin-stdlib` version
Gradle resolves onto that isolated tool classpath today is compatible
with `compose-rules` 0.6.4's requirement, regardless of what's pinned for
the app's actual compilation. This would mean the original 0.4.22 pin
was never a hard, permanent ceiling — just a snapshot of what worked
under whatever dependency graph existed at the time.

**Net effect:** the constraint documented in the earlier incident record
no longer reproduces. Treat that record's specific version numbers as
historical, not as an active constraint — if a future compose-rules bump
does fail again, re-diagnose it rather than assuming this exact class of
failure recurs identically.

## PR #13 needed a manual conflict resolution

All 7 target PRs were opened against an older `main` (before PR #16, the
PDF reader feature, merged). GitHub still reported all of them as
cleanly mergeable against the current `main` tip — except PR #13
(`androidx.test.ext:junit`), which conflicted with PR #4
(`androidx.test:runner`), already merged moments earlier: both touch
adjacent lines in `gradle/libs.versions.toml`'s `[versions]` block, and
the diff context lines Dependabot generated no longer matched after
PR #4 landed.

Resolved by checking out the `dependabot/gradle/androidx.test.ext-junit-1.3.0`
branch locally, merging `main` into it, taking both version bumps
(`androidxTestJunit = "1.3.0"`, `androidxTestRunner = "1.7.0"`), and
pushing the resolved merge commit back to the same branch before
merging the PR through the API.

## PRs closed without merging

Seven other open Dependabot PRs were closed (not merged), each with a
comment explaining why:

| PR | Bump | Why held back |
|---|---|---|
| #6 | `androidx.activity:activity-compose` 1.9.3 → 1.13.0 | Minor jump not in the reviewed batch |
| #7 | `okhttp` 4.12.0 → 5.4.0 | Major version jump |
| #8 | `androidx.core:core-ktx` 1.15.0 → 1.19.0 | Minor jump not in the reviewed batch |
| #11 | `kotlin` 2.0.21 → 2.4.10 | Major jump; exactly the class of change that broke CI repeatedly during initial scaffolding (Hilt/AGP coupling, stdlib metadata walls) |
| #12 | `kotlinx-coroutines` 1.9.0 → 1.11.0 | Previously tried and reverted during initial scaffolding — requires newer `kotlin-stdlib` metadata than the pinned compiler can read |
| #14 | Gradle wrapper 8.10.2 → 9.7.0 | Major jump touching every module's build |
| #15 | `com.google.devtools.ksp` 2.0.21-1.0.28 → 2.3.11 | Tied to a Kotlin compiler version far ahead of the pin; needs to move with a Kotlin bump, not alone |

These remain open candidates for a future, isolated review pass — each
would need its own verification rather than being batched with the
lower-risk bumps above.

## Takeaways

- Real, current CI evidence outweighs a documented incident from an
  earlier, different dependency graph — but treat a surprising pass
  (like compose-rules 0.6.4 here) as worth a note for the next person,
  not silently accepted.
- Batching Dependabot PRs still needs per-PR judgment on risk, even when
  they all show green CI individually — that CI ran against the base
  each PR was opened against, not necessarily the current `main`.
- Sequential merges of PRs touching the same file (here,
  `gradle/libs.versions.toml`) can still conflict even when GitHub
  reports "clean" ahead of time, if an earlier merge in the same batch
  changes adjacent lines.
