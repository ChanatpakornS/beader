# Feature: Reader UX Enhancements

**Status:** In development
**Branch:** `feature/reader-ux-enhancements`
**One-liner:** Four related upgrades to the PDF reader screen, requested
together and shipped as one feature since they all touch the same files.

Scope and design choices below were confirmed with the product owner
before implementation — see "Design decisions" for what was resolved and
why.

## The four items

1. **Navigation drawer** — a drawer on the Reader screen, opened from the
   top bar, listing every PDF in the library (thumbnail + name) so the
   user can jump straight from one document to another, or back to the
   Library, without leaving the reader and re-navigating manually.
2. **Reading mode toggle** — switch between the existing single-page view
   (Previous/Next, one page at a time) and a new **continuous/seamless
   scroll** mode where all pages render into one scrollable column.
3. **Snap-to-page in continuous mode** — while scrolling continuously,
   the list snaps to whole-page boundaries on fling/release rather than
   settling mid-page.
4. **Page-jump input** — a numeric text field + "Go" button next to the
   page indicator, in both reading modes, to jump straight to a page
   number instead of only stepping one at a time.

## Design decisions

- **Drawer contents**: full library list (thumbnail + filename), not just
  a bare "back" button — the point is to let the user move between PDFs
  without a round-trip through the Library screen.
- **"Scroll lock fit with pdf height"** (the ambiguous original ask)
  resolved to mean #3 above: continuous-scroll snapping, not a change to
  the existing single-page pinch-zoom pan bounds. That interaction is
  untouched.
- **One feature, one branch**: all four items touch `PdfReaderScreen.kt`
  / `PdfReaderViewModel.kt` directly, so splitting them into separate
  branches would just create merge churn between them.
- **Page-jump UI**: a plain numeric `OutlinedTextField` + `Button`, not a
  slider — precise input matters more than a scrubbing gesture for
  documents that can run to hundreds of pages.

## What changes, module by module

No new modules this time — this is entirely inside the existing
`:feature:pdfreader` vertical slice, plus small `:app` navigation wiring.
`:core:domain`'s `ObserveLibraryUseCase` (already built for the Library
screen) is reused as-is — no domain-layer changes needed.

| Module | Change | Why |
|---|---|---|
| `:feature:pdfreader` | `PdfReaderUiState.Content` replaces the old single-page-only `Success` state: carries a `readingMode` (`SINGLE_PAGE` / `CONTINUOUS`), the full `pageCount`, a `currentPageIndex`, and a `pages: Map<Int, ByteArray>` cache (one entry in single-page mode, grows as pages are scrolled into view in continuous mode). `PdfReaderViewModel` gains `onToggleReadingMode`, `onJumpToPage`, `onPageNeeded` (lazy per-page load for continuous mode), a `libraryItems: StateFlow<List<PdfLibraryItem>>` (backing the drawer, via `ObserveLibraryUseCase`), and a one-shot `scrollToPageEvents: SharedFlow<Int>` for programmatic scroll requests (mode switch, page jump) — kept as events rather than state to avoid a scroll-position feedback loop with the list's own natural scrolling. `PdfReaderScreen.kt` gains a `ModalNavigationDrawer`, mode-specific content composables (`SinglePageContent`, `ContinuousContent`), a shared `ZoomablePdfPage` (existing pinch-zoom logic, now reusable per-page), and a shared `PageJumpField`. |
| `:feature:pdfreader` navigation | `pdfReaderScreen()` now takes `onNavigateToDocument` and `onNavigateToLibrary` callbacks, supplied by `:app` — features still never reference each other's routes directly. |
| `:app` | `BeaderNavHost` wires `onNavigateToDocument = { uri -> navigateToPdfReader(navController, uri) }` (same helper already used from Library) and `onNavigateToLibrary` as a full pop-to-fresh-Library navigation, so repeatedly switching documents via the drawer can never leave the user stuck behind a deep back stack — the drawer's own "Back to Library" always gets them out in one tap regardless of how many documents they've hopped through. |

## Continuous mode: how lazy loading works

Each page in the `LazyColumn` is only *composed* by Compose when it's at
or near the viewport (standard Lazy layout behavior — this codebase adds
no extra visible-range tracking on top of it). The first composition of
each page item fires `onPageNeeded(index)`, which renders and caches that
page's bytes if they aren't already in the `pages` map — so scrolling
through a document renders pages on demand rather than up front, and nothing
needs to explicitly evict pages that scroll back out of view (Compose
just stops recomposing them; the byte cache is intentionally allowed to
grow for the lifetime of one continuous-mode session — no eviction policy
in this pass, acceptable for "necessary" scope given documents in
practice are dozens, not thousands, of pages).

## Testing plan

- `PdfReaderViewModelTest`: reading-mode toggle preserves the current
  page, `onJumpToPage` clamps to `[0, pageCount)` in both modes,
  `onPageNeeded` doesn't re-fetch an already-cached page, `libraryItems`
  reflects the fake repository's emissions.
- `PdfReaderScreenTest`: single-page mode still renders the page
  indicator and Previous/Next; continuous mode renders the page-jump
  field; the mode-toggle button's label reflects the current mode.
