plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.apollo.graphql.plugin)
}

android {
    namespace = "io.github.drumber.kitsune.data.source.graphql"
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

apollo {
    service("kitsu") {
        packageName.set("io.github.drumber.kitsune.data.source.graphql")
        introspection {
            endpointUrl.set("https://kitsu.app/api/graphql")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }

        mapScalarToKotlinString("ISO8601DateTime")
        mapScalarToKotlinString("Date")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":data:model"))
    implementation(project(":data:common"))

    // Core dependencies
    implementation(libs.jetbrains.kotlinx.coroutines.core)

    // Apollo GraphQL
    api(libs.apollo.graphql.runtime)

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