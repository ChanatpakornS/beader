package com.beader.feature.pdfreader.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beader.feature.pdfreader.PdfReaderRoute
import java.util.Base64

private const val PDF_READER_ROUTE_BASE = "pdf_reader_route"
private const val URI_ARG = "uri"

const val PDF_READER_ROUTE = "$PDF_READER_ROUTE_BASE/{$URI_ARG}"

/**
 * Content URIs contain characters ('/', ':') that are ambiguous inside a nav
 * route path segment, and it's unclear whether Navigation's own route
 * matching additionally percent-decodes segments before they land in a
 * [SavedStateHandle]. Base64 URL-safe encoding sidesteps both problems: its
 * alphabet is only `[A-Za-z0-9_-]`, so nothing in the route-matching or
 * argument-parsing pipeline can alter it, and — unlike `android.net.Uri`'s
 * codec — [Base64] has no dependency on the Android framework, so it works
 * in local JVM unit tests too.
 */
internal fun encodeUriArg(uri: String): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(uri.toByteArray(Charsets.UTF_8))

private fun decodeUriArg(value: String): String = String(Base64.getUrlDecoder().decode(value), Charsets.UTF_8)

/** Type-safe read of this screen's [URI_ARG] nav argument from a [SavedStateHandle]. */
class PdfReaderArgs(
    savedStateHandle: SavedStateHandle,
) {
    val uri: String = decodeUriArg(checkNotNull(savedStateHandle[URI_ARG]) { "Missing required '$URI_ARG' argument" })
}

/**
 * Wires this feature's screens into the app-level [androidx.navigation.NavHost].
 * The route constant and [navigateToPdfReader] are the only things `:app`
 * needs to know about — screen composables stay private to this module.
 * [onNavigateToDocument]/[onNavigateToLibrary] are supplied by `:app` since
 * features never reference each other's routes directly.
 */
fun NavGraphBuilder.pdfReaderScreen(
    onNavigateToDocument: (uri: String) -> Unit,
    onNavigateToLibrary: () -> Unit,
) {
    composable(
        route = PDF_READER_ROUTE,
        arguments = listOf(navArgument(URI_ARG) { type = NavType.StringType }),
    ) {
        PdfReaderRoute(onNavigateToDocument = onNavigateToDocument, onNavigateToLibrary = onNavigateToLibrary)
    }
}

fun navigateToPdfReader(
    navController: NavController,
    uri: String,
) {
    navController.navigate("$PDF_READER_ROUTE_BASE/${encodeUriArg(uri)}")
}
