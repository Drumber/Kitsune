plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.github.drumber.kitsune.data.source.local"
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

    kotlinOptions {
        jvmTarget = ProjectConfig.KOTLIN_JVM_TARGET
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":data:model"))
    implementation(project(":data:common"))

    // Core dependencies
    implementation(libs.jetbrains.kotlinx.coroutines.core)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // Jackson
    implementation(libs.fasterxml.jackson.databind)
    implementation(libs.fasterxml.jackson.kotlin)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}