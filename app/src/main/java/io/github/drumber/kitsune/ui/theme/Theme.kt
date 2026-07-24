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
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.accompanist.themeadapter.material3.createMdc3Theme
import io.github.drumber.kitsune.constants.AppTheme

/**
 * Full Material 3 theme for Kitsune.
 *
 * Backward-compatible: existing callers that pass only [darkTheme] and [dynamicColor] continue
 * to work identically (MDC bridge path).
 *
 * @param darkTheme       Whether to use a dark color scheme. Defaults to the system setting.
 * @param dynamicColor    Enable Material You dynamic color on API 31+. When active, overrides
 *                        [variant] and [amoled].
 * @param variant         Explicit color variant. When `null` (default) the active MDC/XML theme
 *                        is read via [obtainColorScheme] — Compose islands inside XML screens
 *                        automatically inherit the host activity's theme. Pass a non-null value
 *                        to use the Kotlin-defined color schemes and decouple from the XML theme.
 * @param amoled          Apply true-black surface overrides in dark mode (mirrors the XML
 *                        `Theme.Kitsune.DayNight.*Black` night override). Only takes effect
 *                        when [variant] is non-null and [darkTheme] is `true`; when using the
 *                        MDC bridge the AMOLED overrides are already baked into the XML theme.
 * @param content         Composable content to wrap with the theme.
 */
@Composable
fun KitsuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    variant: AppTheme? = null,
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (_, typography, shapes) = createMdc3Theme(
        context = context,
        layoutDirection = layoutDirection,
        readColorScheme = false
    )

    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme: ColorScheme = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(context)
        useDynamic && !darkTheme -> dynamicLightColorScheme(context)
        variant != null -> {
            val base = colorSchemeForVariant(variant, darkTheme)
            if (amoled && darkTheme) base.withAmoledSurfaces() else base
        }
        // MDC bridge: read colors from the XML/MDC theme applied to the host Activity.
        // The XML theme already includes AMOLED overrides when oledBlackMode is on.
        else -> obtainColorScheme(context)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography ?: Typography(),
        shapes = shapes ?: Shapes(),
        content = content
    )
}

/**
 * Variant of [KitsuneTheme] for Compose islands embedded inside XML/Fragment-based screens.
 * Always reads colors from the active MDC/XML theme; dynamic color is disabled by default
 * because the Activity already handles it via [com.google.android.material.color.DynamicColors].
 */
@Composable
fun KitsuneMdcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = KitsuneTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    variant = null,
    amoled = false,
    content = content
)
