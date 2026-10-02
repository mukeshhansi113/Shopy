package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanWhiteColorScheme = lightColorScheme(
    primary = ShopyCrimson,
    onPrimary = Color.White,
    primaryContainer = ShopyPinkContainer,
    onPrimaryContainer = ShopyOnPinkContainer,
    secondary = ShopySecondary,
    onSecondary = Color.White,
    secondaryContainer = ShopySecondaryContainer,
    onSecondaryContainer = ShopyOnSecondaryContainer,
    tertiary = ShopyTeal,
    onTertiary = Color.White,
    tertiaryContainer = ShopyTealLight,
    onTertiaryContainer = Color(0xFF004D40),
    background = Color.White,
    onBackground = ShopyTextPrimary,
    surface = Color.White,
    onSurface = ShopyTextPrimary,
    surfaceVariant = ShopySurfaceVariant,
    onSurfaceVariant = ShopyTextSecondary,
    outline = ShopyOutline,
    outlineVariant = ShopyOutlineVariant
)

@Composable
fun ShopyTheme(
    darkTheme: Boolean = false, // Forced clean white theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = CleanWhiteColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.White.toArgb()
            window.navigationBarColor = Color.White.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShopyTheme(darkTheme = false, dynamicColor = false, content = content)
}
