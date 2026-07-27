package io.github.drumber.kitsune.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.drumber.kitsune.constants.AppTheme

/**
 * Full Material 3 theme for Kitsune.
 *
 * @param darkTheme       Whether to use a dark color scheme. Defaults to the system setting.
 * @param dynamicColor    Enable Material You dynamic color on API 31+. When active, overrides
 *                        [variant] and [amoled].
 * @param variant         Explicit color variant.
 * @param amoled          Apply true-black surface overrides in dark mode (mirrors the XML
 *                        `Theme.Kitsune.DayNight.*Black` night override).
 * @param content         Composable content to wrap with the theme.
 */
@Composable
fun KitsuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    variant: AppTheme = AppTheme.DEFAULT,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme: ColorScheme = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(context)
        useDynamic && !darkTheme -> dynamicLightColorScheme(context)
        else -> {
            val base = colorSchemeForVariant(variant, darkTheme)
            if (amoled && darkTheme) base.withAmoledSurfaces() else base
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
