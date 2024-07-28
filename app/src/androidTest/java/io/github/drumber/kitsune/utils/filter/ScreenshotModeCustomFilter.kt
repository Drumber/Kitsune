package io.github.drumber.kitsune.utils.filter

import androidx.test.filters.AbstractFilter
import io.github.drumber.kitsune.BuildConfig
import org.junit.runner.Description

class ScreenshotModeCustomFilter : AbstractFilter() {

    override fun describe(): String {
        return "only run test if BuildConfig property 'SCREENSHOT_MODE_ENABLED' is 'true'"
    }

    override fun evaluateTest(description: Description?): Boolean {
       return BuildConfig.SCREENSHOT_MODE_ENABLED
    }
}