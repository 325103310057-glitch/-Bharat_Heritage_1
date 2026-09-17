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
    primary = SaffronPrimaryDark,
    onPrimary = SaffronOnPrimaryDark,
    primaryContainer = SaffronPrimaryContainerDark,
    onPrimaryContainer = SaffronOnPrimaryContainerDark,
    secondary = PeacockSecondaryDark,
    onSecondary = PeacockOnSecondaryDark,
    secondaryContainer = PeacockSecondaryContainerDark,
    onSecondaryContainer = PeacockOnSecondaryContainerDark,
    tertiary = MarigoldTertiaryDark,
    onTertiary = MarigoldOnTertiaryDark,
    tertiaryContainer = MarigoldTertiaryContainerDark,
    onTertiaryContainer = MarigoldOnTertiaryContainerDark,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = SaffronOnPrimary,
    primaryContainer = SaffronPrimaryContainer,
    onPrimaryContainer = SaffronOnPrimaryContainer,
    secondary = PeacockSecondary,
    onSecondary = PeacockOnSecondary,
    secondaryContainer = PeacockSecondaryContainer,
    onSecondaryContainer = PeacockOnSecondaryContainer,
    tertiary = MarigoldTertiary,
    onTertiary = MarigoldOnTertiary,
    tertiaryContainer = MarigoldTertiaryContainer,
    onTertiaryContainer = MarigoldOnTertiaryContainer,
    background = ParchmentBackground,
    onBackground = ParchmentOnSurface,
    surface = ParchmentSurface,
    onSurface = ParchmentOnSurface,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = ParchmentOnSurfaceVariant,
    error = ErrorRed
)

@Composable
fun BharatHeritageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve Indian heritage branding by default
    content: @Composable () -> Unit
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = BharatHeritageTheme(darkTheme, dynamicColor, content)
