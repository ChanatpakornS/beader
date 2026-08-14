# Feature: PDF Import & Library

**Status:** In development
**Branch:** `feature/pdf-reader`
**One-liner:** Import a PDF into an on-device library (shown as a thumbnail
grid), tap a thumbnail to read it page by page.

This design went through two rounds of clarification with the product
owner before implementation — the scope below is the confirmed result, not
the first draft. In particular: this is **not** just a pick-and-read flow.
Imported PDFs persist in a library.

## Scope

- **Library screen** (app's start destination): grid of imported PDFs,
  each shown as a thumbnail of its first page. A floating action button
  imports a new PDF via the system file picker (Storage Access Framework).
  Long-press/tap-the-delete-icon removes an entry from the library.
- **Reader screen**: opened by tapping a library thumbnail. Single-page
  view with Previous/Next controls, a page indicator ("Page 3 of 12"), and
  pinch-to-zoom on the current page.
- Imported PDFs and their thumbnails **persist across app restarts** in
  the local Room database — the library isn't an in-memory session list.

**Explicitly out of scope for this first version** (revisit only if
asked): text search, annotations/highlighting, bookmarks, printing/
sharing, password-protected PDFs, page rotation, sort/filter/rename in the
library.

## Why no third-party PDF library

Android ships `android.graphics.pdf.PdfRenderer` (API 21+) — a
rasterizer that opens a PDF from a file descriptor and renders any page to
a `Bitmap`. That covers both rendering a library thumbnail and rendering
reader pages, with zero new Gradle dependencies. Trade-off accepted: no
text selection/search, since `PdfRenderer` only rasterizes — that's fine,
it's explicitly out of scope above.

## Why two feature modules

Library (import, persistence, thumbnail grid) and Reader (single-document
paging view) are separate user-facing destinations with separate
lifecycles — a library entry can be deleted while its document is closed,
and the reader only ever cares about one already-known document. Splitting
them into `:feature:library` and `:feature:pdfreader` keeps each focused
and matches the rule that feature modules never depend on each other:
`:app`'s `NavHost` is the only place that knows both exist, composing the
navigation from Library's tap callback into Reader's route.

## What changes, module by module

Follows the existing Clean Architecture layering
(`.ai/docs/01-architecture/overview.md`, `.ai/architecture-rules.json`) — same
shape as the `feature:sample` reference slice, one level down.

| Module | Change | Why |
|---|---|---|
| `:core:pdf` (**new**, `core-infrastructure` type) | `PdfRendererDataSource`: wraps `ContentResolver` + `android.graphics.pdf.PdfRenderer`. Opens a document by URI string, returns page count, renders a page to PNG bytes. Also the first Hilt binding for the `@Dispatcher(BeaderDispatchers.Io)` qualifier declared in `:core:common` (nothing provided it before this feature). | Implementation detail, same tier as `:core:network`/`:core:database`. Never referenced by `:feature:*` directly — only `:core:data` may see it. |
| `:core:database` | Add `ImportedPdfEntity` (uri, fileName, pageCount, thumbnail PNG bytes as a BLOB, imported timestamp) and `ImportedPdfDao`. Bump `BeaderDatabase` to version 2 with `fallbackToDestructiveMigration()` (pre-1.0 scaffold, no shipped installs to migrate yet). | Library persistence needs a real table; Room is already this app's source of truth for lists. |
| `:core:domain` | Add `model/PdfDocument.kt`, `model/PdfPage.kt`, `model/PdfLibraryItem.kt`, `repository/PdfRepository.kt` (open/render/close a document by URI), `repository/PdfLibraryRepository.kt` (observe/save/delete library entries), and use cases: `OpenPdfDocumentUseCase`, `LoadPdfPageUseCase`, `ClosePdfDocumentUseCase`, `ImportPdfUseCase` (coordinates both repositories — opens, renders a thumbnail, persists, then closes), `ObserveLibraryUseCase`, `DeleteLibraryItemUseCase`. | Business rules live here, framework-free. URIs and page images cross this layer as `String`/`ByteArray`, never `android.net.Uri`/`Bitmap`, per the forbidden-import rule. |
| `:core:data` | `repository/PdfRepositoryImpl.kt` (backed by `:core:pdf`), `repository/PdfLibraryRepositoryImpl.kt` (backed by `ImportedPdfDao`), both bound in `di/DataModule.kt`. New `:core:pdf` dependency. | Same role as `SampleRepositoryImpl` — the only place infra-specific types get mapped to domain models. |
| `:core:testing` | `repository/FakePdfRepository.kt`, `repository/FakePdfLibraryRepository.kt`. | Mirrors `FakeSampleRepository` so ViewModel tests don't hand-roll mocks of these interfaces. |
| `:feature:library` (**new**) | `LibraryUiState.kt`, `LibraryViewModel.kt`, `LibraryScreen.kt` (`LibraryRoute` + stateless `LibraryScreen`), `navigation/LibraryNavigation.kt`. `LibraryRoute` owns the `ActivityResultContracts.OpenDocument()` picker, `takePersistableUriPermission`, and the file's display-name lookup — platform/Activity concerns stay out of the ViewModel. | Entry point of the app. |
| `:feature:pdfreader` (**new**) | `PdfReaderUiState.kt`, `PdfReaderViewModel.kt` (reads the document URI from `SavedStateHandle`, a nav argument — no file picker here), `PdfReaderScreen.kt` (pinch-to-zoom via `detectTransformGestures` + `graphicsLayer`), `navigation/PdfReaderNavigation.kt`. | Reader only ever operates on one already-imported document. |
| `:app` | `BeaderNavHost`'s `startDestination` is now `LIBRARY_ROUTE`. Registers `libraryScreen(onOpenDocument = { uri -> navigateToPdfReader(navController, uri) })` and `pdfReaderScreen()`. `feature:sample` is no longer wired into the graph (module and its tests are untouched — it stays as the documented reference slice, just not what users see first). `MainActivityTest` updated to assert on "PDF Library" instead of "Beader Sample". | Makes the new screens reachable; features never link to each other directly. |
| `settings.gradle.kts`, `.ai/architecture-rules.json` | Register `:core:pdf`, `:feature:library`, `:feature:pdfreader` in both. | Keep the machine-readable module map in sync with reality. |

## Data flow

```
LibraryRoute (Composable)
   │ OpenDocument() picker result → uriString + display name
   ▼
LibraryViewModel.onImportDocument
   │ calls
ImportPdfUseCase                              (:core:domain)
   │           │
   │ open+render│ persist
   ▼           ▼
PdfRepository   PdfLibraryRepository           (:core:domain, interfaces only)
   ▲                ▲
   │ implements      │ implements
PdfRepositoryImpl    PdfLibraryRepositoryImpl  (:core:data)
   │ delegates to        │ delegates to
PdfRendererDataSource     ImportedPdfDao        (:core:pdf, :core:database)

--- tap a thumbnail ---

navigateToPdfReader(uri) → PdfReaderRoute → PdfReaderViewModel
   │ reads uri from SavedStateHandle, calls
OpenPdfDocumentUseCase → LoadPdfPageUseCase    (:core:domain, same PdfRepository as above)
```

`imageBytes`/`thumbnailBytes` (PNG-encoded) cross from `:core:pdf` up
through `:core:data` and `:core:domain` as plain `ByteArray` — never a
`Bitmap` — so `:core:domain` stays Android-free. Feature modules decode
the bytes back to a `Bitmap`/`ImageBitmap` only at the point of display.

## Testing plan

- `ImportPdfUseCaseTest` (`:core:domain`, JUnit 5 + MockK): the
  open-render-save-close orchestration, including the early-return path
  when opening the document fails.
- `LibraryViewModelTest`, `PdfReaderViewModelTest` (JUnit 5 + MockK +
  Turbine, using the `:core:testing` fakes): loading → success
  transitions, delete removes an item, import failure surfaces as a
  one-off event rather than replacing the list state, next/previous page
  clamping at the first/last page.
- `LibraryScreenTest`, `PdfReaderScreenTest` (Compose UI tests, stateless
  screens): loading/error/success rendering, including a real decodable
  1×1 PNG generated on-device via `Bitmap.compress` (instrumented tests
  run on a real Android runtime, so this doesn't need a fixture asset).
