package com.cmptemplate.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cmptemplate.core.designsystem.generated.resources.Res
import com.cmptemplate.core.designsystem.generated.resources.exo2_bold
import com.cmptemplate.core.designsystem.generated.resources.exo2_extrabold
import com.cmptemplate.core.designsystem.generated.resources.exo2_semibold
import org.jetbrains.compose.resources.Font

/** ControlFix brand palette (the same values LockdownControl and GameController ship). */
object BrandColors {
    val Lime = Color(0xFFA9F000)
    val LimeDeep = Color(0xFF7CC800)
    /** Lime readable on light surfaces (about 3.9:1); plain lime is for dark grounds only. */
    val LimeOnLight = Color(0xFF5B8A00)
    val Mint = Color(0xFF31DCC0)
    val Teal = Color(0xFF17B3A4)
    val TealDeep = Color(0xFF0E8E93)
    val Navy = Color(0xFF101D33)
    val Ink = Color(0xFF070C17)
    val Paper = Color(0xFFE9F2F7)
    val Steel = Color(0xFF7FA0B3)
    val Critical = Color(0xFFFF5C5C)
    val Warning = Color(0xFFFFB547)
}

private fun navyTint(fraction: Float) = lerp(BrandColors.Navy, BrandColors.Paper, fraction)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = BrandColors.Lime,
    onPrimary = BrandColors.Ink,
    primaryContainer = navyTint(0.16f),
    onPrimaryContainer = BrandColors.Lime,
    secondary = BrandColors.Teal,
    onSecondary = BrandColors.Ink,
    secondaryContainer = navyTint(0.12f),
    onSecondaryContainer = BrandColors.Mint,
    tertiary = BrandColors.Mint,
    onTertiary = BrandColors.Ink,
    background = BrandColors.Ink,
    onBackground = BrandColors.Paper,
    surface = BrandColors.Navy,
    onSurface = BrandColors.Paper,
    surfaceVariant = navyTint(0.12f),
    onSurfaceVariant = BrandColors.Steel,
    surfaceTint = BrandColors.Lime,
    surfaceContainerLowest = BrandColors.Ink,
    surfaceContainerLow = navyTint(0.04f),
    surfaceContainer = navyTint(0.08f),
    surfaceContainerHigh = navyTint(0.12f),
    surfaceContainerHighest = navyTint(0.16f),
    outline = BrandColors.Steel,
    outlineVariant = navyTint(0.20f),
    error = BrandColors.Critical,
    onError = BrandColors.Ink,
    errorContainer = lerp(BrandColors.Navy, BrandColors.Critical, 0.28f),
    onErrorContainer = BrandColors.Paper,
)

internal val LightColors: ColorScheme = lightColorScheme(
    primary = BrandColors.LimeOnLight,
    onPrimary = Color.White,
    primaryContainer = lerp(BrandColors.Paper, BrandColors.LimeDeep, 0.25f),
    onPrimaryContainer = BrandColors.Ink,
    secondary = BrandColors.TealDeep,
    onSecondary = Color.White,
    secondaryContainer = lerp(BrandColors.Paper, BrandColors.TealDeep, 0.18f),
    onSecondaryContainer = BrandColors.Ink,
    tertiary = BrandColors.TealDeep,
    onTertiary = Color.White,
    background = BrandColors.Paper,
    onBackground = BrandColors.Ink,
    surface = Color.White,
    onSurface = BrandColors.Ink,
    surfaceVariant = lerp(Color.White, BrandColors.Steel, 0.15f),
    onSurfaceVariant = BrandColors.Navy,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = lerp(Color.White, BrandColors.Paper, 0.5f),
    surfaceContainer = BrandColors.Paper,
    surfaceContainerHigh = lerp(BrandColors.Paper, BrandColors.Steel, 0.10f),
    surfaceContainerHighest = lerp(BrandColors.Paper, BrandColors.Steel, 0.18f),
    outline = BrandColors.Steel,
    outlineVariant = lerp(BrandColors.Paper, BrandColors.Steel, 0.35f),
    error = Color(0xFFC62828),
    onError = Color.White,
)

/** Exo 2 (SIL OFL, docs/branding/Exo2-OFL.txt) for headings, titles and labels. */
@Composable
fun exo2FontFamily(): FontFamily = FontFamily(
    Font(Res.font.exo2_semibold, FontWeight.SemiBold),
    Font(Res.font.exo2_bold, FontWeight.Bold),
    Font(Res.font.exo2_extrabold, FontWeight.ExtraBold),
)

/** Brand type: Exo 2 for display/headline/title/label, body text stays on the platform font. */
@Composable
private fun brandTypography(): Typography {
    val exo = exo2FontFamily()
    return remember(exo) {
        Typography().run {
            fun TextStyle.exo(weight: FontWeight) = copy(fontFamily = exo, fontWeight = weight)
            copy(
                displayLarge = displayLarge.exo(FontWeight.ExtraBold),
                displayMedium = displayMedium.exo(FontWeight.ExtraBold),
                displaySmall = displaySmall.exo(FontWeight.ExtraBold),
                headlineLarge = headlineLarge.exo(FontWeight.ExtraBold),
                headlineMedium = headlineMedium.exo(FontWeight.ExtraBold),
                headlineSmall = headlineSmall.exo(FontWeight.Bold),
                titleLarge = titleLarge.exo(FontWeight.SemiBold),
                titleMedium = titleMedium.exo(FontWeight.SemiBold),
                titleSmall = titleSmall.exo(FontWeight.SemiBold),
                labelLarge = labelLarge.exo(FontWeight.SemiBold),
            )
        }
    }
}

/**
 * Single theme entry point every app/feature composable root wraps itself in. Dark navy or light
 * paper; the app passes [darkTheme] from [ThemeManager] (the user's choice), tests pass it directly.
 * Keep colours here, not per feature, so a rebrand is a one-file change.
 */
@Composable
fun CMPTemplateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SystemBarsEffect(darkTheme)
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = brandTypography(),
        content = content,
    )
}

/**
 * Status/navigation bar icons must contrast with the APP's theme, not the device's: with an in-app
 * light/dark choice the two differ, and the bar icons vanish (LockdownControl). Android only.
 */
@Composable
internal expect fun SystemBarsEffect(darkTheme: Boolean)
