package io.github.drumber.kitsune.config

import android.os.Build

object ImageConfig {
    /** Use WebP images if available. [android.graphics.ImageDecoder] is only available on API 28+. */
    val useWebp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}