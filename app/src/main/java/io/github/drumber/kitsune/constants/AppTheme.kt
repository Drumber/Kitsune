package io.github.drumber.kitsune.constants

import androidx.annotation.StyleRes
import io.github.drumber.kitsune.R

enum class AppTheme(@StyleRes val themeRes: Int, @StyleRes val blackThemeRes: Int) {
    DEFAULT(R.style.Theme_Kitsune_DayNight, R.style.Theme_Kitsune_DayNight_Black),
    PURPLE(R.style.Theme_Kitsune_DayNight_Purple, R.style.Theme_Kitsune_DayNight_Purple_Black),
    BLUE(R.style.Theme_Kitsune_DayNight_Blue, R.style.Theme_Kitsune_DayNight_Blue_Black),
    GREEN(R.style.Theme_Kitsune_DayNight_Green, R.style.Theme_Kitsune_DayNight_Green_Black),
}
