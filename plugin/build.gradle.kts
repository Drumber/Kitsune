import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
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
