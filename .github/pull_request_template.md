## Summary

<!-- What does this PR change, and why? Link the issue it closes if any. -->

## Type of change

- [ ] Feature
- [ ] Bug fix
- [ ] Refactor (no behavior change)
- [ ] Chore / tooling / CI
- [ ] Documentation

## Architecture checklist

- [ ] Changes respect module dependency direction (`:app` → `:feature:*` → `:core:*`; `:core:domain` has no Android dependency)
- [ ] Business logic lives in ViewModel / use case, not in a Composable
- [ ] New/changed public API in `:core:*` is used by more than one feature (otherwise it belongs in the feature module)
- [ ] `StateFlow` used for UI state exposure, not `LiveData`

## Testing

- [ ] Unit tests added/updated for new logic
- [ ] Ran the app locally and exercised the golden path
- [ ] `./gradlew detekt spotlessCheck testDebugUnitTest` passes locally

## Screenshots / recordings

<!-- For UI changes, attach before/after screenshots or a screen recording. -->
