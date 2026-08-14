# Prompt Engineering Guide — Working With AI Coding Agents on This Repo

This document is for humans directing an AI coding agent (Cursor, Windsurf,
Copilot, Claude Code, etc.) on this repository. For the machine-readable
rule set the agent itself should load, see `.ai/coding-standards.md` and
`.ai/architecture-rules.json`.

## Before prompting: point the agent at context

Every AI tool that supports it should be configured to load, in order:
1. `.ai/coding-standards.md` — hard constraints (never do X, always do Y).
2. `.ai/architecture-rules.json` — machine-readable module dependency rules.
3. `.ai/docs/01-architecture/overview.md` — the "why" behind the rules.

If your tool doesn't auto-load these, paste them into the system/context
window before the first prompt in a session.

## Writing effective prompts for this codebase

**Name the module, not just the feature.** "Add a bookmark toggle to the
sample list" is ambiguous about which layer owns the change. Prefer: "Add
a `BookmarkItem` use case in `:core:domain`, a `SampleRepository` method
implemented in `:core:data`, and wire it into `SampleViewModel` in
`:feature:sample`." Specificity about layers prevents the agent from
putting business logic in the wrong place (most commonly: in the
Composable).

**State the existing pattern to follow.** This repo has one fully-worked
example slice (`feature:sample`, `core:domain`'s `SampleRepository`/
`GetSampleItemsUseCase`, `core:data`'s `SampleRepositoryImpl`). When asking
for a new feature, say "follow the same UiState/ViewModel/UseCase/
Repository shape as `feature:sample`" rather than describing the pattern
from scratch — the agent can read the real files.

**Ask for tests in the same prompt as the implementation**, not as a
follow-up. Agents deprioritize tests when they're a separate ask. Specify
which layer's test doubles to use: ViewModel tests should use
`FakeSampleRepository`-style fakes from `:core:testing`, not ad-hoc mocks
repeated per test.

**Constrain scope explicitly for bug fixes.** "Fix X" without a scope
constraint invites unrelated refactors. State: "Fix only the bug described;
do not refactor surrounding code."

## What to review carefully in AI-generated output

1. **Dependency direction.** Check new `implementation(project(...))` lines
   in any `build.gradle.kts` against `.ai/docs/01-architecture/overview.md`'s
   dependency graph. An agent unfamiliar with the boundary will happily add
   a `:feature:*` → `:feature:*` dependency or an `android.*` import inside
   `:core:domain` if not stopped.
2. **State exposure type.** Grep the diff for `LiveData` — it should never
   appear. Also check for `MutableStateFlow` exposed as a public/mutable
   type from a ViewModel instead of as `StateFlow`.
3. **DTO/entity leakage.** A DTO (`*Dto`, `:core:network`) or entity
   (`*Entity`, `:core:database`) type should never appear in a
   `:feature:*` module's imports.
4. **Magic numbers and strings in Composables.** Should come from
   `:core:designsystem` tokens (`MaterialTheme.colorScheme`, spacing
   constants) rather than hardcoded `dp`/`sp`/hex values, except in the
   design system module itself.
5. **Comment noise.** Per `.ai/coding-standards.md`, agents should not
   narrate what code does in comments. Strip any comment that just restates
   the next line.

## Iterating when the agent gets the architecture wrong

Don't just say "that's wrong" — name the rule it violated and point at the
doc: "This puts the network call directly in the ViewModel. Per
`.ai/docs/01-architecture/overview.md`, `:feature:*` should only call
`:core:domain` use cases. Move the Retrofit call into `:core:data` and
expose it through `SampleRepository`." Agents correct much faster with the
specific boundary named than with a vague "please fix the architecture."

## Session hygiene

- Keep AI sessions scoped to one module or one vertical slice where
  possible — cross-cutting sessions increase the chance of layering
  mistakes.
- Re-paste `.ai/coding-standards.md` at the start of a new session; don't
  assume context carries over between sessions or tools.
- When an agent proposes a new module, check it against
  `.ai/architecture-rules.json`'s `moduleTypes` before accepting — new
  modules should fit one of the existing archetypes (`app`, `feature`,
  `core-domain`, `core-data`, `core-infrastructure`, `core-ui`) rather than
  inventing a new one ad hoc.
