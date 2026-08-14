package com.beader.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.beader.feature.library.LibraryRoute

const val LIBRARY_ROUTE = "library_route"

/**
 * Wires this feature's screens into the app-level [androidx.navigation.NavHost].
 * [onOpenDocument] is supplied by `:app` and navigates into `:feature:pdfreader`
 * — features never reference each other directly.
 */
fun NavGraphBuilder.libraryScreen(onOpenDocument: (uri: String) -> Unit) {
    composable(route = LIBRARY_ROUTE) {
        LibraryRoute(onOpenDocument = onOpenDocument)
    }
}
