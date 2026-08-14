# Feature: Reader Reading-Experience Polish

**Status:** In development
**Branch:** `feature/reader-gapless-gestures`
**One-liner:** Four reading-interaction fixes to the reader screen built in
`feature/reader-ux-enhancements` — gapless continuous scroll, a blank
system-colored top bar, and tap gestures (double-tap to reset zoom,
single-tap edges to page).

## The four items

1. **Zero gap between pages in continuous mode.** Previously each page in
   the continuous `LazyColumn` was forced to exactly one viewport height
   (`fillParentMaxSize()`), regardless of the page's actual rendered
   aspect ratio. Since rendered pages are almost always shorter than a
   full screen (portrait document pages vs. a taller phone viewport), the
   `Image`'s default `ContentScale.Fit` centered the page inside that
   oversized slot, leaving a visible band of empty space above and below
   every page — exactly the "huge space before going to the next page"
   reported. Fixed by sizing each continuous-mode item to its own
   intrinsic aspect ratio at full width instead of a fixed viewport
   height, so consecutive pages sit flush against each other.
2. **Blank top bar title**, system-themed. The literal "PDF Reader" title
   text is removed (left blank) — the mode-toggle button and drawer
   button remain. No color changes were needed: the `TopAppBar` already
   had no hardcoded colors, so it was already inheriting
   `MaterialTheme.colorScheme` (which in turn follows the system's
   light/dark and, on Android 12+, dynamic-color wallpaper theme, per
   `BeaderTheme`) — confirmed this stays true, not modified further. A
   toolbar with real tools/actions is explicitly deferred to a future
   feature, not this one.
3. **Double-tap resets zoom.** Double-tapping a page resets pinch-zoom
   scale to `1x` (which, since pages are always rendered at the display's
   own width, is exactly "fit to display" — no separate fit calculation
   needed) and clears any pan offset.
4. **Single-tap edges page forward/backward** — tapping the right half of
   a page goes to the next page, the left half to the previous page.
   Scoped to single-page mode only (see decision below).

## Design decisions made without a separate confirmation round

Unlike the previous reader feature, these four were specific, mechanical
UI-interaction fixes with one clearly correct reading — each is recorded
here rather than raised as a question upfront:

- **Tap-to-page is single-page-mode only.** In continuous mode, scrolling
  already is the page-advance gesture; adding a competing tap-driven jump
  on top of free scrolling would fight the user's own scroll position
  rather than help it. Double-tap-to-reset-zoom still works in both
  modes, since zoom is orthogonal to which reading mode is active.
- **Tap and double-tap share one gesture detector** (`detectTapGestures`
  with both `onTap` and `onDoubleTap` in the same call) so Compose's
  built-in tap-timing disambiguation handles the single-vs-double
  distinction — no manual delay/debounce logic needed. Pinch/pan
  (`detectTransformGestures`) stays a second, independent `pointerInput`
  block on the same node, which is the standard way to combine tap and
  transform gestures on one Compose node.
- **No change to single-page mode's sizing.** Single-page mode's
  fill-and-center behavior (a page shorter than the viewport sits
  centered with blank margins) is normal, expected single-page-reader
  behavior and wasn't part of the complaint — only continuous mode's
  inter-page spacing was.

## What changes

All within `:feature:pdfreader` — no new files, no domain/data changes.

| File | Change |
|---|---|
| `PdfReaderScreen.kt` | `PdfReaderTopBar`: `title = {}` (blank) instead of `Text("PDF Reader")`. `ZoomablePdfPage`: sizing is now entirely caller-controlled via the `modifier` parameter (no more hardcoded internal `fillMaxSize()`); adds a `detectTapGestures` pointer-input block (double-tap resets zoom/pan; single-tap, when `onTapNavigate` is supplied, dispatches based on which half of the page width was tapped). `SinglePageContent` passes `Modifier.fillMaxSize()` (same net sizing as before) and wires `onTapNavigate` to `onNextPage`/`onPreviousPage`. `ContinuousContent`'s per-item content uses `Modifier.fillMaxWidth()` (no forced height) and does not pass `onTapNavigate`; its loading placeholder uses a fixed A4-ish aspect ratio instead of a full viewport height, so the loading state doesn't itself reintroduce a large gap before the real page swaps in. |

## Testing plan

- `PdfReaderScreenTest`: top bar renders without a title (no "PDF Reader"
  text node); single-page and continuous content still render their
  existing indicators/fields (unaffected by the sizing/gesture changes,
  which aren't practically assertable through Compose UI tests without
  synthetic multi-touch input — covered by manual verification instead,
  noted below).
- Pinch-zoom, double-tap, single-tap-to-page, and the continuous-mode
  gap fix are interaction/rendering behaviors that Compose UI tests
  can't meaningfully assert without a real gesture-injection harness this
  codebase doesn't have; verified by code review of the gesture-detector
  wiring and the sizing-modifier change instead of a new automated test.
