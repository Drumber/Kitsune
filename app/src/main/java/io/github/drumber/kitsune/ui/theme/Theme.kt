package io.github.drumber.kitsune.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.accompanist.themeadapter.material3.createMdc3Theme

@Composable
fun KitsuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (_, typography, shapes) = createMdc3Theme(
        context = context,
        layoutDirection = layoutDirection,
        readColorScheme = false
    )

    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        else -> obtainColorScheme(context)
    }

    val libraryStatusColors = getLibraryStatusColors(darkTheme, colorScheme)

    CompositionLocalProvider(LocalLibraryStatusColors provides libraryStatusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography!!,
            shapes = shapes!!,
            content = content
        )
    }
}
