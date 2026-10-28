package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaContainer,
    onPrimary = TerracottaPrimaryDark,
    primaryContainer = TerracottaPrimary,
    onPrimaryContainer = TerracottaContainer,
    secondary = IndigoContainer,
    onSecondary = IndigoSecondary,
    background = DarkBackground,
    surface = DarkCardSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = OnTerracotta,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaPrimaryDark,
    secondary = IndigoSecondary,
    onSecondary = OnIndigo,
    secondaryContainer = IndigoContainer,
    background = WarmCreamBackground,
    surface = CardSurfaceWhite,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    outline = CardBorderColor
)

@Composable
fun DigitalSaathiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
