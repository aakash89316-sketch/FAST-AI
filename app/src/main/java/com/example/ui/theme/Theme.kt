package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FastAiDarkColorScheme = darkColorScheme(
    primary = FastAiHeaderBlue,
    onPrimary = FastAiOnHeaderBlue,
    primaryContainer = FastAiUserBubble,
    onPrimaryContainer = FastAiTextPrimary,
    secondary = FastAiAiBubble,
    onSecondary = FastAiTextPrimary,
    secondaryContainer = FastAiSurfaceElevated,
    onSecondaryContainer = FastAiTextSecondary,
    background = FastAiBackground,
    onBackground = FastAiTextPrimary,
    surface = FastAiSurface,
    onSurface = FastAiTextPrimary,
    surfaceVariant = FastAiInputSurface,
    onSurfaceVariant = FastAiTextMuted,
    outline = FastAiBorderSubtle,
    error = FastAiErrorBorder,
    errorContainer = FastAiErrorContainer,
    onErrorContainer = FastAiOnErrorContainer
)

@Composable
fun FastAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FastAiDarkColorScheme,
        typography = FastAiTypography,
        content = content
    )
}
