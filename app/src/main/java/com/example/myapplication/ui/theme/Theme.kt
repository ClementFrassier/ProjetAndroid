package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Espresso,
    primaryContainer = Walnut,
    onPrimaryContainer = Ivory,
    secondary = Clay,
    onSecondary = Ivory,
    secondaryContainer = Charcoal,
    onSecondaryContainer = Linen,
    tertiary = Moss,
    onTertiary = Ivory,
    background = Espresso,
    onBackground = Ivory,
    surface = Charcoal,
    onSurface = Ivory,
    surfaceVariant = Walnut,
    onSurfaceVariant = Mist,
    outline = Slate,
    error = ErrorRose
)

private val LightColorScheme = lightColorScheme(
    primary = Walnut,
    onPrimary = Ivory,
    primaryContainer = Linen,
    onPrimaryContainer = Espresso,
    secondary = Clay,
    onSecondary = Ivory,
    secondaryContainer = ColorTokens.secondaryContainer,
    onSecondaryContainer = Espresso,
    tertiary = Moss,
    onTertiary = Ivory,
    background = Ivory,
    onBackground = Espresso,
    surface = ColorTokens.surfaceElevated,
    onSurface = Espresso,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    outline = ColorTokens.outlineSoft,
    error = ErrorRose
)

private object ColorTokens {
    val surfaceElevated = Color(0xFFFFFBF5)
    val secondaryContainer = Color(0xFFE9D6BF)
    val outlineSoft = Color(0xFFD0BDA7)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // On fige volontairement la palette pour garder la meme identite visuelle
    // sur tous les appareils, meme quand Android propose des couleurs dynamiques.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
