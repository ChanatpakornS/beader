# Incident Record: PR #16 `PdfReaderViewModelTest` Failures

**Date:** 2026-08-14
**PR:** [#16 — feat: add PDF import library with reader (first app feature)](https://github.com/ChanatpakornS/beader/pull/16)
**Outcome:** Fixed. Root cause was misdiagnosed on the first attempt; the
second fix addressed the actual problem.

## Symptom

All three tests in `PdfReaderViewModelTest` failed in CI's "Unit tests"
job (`:feature:pdfreader:testDebugUnitTest`), each reporting a bare
exception at the line that constructs `PdfReaderViewModel` in `setUp()`.
`LibraryViewModelTest`, in the sibling `:feature:library` module, passed.

## First attempt (wrong diagnosis)

**Symptom seen:** `java.lang.RuntimeException` at the `PdfReaderViewModel(...)`
constructor call.

**Wrong diagnosis:** `PdfReaderViewModel` takes a `SavedStateHandle`
constructor parameter, and `LibraryViewModel` (which passed) doesn't — so
it looked like AGP's "not mocked" stub-`android.jar` behavior for
`SavedStateHandle` construction in a plain JVM unit test.

**Fix applied:** Added `testOptions.unitTests.isReturnDefaultValues = true`
to `feature/pdfreader/build.gradle.kts`.

**Result:** Did not fix it. It changed the failure from a loud
`RuntimeException` to a `java.lang.NullPointerException` at the same line
— because `isReturnDefaultValues = true` makes stubbed Android methods
return `null`/`0`/`false` instead of throwing, and *something* in the
construction path was still touching a real Android class.

## Second attempt (actual root cause)

**Real culprit:** `PdfReaderArgs` (in `PdfReaderNavigation.kt`), read
during `PdfReaderViewModel`'s field initialization, called
`android.net.Uri.decode(...)` on the nav argument. `android.net.Uri` is a
genuine Android platform class with no implementation in the stub
`android.jar` that local unit tests compile and run against —
`SavedStateHandle` itself was never the problem; it's a real, pure
in-memory AndroidX class that works fine in plain JUnit tests.

With `isReturnDefaultValues = false` (the default), the stubbed
`Uri.decode` call threw `RuntimeException: Method decode not mocked`.
With `isReturnDefaultValues = true` (the first, wrong fix), the same stub
call instead returned `null`, which Kotlin's compiler-inserted
platform-type null-check then rejected via `NullPointerException`, since
`PdfReaderArgs.uri` is declared as a non-null `String`.

**Fix applied:**
1. Reverted the `isReturnDefaultValues` change — it only masked the
   problem and would silently hide any future accidental Android-framework
   call in this module's unit tests instead of failing loudly.
2. Replaced `android.net.Uri.encode`/`Uri.decode` with `java.util.Base64`
   URL-safe encoding (`Base64.getUrlEncoder().withoutPadding()`) for the
   nav-argument codec:
   - `java.util.Base64` has no Android framework dependency, so it works
     in local JVM unit tests without Robolectric.
   - Its output alphabet is only `[A-Za-z0-9_-]`, which sidesteps a
     separate, real concern: it was unclear whether Jetpack Navigation's
     own route matching additionally percent-decodes path segments before
     populating a `SavedStateHandle`, which could have caused the
     original `Uri.encode`/`Uri.decode` pair (or an `URLEncoder`/
     `URLDecoder` pair, briefly considered as an intermediate fix) to be
     silently double-decoded in production. A Base64 payload contains no
     `%`-escapes, so it round-trips identically regardless of how many
     times (if any) an intermediate layer tries to percent-decode it.
3. Updated `PdfReaderViewModelTest` to seed its `SavedStateHandle` with
   the same encoding (`encodeUriArg(DOCUMENT_URI)`, exposed as
   `internal`) that real navigation now produces, instead of a raw
   unencoded string.
4. Verified the encode/decode round-trip against realistic SAF content
   URIs — including ones with pre-existing percent-encoded segments and
   special characters — using a directly-invoked `kotlinc` script, since
   this sandbox cannot run the project's own AGP-based Gradle tasks
   (Google Maven is network-blocked; see the prior PR #1 incident record
   for the same limitation).

## Takeaways

- **A test failure's proximate location (the constructor call on the
  stack trace) is not necessarily its cause.** The exception surfaced at
  `PdfReaderViewModel(...)`, but the actual problem was in a class it
  calls during field initialization (`PdfReaderArgs`), several frames
  removed from the visible line number in GitHub's truncated Actions log
  output.
- **`isReturnDefaultValues = true` changes a failure's shape, not
  necessarily whether it fails.** Reaching for it as a first response to
  "not mocked" is a plausible but incomplete diagnosis — it's suppression,
  not a fix, unless the code genuinely doesn't care about the return value
  of the stubbed call. Confirm what's actually being stubbed before
  reaching for it.
- **Prefer framework-independent codecs (`java.util.Base64`,
  `java.net.URLEncoder`) over `android.net.Uri`'s equivalents when the
  code needs to run in a plain JVM unit test.** This is a recurring
  category of bug in this codebase — watch for it whenever a ViewModel or
  use case touches `android.*` types directly rather than through an
  injected abstraction.
- When encoding a value into a route path segment, **prefer an alphabet
  immune to whatever the navigation framework's route-matching does
  internally** (e.g. Base64 URL-safe) over relying on exact single-pass
  decode semantics you haven't verified against the pinned library
  version's actual behavior.
