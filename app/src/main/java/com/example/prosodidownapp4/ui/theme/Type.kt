package com.example.prosodidownapp4.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.R

// ── Font Family ────────────────────────────────────────────────────────────
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_extra_light,        FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_light,              FontWeight.Light,      FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_light_italic,       FontWeight.Light,      FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_regular,            FontWeight.Normal,     FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_italic,             FontWeight.Normal,     FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_medium,             FontWeight.Medium,     FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_medium_italic,      FontWeight.Medium,     FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_semi_bold,          FontWeight.SemiBold,   FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_semi_bold_italic,   FontWeight.SemiBold,   FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_bold,               FontWeight.Bold,       FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_bold_italic,        FontWeight.Bold,       FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_extra_bold,         FontWeight.ExtraBold,  FontStyle.Normal),
    Font(R.font.plus_jakarta_sans_extra_bold_italic,  FontWeight.ExtraBold,  FontStyle.Italic),
)

// ── Typography ─────────────────────────────────────────────────────────────
val ProsodiTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
    ),
)