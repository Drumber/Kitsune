package io.github.drumber.kitsune.fastlane

import io.github.drumber.kitsune.preference.AppTheme

data class CaptureConfig(
    val name: String,
    val isDarkMode: Boolean,
    val appTheme: AppTheme,
    val targets: Set<ScreenshotTarget>,
)

enum class ScreenshotTarget {
    HOME_SCREEN,
    SEARCH_SCREEN,
    DETAILS_SCREEN,
    DETAILS_RATINGS_SCREEN,
}

val captureScreenshotsConfig = setOf(
    CaptureConfig(
        name = "0_light",
        isDarkMode = false,
        appTheme = AppTheme.DEFAULT,
        targets = setOf(
            ScreenshotTarget.HOME_SCREEN,
            ScreenshotTarget.SEARCH_SCREEN,
            ScreenshotTarget.DETAILS_SCREEN,
            ScreenshotTarget.DETAILS_RATINGS_SCREEN,
        )
    ),
    CaptureConfig(
        name = "1_dark",
        isDarkMode = true,
        appTheme = AppTheme.DEFAULT,
        targets = setOf(
            ScreenshotTarget.HOME_SCREEN,
            ScreenshotTarget.DETAILS_SCREEN,
        )
    ),
    CaptureConfig(
        name = "2_dark_purple",
        isDarkMode = true,
        appTheme = AppTheme.PURPLE,
        targets = setOf(ScreenshotTarget.HOME_SCREEN)
    ),
    CaptureConfig(
        name = "3_dark_blue",
        isDarkMode = true,
        appTheme = AppTheme.BLUE,
        targets = setOf(ScreenshotTarget.HOME_SCREEN)
    ),
)
