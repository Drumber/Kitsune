package io.github.drumber.kitsune.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.AppTheme
import io.github.drumber.kitsune.util.extensions.getResourceId

object KitsuneWidgetTheme {

    fun Context.applyTheme(appTheme: AppTheme) {
        setTheme(appTheme.themeRes)
    }

    @Composable
    fun getColors(useDynamicColorTheme: Boolean): ColorProviders {
        if (useDynamicColorTheme) {
            return GlanceTheme.colors
        }
        return getColorSchemeFromAppTheme(LocalContext.current)
    }

    @SuppressLint("RestrictedApi")
    private fun getColorSchemeFromAppTheme(context: Context): ColorProviders {
        return colorProviders(
            primary = ColorProvider(context.theme.getResourceId(R.attr.colorPrimary)),
            onPrimary = ColorProvider(context.theme.getResourceId(R.attr.colorOnPrimary)),
            primaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorPrimaryContainer)),
            onPrimaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorOnPrimaryContainer)),
            secondary = ColorProvider(context.theme.getResourceId(R.attr.colorSecondary)),
            onSecondary = ColorProvider(context.theme.getResourceId(R.attr.colorOnSecondary)),
            secondaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorSecondaryContainer)),
            onSecondaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorOnSecondaryContainer)),
            tertiary = ColorProvider(context.theme.getResourceId(R.attr.colorTertiary)),
            onTertiary = ColorProvider(context.theme.getResourceId(R.attr.colorOnTertiary)),
            tertiaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorTertiaryContainer)),
            onTertiaryContainer = ColorProvider(context.theme.getResourceId(R.attr.colorOnTertiaryContainer)),
            error = ColorProvider(context.theme.getResourceId(R.attr.colorError)),
            errorContainer = ColorProvider(context.theme.getResourceId(R.attr.colorErrorContainer)),
            onError = ColorProvider(context.theme.getResourceId(R.attr.colorOnError)),
            onErrorContainer = ColorProvider(context.theme.getResourceId(R.attr.colorOnErrorContainer)),
            background = ColorProvider(context.theme.getResourceId(R.attr.colorSurface)),
            onBackground = ColorProvider(context.theme.getResourceId(R.attr.colorOnBackground)),
            surface = ColorProvider(context.theme.getResourceId(R.attr.colorSurface)),
            onSurface = ColorProvider(context.theme.getResourceId(R.attr.colorOnSurface)),
            surfaceVariant = ColorProvider(context.theme.getResourceId(R.attr.colorSurfaceVariant)),
            onSurfaceVariant = ColorProvider(context.theme.getResourceId(R.attr.colorOnSurfaceVariant)),
            outline = ColorProvider(context.theme.getResourceId(R.attr.colorOutline)),
            inverseOnSurface = ColorProvider(context.theme.getResourceId(R.attr.colorOnSurfaceInverse)),
            inverseSurface = ColorProvider(context.theme.getResourceId(R.attr.colorSurfaceInverse)),
            inversePrimary = ColorProvider(context.theme.getResourceId(R.attr.colorPrimaryInverse)),
            widgetBackground = ColorProvider(context.theme.getResourceId(R.attr.colorSurface)),
        )
    }
}