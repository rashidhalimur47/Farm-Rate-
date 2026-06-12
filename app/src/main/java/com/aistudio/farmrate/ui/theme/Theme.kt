package com.aistudio.farmrate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color Definitions
val PrimaryGreen = Color(0xFF1B5E20)       // Deep Forest Green
val SecondaryGreen = Color(0xFF2E7D32)     // Organic Medium Green
val AccentGreen = Color(0xFF4CAF50)        // Bright Fresh Green
val LightBackground = Color(0xFFF7FBF8)    // Off-white organic ground tint
val LightSurface = Color(0xFFFFFFFF)       // Crisp White Cards
val TextPrimary = Color(0xFF1A301E)        // Deep Greenish Dark-brown for readability
val TextSecondary = Color(0xFF536355)      // Soft Muted Sage For Secondary Info
val BorderColor = Color(0xFFE3ECE5)        // Light Greenish Border
val WarningBg = Color(0xFFFFF3CD)          // Soft amber warning for offline banner
val WarningText = Color(0xFF856404)        // Amber dark text

val DarkPrimaryGreen = Color(0xFF81C784)
val DarkBackground = Color(0xFF111D13)
val DarkSurface = Color(0xFF1E2E20)
val DarkTextPrimary = Color(0xFFE8F5E9)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen,
    tertiary = AccentGreen,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BorderColor,
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    secondary = AccentGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF2C3E2E),
    onSurfaceVariant = Color(0xFFA5D6A7)
)

val FarmTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp,
        color = PrimaryGreen
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = PrimaryGreen
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = TextSecondary
    )
)

@Composable
fun FarmRateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FarmTypography,
        content = content
    )
}
