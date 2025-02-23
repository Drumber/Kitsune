plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.aboutlibraries.plugin)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("kitsune-plugin")
}

val screenshotMode: String by project

android {
    namespace = "io.github.drumber.kitsune"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "io.github.drumber.kitsune"
        minSdk = 26
        targetSdk = 35
        versionCode = 38
        versionName = "2.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "SCREENSHOT_MODE_ENABLED", screenshotMode)
        buildConfigField("boolean", "INSTRUMENTED_TEST", "false")
    }

    androidResources {
        generateLocaleConfig = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            vcsInfo.include = false
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }

        create("instrumented") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".instrumented"
            buildConfigField("boolean", "INSTRUMENTED_TEST", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources.excludes += "META-INF/*.kotlin_module"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        animationsDisabled = true
        testBuildType = "instrumented"
    }
}

aboutLibraries {
    offlineMode = true
    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
}

dependencies {
    // Android core and support libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.accompanist.themeadapter.material3)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // SwipeRefresh layout
    implementation(libs.androidx.swiperefreshlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Preference
    implementation(libs.androidx.preference.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // WorkManager
    implementation(libs.androidx.workmanager)

    // Material
    implementation(libs.google.android.material)

    // Glance AppWidget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.preview)

    // Kotlin coroutines
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // ViewPager
    implementation(libs.androidx.viewpager2)

    // Glide
    implementation(libs.bumptech.glide)
    ksp(libs.bumptech.glide.ksp)
    implementation(libs.bumptech.glide.okhttp3)
    implementation(libs.bumptech.glide.compose)

    // Koin DI
    implementation(libs.insert.koin.android)
    implementation(libs.insert.koin.androidx.navigation)

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
    implementation(libs.squareup.okhttp3.logging)

    // Algolia Instantsearch
    implementation(libs.algolia.instantsearch.android)
    implementation(libs.algolia.instantsearch.android.paging3)
    implementation(libs.algolia.instantsearch.coroutines)

    // Kotlinx serialization
    implementation(libs.jetbrains.kotlinx.serialization)

    // Ktor client
    implementation(libs.ktor.client.okhttp)

    // Kotpref
    implementation(libs.chibatching.kotpref)
    implementation(libs.chibatching.kotpref.enum)
    implementation(libs.chibatching.kotpref.livedata)

    // Security Crypto
    implementation(libs.androidx.security.crypto)

    // TreeView
    implementation(libs.bmelnychuk.treeview)

    // Expandable text view
    implementation(libs.blogc.expandabletextview)

    // CircleImageView
    implementation(libs.hdodenhof.circleimageview)

    // Material Rating Bar
    implementation(libs.zhanghai.materialratingbar)

    // MPAndroidCharts
    implementation(libs.philjay.mpandroidchart)

    // Photo View
    implementation(libs.chrisbanes.photoview)

    // Hauler Gesture
    implementation(libs.futured.hauler)
    implementation(libs.futured.hauler.databinding)

    // AboutLibraries
    implementation(libs.mikepenz.aboutlibraries.core)
    implementation(libs.mikepenz.aboutlibraries)

    // LeakCanary
    debugImplementation(libs.squareup.leakcanary)

    // Glide Transformations (only used for demo screenshots)
    if (screenshotMode.toBoolean()) {
        implementation(libs.wasabeef.glide.transformations)
    }

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.tngtech.archunit.junit4)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)

    // Compose tests
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.insert.koin.test.junit4)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)

    // fastlane screengrab
    androidTestImplementation(libs.fastlane.screengrab)
}