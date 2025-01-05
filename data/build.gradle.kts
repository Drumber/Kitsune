plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "io.github.drumber.kitsune.data"
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
    implementation(project(":shared"))
    implementation(project(":data:common"))
    implementation(project(":data:source:local"))
    implementation(project(":data:source:jsonapi"))
    implementation(project(":data:source:graphql"))

    // Core dependencies
    implementation(libs.jetbrains.kotlinx.coroutines.core)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

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