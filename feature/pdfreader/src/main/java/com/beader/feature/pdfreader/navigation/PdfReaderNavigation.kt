package com.beader.feature.pdfreader.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beader.feature.pdfreader.PdfReaderRoute

private const val PDF_READER_ROUTE_BASE = "pdf_reader_route"
private const val URI_ARG = "uri"

const val PDF_READER_ROUTE = "$PDF_READER_ROUTE_BASE/{$URI_ARG}"

/** Type-safe read of this screen's [URI_ARG] nav argument from a [SavedStateHandle]. */
class PdfReaderArgs(
    savedStateHandle: SavedStateHandle,
) {
    val uri: String = Uri.decode(checkNotNull(savedStateHandle[URI_ARG]) { "Missing required '$URI_ARG' argument" })
}

/**
 * Wires this feature's screens into the app-level [androidx.navigation.NavHost].
 * The route constant and [navigateToPdfReader] are the only things `:app`
 * needs to know about — screen composables stay private to this module.
 */
fun NavGraphBuilder.pdfReaderScreen() {
    composable(
        route = PDF_READER_ROUTE,
        arguments = listOf(navArgument(URI_ARG) { type = NavType.StringType }),
    ) {
        PdfReaderRoute()
    }
}

fun navigateToPdfReader(
    navController: NavController,
    uri: String,
) {
    navController.navigate("$PDF_READER_ROUTE_BASE/${Uri.encode(uri)}")
}
