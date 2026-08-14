package com.beader.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.beader.feature.sample.navigation.SAMPLE_ROUTE
import com.beader.feature.sample.navigation.sampleScreen

/**
 * Top-level [NavHost] for the app. Each feature module owns its own graph
 * builder extension (e.g. [sampleScreen]) and is wired in here — features
 * never reference each other directly.
 */
@Composable
fun BeaderNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = SAMPLE_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        sampleScreen()
    }
}
