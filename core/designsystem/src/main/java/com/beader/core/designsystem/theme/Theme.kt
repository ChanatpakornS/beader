package com.beader.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        onPrimary = Purple20,
        secondary = PurpleGrey80,
        onSecondary = Purple10,
        tertiary = Pink80,
        onTertiary = Pink40,
        error = Error80,
        onError = Error40,
        background = Neutral10,
        surface = Neutral10,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        onPrimary = Neutral99,
        secondary = PurpleGrey40,
        onSecondary = Neutral99,
        tertiary = Pink40,
        onTertiary = Neutral99,
        error = Error40,
        onError = Neutral99,
        background = Neutral99,
        surface = Neutral99,
    )

/**
 * Single theming entry point for the whole app. Every screen renders
 * beneath [BeaderTheme] — feature modules never define their own
 * `MaterialTheme`.
 */
@Composable
fun BeaderTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            useDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BeaderTypography,
        content = content,
    )
}
