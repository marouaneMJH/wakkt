package com.wakkt.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFF3B5BDB)

private val LightColors = lightColorScheme(
    primary = Accent,
    primaryContainer = Color(0xFFE3E8FF),
    onPrimaryContainer = Color(0xFF1B2A78),
    background = Color(0xFFFCFCFE),
    surface = Color(0xFFFCFCFE),
    surfaceContainerLow = Color(0xFFF3F4F8),
    surfaceContainerHigh = Color(0xFFE8EAF0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FA5FF),
    onPrimary = Color(0xFF0E1A5C),
    primaryContainer = Color(0xFF26346F),
    onPrimaryContainer = Color(0xFFDDE3FF),
    background = Color(0xFF101217),
    surface = Color(0xFF101217),
    surfaceContainerLow = Color(0xFF181B22),
    surfaceContainerHigh = Color(0xFF242832),
)

private val WakktShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val Base = Typography()
private val WakktTypography = Typography(
    displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.Medium),
    titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
)

@Composable
fun WakktTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = WakktShapes,
        typography = WakktTypography,
        content = content,
    )
}
