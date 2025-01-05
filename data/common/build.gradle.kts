plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = ProjectConfig.JAVA_VERSION
    targetCompatibility = ProjectConfig.JAVA_VERSION
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.KOTLIN_JVM_TARGET)
    }
}