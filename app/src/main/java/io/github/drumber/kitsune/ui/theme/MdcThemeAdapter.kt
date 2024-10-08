package io.github.drumber.kitsune.ui.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import io.github.drumber.kitsune.R

fun obtainColorScheme(
    context: Context
): ColorScheme {
    return context.obtainStyledAttributes(R.styleable.MdcThemeAdapter).use { ta ->
        val primary = ta.parseColor(R.styleable.MdcThemeAdapter_colorPrimary)
        val onPrimary = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnPrimary)
        val primaryInverse =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorPrimaryInverse)
        val primaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorPrimaryContainer)
        val onPrimaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnPrimaryContainer)
        val secondary = ta.parseColor(R.styleable.MdcThemeAdapter_colorSecondary)
        val onSecondary = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnSecondary)
        val secondaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSecondaryContainer)
        val onSecondaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnSecondaryContainer)
        val tertiary = ta.parseColor(R.styleable.MdcThemeAdapter_colorTertiary)
        val onTertiary = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnTertiary)
        val tertiaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorTertiaryContainer)
        val onTertiaryContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnTertiaryContainer)
        val background =
            ta.parseColor(R.styleable.MdcThemeAdapter_android_colorBackground)
        val onBackground = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnBackground)
        val surface = ta.parseColor(R.styleable.MdcThemeAdapter_colorSurface)
        val onSurface = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnSurface)
        val surfaceVariant =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceVariant)
        val onSurfaceVariant =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnSurfaceVariant)
        val elevationOverlay =
            ta.parseColor(R.styleable.MdcThemeAdapter_elevationOverlayColor)
        val surfaceInverse =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceInverse)
        val onSurfaceInverse =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnSurfaceInverse)
        val outline = ta.parseColor(R.styleable.MdcThemeAdapter_colorOutline)
        val outlineVariant =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOutlineVariant)
        val error = ta.parseColor(R.styleable.MdcThemeAdapter_colorError)
        val onError = ta.parseColor(R.styleable.MdcThemeAdapter_colorOnError)
        val errorContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorErrorContainer)
        val onErrorContainer =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorOnErrorContainer)
        val scrimBackground = ta.parseColor(R.styleable.MdcThemeAdapter_scrimBackground)
        val surfaceBright = ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceBright)
        val surfaceContainer = ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceContainer)
        val surfaceContainerHigh =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceContainerHigh)
        val surfaceContainerHighest =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceContainerHighest)
        val surfaceContainerLow =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceContainerLow)
        val surfaceContainerLowest =
            ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceContainerLowest)
        val surfaceDim = ta.parseColor(R.styleable.MdcThemeAdapter_colorSurfaceDim)

        val isLightTheme = ta.getBoolean(R.styleable.MdcThemeAdapter_isLightTheme, true)

        if (isLightTheme) {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                inversePrimary = primaryInverse,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                surfaceTint = elevationOverlay,
                inverseSurface = surfaceInverse,
                inverseOnSurface = onSurfaceInverse,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = scrimBackground,
                surfaceBright = surfaceBright,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceDim = surfaceDim,
            )
        } else {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                inversePrimary = primaryInverse,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                surfaceTint = elevationOverlay,
                inverseSurface = surfaceInverse,
                inverseOnSurface = onSurfaceInverse,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = scrimBackground,
                surfaceBright = surfaceBright,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainerLowest = surfaceContainerLowest,
                surfaceDim = surfaceDim,
            )
        }
    }
}

private fun TypedArray.parseColor(@StyleableRes index: Int): Color {
    return Color(getColorOrThrow(index))
}