plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "io.github.drumber.kitsune.data.source.algolia"
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
}

dependencies {
    implementation(project(":data:model"))

    // Kotlin coroutines
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)

    // Algolia Instantsearch
    implementation(libs.algolia.instantsearch.android)
    implementation(libs.algolia.instantsearch.android.paging3)
    implementation(libs.algolia.instantsearch.coroutines)

    // Kotlinx serialization
    implementation(libs.jetbrains.kotlinx.serialization)

    // Ktor client
    implementation(libs.ktor.client.okhttp)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}