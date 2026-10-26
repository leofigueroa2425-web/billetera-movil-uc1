package com.tuapp.billetera.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BankingColors = lightColorScheme(
    primary = BankNavy, onPrimary = Color.White,
    secondary = BankYellow, onSecondary = BankNavy,
    background = BankBackground, onBackground = BankNavy,
    surface = Color.White, onSurface = BankNavy,
    surfaceContainerHigh = Color.White, surfaceContainer = Color.White,
    surfaceTint = Color.Transparent,
    surfaceVariant = BankBackground, onSurfaceVariant = BankMuted,
    outline = BankMuted, outlineVariant = BankBorder,
    error = BankRed
)

@Composable
fun BilleteraMovilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BankingColors, typography = Typography, content = content)
}
