rootProject.name = "plugin"

pluginManagement {
    repositories {
        maven { url = uri("") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("") }
        google()
        mavenCentral()
    }
}
