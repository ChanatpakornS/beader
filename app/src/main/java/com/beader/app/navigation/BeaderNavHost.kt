package com.beader.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.beader.feature.library.navigation.LIBRARY_ROUTE
import com.beader.feature.library.navigation.libraryScreen
import com.beader.feature.pdfreader.navigation.navigateToPdfReader
import com.beader.feature.pdfreader.navigation.pdfReaderScreen

/**
 * Top-level [NavHost] for the app. Each feature module owns its own graph
 * builder extension (e.g. [libraryScreen]) and is wired in here — features
 * never reference each other directly. Cross-feature navigation (Library ->
 * Reader) is composed here by passing a lambda into [libraryScreen].
 */
@Composable
fun BeaderNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = LIBRARY_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        libraryScreen(onOpenDocument = { uri -> navigateToPdfReader(navController, uri) })
        pdfReaderScreen(
            onNavigateToDocument = { uri -> navigateToPdfReader(navController, uri) },
            onNavigateToLibrary = {
                navController.navigate(LIBRARY_ROUTE) {
                    popUpTo(LIBRARY_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }
}
