plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.gradle.api)
    implementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins.register("kitsune-plugin") {
        id = "kitsune-plugin"
        implementationClass = "io.github.drumber.plugin.CustomPlugin"
    }
}
