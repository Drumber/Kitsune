plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle-api:8.4.1")
    implementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins.register("kitsune-plugin") {
        id = "kitsune-plugin"
        implementationClass = "io.github.drumber.plugin.CustomPlugin"
    }
}
