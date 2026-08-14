# Git Workflow

## Branching strategy: trunk-based development

`main` is always releasable. Work happens on short-lived branches merged
back into `main` via pull request, not on long-lived `develop`/`release`
branches (GitFlow). Feature flags gate incomplete work that must land on
`main` before it's ready for users, rather than living on a branch for
weeks.

- **`main`** — protected, always green, always deployable. Direct pushes disabled.
- **`feature/<short-description>`** — new functionality. Branch from `main`, merge back via PR.
- **`fix/<short-description>`** — bug fixes.
- **`chore/<short-description>`** — tooling, CI, dependency bumps, refactors with no behavior change.
- **`release/<version>`** — cut only at release time, for release-only stabilization (rare; most fixes still land on `main` first and get cherry-picked).

Keep branches short-lived — days, not weeks. If a feature needs longer,
split it into smaller PRs behind a feature flag rather than keeping a
branch open.

## Conventional Commits

Every commit message follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

**Types:** `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `ci`, `chore`, `revert`

**Scope** is the module name without the `:` prefix, e.g. `feature-sample`, `core-data`, `core-designsystem`. Omit scope for changes spanning many modules.

Examples:

```
feat(feature-sample): add favorite toggle to sample list

fix(core-data): fall back to cached items when network refresh fails

refactor(core-domain): extract ToggleFavoriteUseCase from SampleViewModel

chore: bump AGP to 8.7.2
```

Breaking changes add `!` after the type/scope and a `BREAKING CHANGE:` footer:

```
refactor(core-domain)!: rename SampleRepository.getItems to observeSampleItems

BREAKING CHANGE: all callers must switch to the Flow-returning API.
```

Commit messages drive automated changelog generation — write the summary
as what changed and why, not "fixed bug" or "updates".

## Pull request rules

1. **One logical change per PR.** A PR that mixes a feature with an unrelated refactor is two PRs.
2. **Fill out the PR template** (`.github/pull_request_template.md`), including the architecture checklist — it exists to catch layering violations (e.g. business logic leaking into a Composable) before review.
3. **CI must be green** before merge: `detekt`, `spotlessCheck`, unit tests, and `assembleDebug` all run on every PR (see `.github/workflows/ci.yml`).
4. **At least one approval** from a CODEOWNER for files under their ownership (see `.github/CODEOWNERS`).
5. **Squash merge** into `main` — keeps `main` history one commit per PR, matching the Conventional Commit written in the PR title.
6. **Delete the branch** after merge.

## Versioning

The app follows [Semantic Versioning](https://semver.org/) for `versionName`
(`MAJOR.MINOR.PATCH`). `versionCode` increments by 1 on every release build
regardless of the semantic bump. Bump the version in `app/build.gradle.kts`
as part of the release PR, not as a separate commit.
