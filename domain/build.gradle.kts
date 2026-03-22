import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.drumber.kitsune.domain"
    compileSdk = ProjectConfig.COMPILE_SDK

    defaultConfig {
        minSdk = ProjectConfig.MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = ProjectConfig.JAVA_VERSION
        targetCompatibility = ProjectConfig.JAVA_VERSION
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(ProjectConfig.KOTLIN_JVM_TARGET)
        languageVersion = KotlinVersion.fromVersion(ProjectConfig.KOTLIN_LANGUAGE_VERSION)
    }
}

dependencies {
    implementation(project(":core-util"))
    implementation(project(":data"))
    implementation(project(":data:model"))

    // Core dependencies
    implementation(libs.jetbrains.kotlinx.coroutines.core)

    // WorkManager
    implementation(libs.androidx.workmanager)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

    // Retrofit
    implementation(libs.squareup.retrofit2.retrofit)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}