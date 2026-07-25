// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.legacy.kapt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.parcelize) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.aboutlibraries.plugin) apply false
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
    parallel = true
    // Run on the whole project source tree from the root task.
    source.setFrom(
        files(
            "app/src/main/java",
            "app/src/test/java",
            "app/src/androidTest/java"
        )
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
