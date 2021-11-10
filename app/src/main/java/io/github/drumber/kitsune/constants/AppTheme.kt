package io.github.drumber.kitsune.constants

import androidx.annotation.StyleRes
import io.github.drumber.kitsune.R

enum class AppTheme(@StyleRes val themeRes: Int) {
    DEFAULT(R.style.Theme_Kitsune_DayNight),
    PURPLE(R.style.Theme_Kitsune_DayNight_Purple)
}