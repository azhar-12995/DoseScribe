package com.azhar.dosescribe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandBlueDark,
    onSecondary = Color.White,
    tertiary = CardGreen,
    onTertiary = Color.White,
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE6EAF0),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EAF0),
    surfaceVariant = Color(0xFF1E242C),
    onSurfaceVariant = Color(0xFFB6BFCC),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF3A4250),
    outlineVariant = Color(0xFF2A3140)
)

private val LightColorPalette = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandBlueDark,
    onSecondary = Color.White,
    secondaryContainer = BrandBlueSoft,
    onSecondaryContainer = BrandBlueDark,
    tertiary = CardGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessGreenLight,
    onTertiaryContainer = SuccessGreen,
    background = SurfaceBg,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRed,
    outline = DividerSoft,
    outlineVariant = DividerSoft
)

@Composable
fun DoseScribeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
