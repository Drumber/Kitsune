plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.apollo.graphql.plugin)
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

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(project(":core-util"))
    implementation(project(":data:model"))

    // Kotlin coroutines
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // jsonapi-converter
    implementation(libs.jasminb.jsonapi)

    // Jackson
    implementation(libs.fasterxml.jackson.databind)
    implementation(libs.fasterxml.jackson.kotlin)

    // Retrofit
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.jackson)

    // OkHttp
    implementation(libs.squareup.okhttp3.okhttp)

    // Apollo GraphQL
    api(libs.apollo.graphql.runtime)

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
    testImplementation(libs.robolectric)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)
    testImplementation(libs.insert.koin.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)
}