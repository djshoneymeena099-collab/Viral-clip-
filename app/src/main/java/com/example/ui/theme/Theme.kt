package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ViralClipDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = VoidBlack,
    primaryContainer = DeepViolet,
    onPrimaryContainer = TextPrimary,
    secondary = NeonBlue,
    onSecondary = VoidBlack,
    secondaryContainer = Color(0xFF0A2A3B),
    onSecondaryContainer = NeonBlue,
    tertiary = NeonMagenta,
    onTertiary = VoidBlack,
    tertiaryContainer = Color(0xFF3A102B),
    onTertiaryContainer = TextPrimary,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = VoidBlack,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    surfaceContainerHighest = Color(0xFF252945),
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF1F2238),
    error = AlertCrimson,
    onError = VoidBlack
)

val ViralClipShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object ViralGradients {
    val primaryAction = Brush.horizontalGradient(
        colors = listOf(DeepViolet, NeonPurple, NeonBlue)
    )

    val subtleBorder = Brush.linearGradient(
        colors = listOf(
            NeonPurple.copy(alpha = 0.55f),
            NeonBlue.copy(alpha = 0.45f),
            NeonMagenta.copy(alpha = 0.35f)
        )
    )

    val cardGlow = Brush.verticalGradient(
        colors = listOf(
            ObsidianElevated.copy(alpha = 0.92f),
            ObsidianCard.copy(alpha = 0.96f)
        )
    )

    val proGold = Brush.horizontalGradient(
        colors = listOf(NeonPurple, NeonMagenta, HighlightAmber)
    )

    val screenBackdrop = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF120A24),
            VoidBlack,
            Color(0xFF050B14)
        )
    )
}

@Composable
fun ViralClipTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ViralClipDarkColorScheme,
        typography = ViralClipTypography,
        shapes = ViralClipShapes,
        content = content
    )
}
