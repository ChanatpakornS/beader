package com.beader.feature.sample.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.beader.feature.sample.SampleRoute

const val SAMPLE_ROUTE = "sample_route"

/**
 * Wires this feature's screens into the app-level [androidx.navigation.NavHost].
 * The route constant is the only thing `:app` needs to know about — screen
 * composables and their arguments stay private to this module.
 */
fun NavGraphBuilder.sampleScreen() {
    composable(route = SAMPLE_ROUTE) {
        SampleRoute()
    }
}
