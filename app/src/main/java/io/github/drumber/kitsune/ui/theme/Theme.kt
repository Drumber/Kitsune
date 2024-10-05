package io.github.drumber.kitsune.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
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
    var (colorScheme, typography, shapes) = createMdc3Theme(
        context = context,
        layoutDirection = layoutDirection
    )

    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    colorScheme = when {
        useDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        else -> colorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme!!,
        typography = typography!!,
        shapes = shapes!!,
        content = content
    )
}
