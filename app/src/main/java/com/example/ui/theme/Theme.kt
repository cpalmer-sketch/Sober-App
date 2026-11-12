package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Gold500,
    onPrimary = Color(0xFF261800),
    primaryContainer = Color(0xFF533B00),
    onPrimaryContainer = Gold100,
    secondary = Emerald500,
    onSecondary = Color(0xFF003823),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Emerald200,
    tertiary = Indigo500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF282B70),
    onTertiaryContainer = Indigo200,
    background = DarkBg,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorder,
    error = Rose500,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Gold600,
    onPrimary = Color.White,
    primaryContainer = Gold100,
    onPrimaryContainer = Color(0xFF452B00),
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald200,
    onSecondaryContainer = Color(0xFF003823),
    tertiary = Indigo600,
    onTertiary = Color.White,
    tertiaryContainer = Indigo200,
    onTertiaryContainer = Color(0xFF1E1E62),
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder,
    error = Rose500,
    onError = Color.White
)

@Composable
fun SoberRewardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent recovery branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = SoberRewardsTheme(darkTheme, dynamicColor, content)
