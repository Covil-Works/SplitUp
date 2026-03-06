package com.thaicrew.splitup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAction,
    secondary = PurpleLight,
    onPrimary = LightGreyText,
    primaryContainer = PurpleAction,
    onPrimaryContainer = LightGreyText,

    background = DarkBackground,
    onBackground = LightGreyText,

    surface = SurfaceBlueGrey,
    onSurface = LightGreyText,
    surfaceVariant = SurfaceBlueGrey,
    onSurfaceVariant = LightGreyText,

    // Desativa a "elevação tonal" que clareia ou mancha as superfícies
    surfaceTint = Color.Transparent,

    // Força a barra de navegação e outros containers a usarem a nossa cor
    surfaceContainer = SurfaceBlueGrey,
    surfaceContainerHigh = SurfaceBlueGrey,
    surfaceContainerHighest = SurfaceBlueGrey,
    surfaceContainerLow = SurfaceBlueGrey,

    // Cor de fundo das barras (topbar e navbar) — edite BarBackground em Color.kt
    surfaceBright = BarBackground
)
@Composable
fun SplitUpTheme(
    // Forçando o tema escuro por padrão
    darkTheme: Boolean = true,
    // Desativando cores dinâmicas para a paleta não ser sobrescrita pelo sistema
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}