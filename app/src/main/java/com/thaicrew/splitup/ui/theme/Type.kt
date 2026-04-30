package com.thaicrew.splitup.ui.theme

import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val PoppinsFontFamily = FontFamily.SansSerif

private fun TextStyle.withPoppins() = copy(fontFamily = PoppinsFontFamily)

private val BaseTypography = MaterialTypography()

val Typography = MaterialTypography(
    displayLarge = BaseTypography.displayLarge.withPoppins(),
    displayMedium = BaseTypography.displayMedium.withPoppins(),
    displaySmall = BaseTypography.displaySmall.withPoppins(),
    headlineLarge = BaseTypography.headlineLarge.withPoppins(),
    headlineMedium = BaseTypography.headlineMedium.withPoppins(),
    headlineSmall = BaseTypography.headlineSmall.withPoppins(),
    titleLarge = BaseTypography.titleLarge.withPoppins(),
    titleMedium = BaseTypography.titleMedium.withPoppins(),
    titleSmall = BaseTypography.titleSmall.withPoppins(),
    bodyLarge = BaseTypography.bodyLarge.copy(
        fontFamily = PoppinsFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = BaseTypography.bodyMedium.withPoppins(),
    bodySmall = BaseTypography.bodySmall.withPoppins(),
    labelLarge = BaseTypography.labelLarge.withPoppins(),
    labelMedium = BaseTypography.labelMedium.withPoppins(),
    labelSmall = BaseTypography.labelSmall.withPoppins()
)
