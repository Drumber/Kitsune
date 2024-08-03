package io.github.drumber.kitsune.utils.filter

import androidx.test.filters.CustomFilter

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@CustomFilter(filterClass = ScreenshotModeCustomFilter::class)
annotation class RequiresScreenshotMode
