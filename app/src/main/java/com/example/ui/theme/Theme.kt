package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EditorialPrimaryDark,
    onPrimary = EditorialOnPrimaryDark,
    primaryContainer = EditorialPrimaryContainerDark,
    onPrimaryContainer = EditorialOnPrimaryContainerDark,
    secondary = EditorialSecondaryDark,
    onSecondary = EditorialOnSecondaryDark,
    secondaryContainer = EditorialSecondaryContainerDark,
    onSecondaryContainer = EditorialOnSecondaryContainerDark,
    tertiary = EditorialTertiaryDark,
    onTertiary = EditorialOnTertiaryDark,
    tertiaryContainer = EditorialTertiaryContainerDark,
    onTertiaryContainer = EditorialOnTertiaryContainerDark,
    background = EditorialBackgroundDark,
    surface = EditorialSurfaceDark,
    surfaceVariant = EditorialSurfaceVariantDark,
    onBackground = EditorialOnSurfaceDark,
    onSurface = EditorialOnSurfaceDark,
    onSurfaceVariant = EditorialOnSurfaceVariantDark,
    outline = EditorialOutlineDark,
    outlineVariant = EditorialOutlineVariantDark,
    error = EditorialErrorDark,
    errorContainer = EditorialErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialPrimaryLight,
    onPrimary = EditorialOnPrimaryLight,
    primaryContainer = EditorialPrimaryContainerLight,
    onPrimaryContainer = EditorialOnPrimaryContainerLight,
    secondary = EditorialSecondaryLight,
    onSecondary = EditorialOnSecondaryLight,
    secondaryContainer = EditorialSecondaryContainerLight,
    onSecondaryContainer = EditorialOnSecondaryContainerLight,
    tertiary = EditorialTertiaryLight,
    onTertiary = EditorialOnTertiaryLight,
    tertiaryContainer = EditorialTertiaryContainerLight,
    onTertiaryContainer = EditorialOnTertiaryContainerLight,
    background = EditorialBackgroundLight,
    surface = EditorialSurfaceLight,
    surfaceVariant = EditorialSurfaceVariantLight,
    onBackground = EditorialOnSurfaceLight,
    onSurface = EditorialOnSurfaceLight,
    onSurfaceVariant = EditorialOnSurfaceVariantLight,
    outline = EditorialOutlineLight,
    outlineVariant = EditorialOutlineVariantLight,
    error = EditorialErrorLight,
    errorContainer = EditorialErrorContainerLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
