package com.thaicrew.splitup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAction,
    secondary = PurpleLight,
    tertiary = PurpleLight,

    onPrimary = LightGreyText,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,

    primaryContainer = Color(0xFF4A3FB3),
    onPrimaryContainer = LightGreyText,
    secondaryContainer = Color(0xFF323656),
    onSecondaryContainer = LightGreyText,

    background = DarkBackground,
    onBackground = LightGreyText,

    surface = SurfaceCard,
    onSurface = LightGreyText,
    surfaceVariant = SurfaceBlueGrey,
    onSurfaceVariant = PurpleLight,
    outline = SoftOutline,
    outlineVariant = SoftOutline.copy(alpha = 0.7f),

    error = DestructiveAccent,
    onError = DarkBackground,
    errorContainer = Color(0xFF4B1F2A),
    onErrorContainer = LightGreyText,

    // Neutraliza a tonal elevation para preservar a leitura da paleta customizada.
    surfaceTint = Color.Transparent,

    // Hierarquia entre fundo, barra e cards para melhorar profundidade.
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = SurfaceCard,
    surfaceContainer = SurfaceCard,
    surfaceContainerHigh = SurfaceCard,
    surfaceContainerHighest = SurfaceCard,
    surfaceDim = BarBackground,
    surfaceBright = BarBackground
)

@Composable
fun SplitUpTheme(
    // Mantém o tema escuro como padrão.
    darkTheme: Boolean = true,
    // Mantém desativado para não sobrescrever a identidade visual.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
