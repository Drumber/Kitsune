import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.legacy.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.aboutlibraries.plugin)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.kover)
    id("kitsune-plugin")
}

val screenshotMode: String by project

android {
    namespace = "io.github.drumber.kitsune"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "io.github.drumber.kitsune"
        minSdk = 26
        targetSdk = 36
        versionCode = 40
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "SCREENSHOT_MODE_ENABLED", screenshotMode)
        buildConfigField("boolean", "INSTRUMENTED_TEST", "false")
    }

    androidResources {
        generateLocaleConfig = true
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
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
        unitTests {
            // Robolectric loads a resource table per sandbox; across a large suite this
            // accumulates and exhausts the default (512 MB) fork heap, causing late-running
            // classes to fail with OutOfMemoryError. Raise the heap and recycle the JVM
            // periodically to release that memory.
            all {
                it.maxHeapSize = "2g"
                it.forkEvery = 40
            }
        }
    }
}

tasks.matching { it.name.contains("connected\\w*AndroidTest".toRegex()) }.configureEach {
    val screenShotModeEnabled = screenshotMode.toBoolean()
    doFirst {
        if (!screenShotModeEnabled) {
            // test will be skipped by 'assumeTrue(BuildConfig.SCREENSHOT_MODE_ENABLED)' in @BeforeClass
            logger.lifecycle("NOTE: SCREENSHOT_MODE_ENABLED is disabled. Instrumented test 'CaptureScreenshots.kt' will be skipped...")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        languageVersion = KotlinVersion.KOTLIN_2_2
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

aboutLibraries {
    offlineMode = true
    // Remove the "generated" timestamp to allow for reproducible builds
    excludeFields = arrayOf("generated")
}

kover {
    reports {
        filters {
            excludes {
                // Generated code (data binding, navigation safe-args, Glide, Room, KSP, etc.)
                classes(
                    "*.databinding.*",
                    "*.BR",
                    "*.BuildConfig",
                    "*Binding",
                    "*Args",
                    "*Directions",
                    "*GlideModule*",
                    "*_Factory",
                    "*_Impl",
                    "hilt_aggregated_deps.*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
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
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.insert.koin.androidx.compose)
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

    // Markwon (post content formatting)
    implementation(libs.noties.markwon.core)
    implementation(libs.noties.markwon.html)
    implementation(libs.noties.markwon.image.glide)
    implementation(libs.noties.markwon.linkify)

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
    implementation(libs.mikepenz.aboutlibraries.compose.m3)

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
    // testBuildType is "instrumented", so the test-manifest (provides the host
    // ComponentActivity for createComposeRule) must also be on that build type.
    "instrumentedImplementation"(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.insert.koin.test.junit4)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)
    testImplementation(libs.cashapp.turbine)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.squareup.okhttp3.mockwebserver)

    // fastlane screengrab
    androidTestImplementation(libs.fastlane.screengrab)
}