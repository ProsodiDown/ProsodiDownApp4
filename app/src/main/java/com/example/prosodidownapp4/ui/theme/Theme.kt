package com.example.prosodidownapp4.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Prosodi Down memakai satu skema warna terang (light) yang konsisten,
 * sesuai DESAIN.md. 
 */
private val ProsodiLightColorScheme = lightColorScheme(
    primary = ProsodiPrimary,
    onPrimary = ProsodiOnPrimary,
    secondary = ProsodiSecondary,
    onSecondary = ProsodiOnPrimary,
    tertiary = ProsodiAccent,
    onTertiary = ProsodiOnPrimary,
    background = ProsodiBackground,
    onBackground = ProsodiOnBackground,
    surface = ProsodiBackground,
    onSurface = ProsodiOnBackground,
)

private val ProsodiDarkColorScheme = darkColorScheme(
    primary = ProsodiPrimaryDark,
    onPrimary = Color(0xFF07111F),
    secondary = ProsodiSecondaryDark,
    onSecondary = Color(0xFF07111F),
    tertiary = ProsodiAccentDark,
    onTertiary = Color(0xFF07111F),
    background = ProsodiBackgroundDark,
    onBackground = ProsodiOnBackgroundDark,
    surface = ProsodiSurfaceDark,
    onSurface = ProsodiOnSurfaceDark,
)

@Composable
fun ProsodiDownApp4Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ProsodiDarkColorScheme else ProsodiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProsodiTypography,
        content = content,
    )
}
