package com.example.prosodidownapp4.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Prosodi Down memakai satu skema warna terang (light) yang konsisten,
 * sesuai DESAIN.md. Dynamic color (Material You) SENGAJA tidak dipakai,
 * supaya identitas visual aplikasi tetap konsisten di semua device/user,
 * tidak berubah-ubah ikut wallpaper masing-masing HP.
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

@Composable
fun ProsodiDownApp4Theme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ProsodiLightColorScheme,
        typography = ProsodiTypography,
        content = content,
    )
}