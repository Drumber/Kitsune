pluginManagement {
    includeBuild("plugin")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "Kitsune"
include(":app")
include(":domain")
include(":adapters")
include(":data")
include(":widget")
include(":data:source:local")
include(":data:source:jsonapi")
include(":data:source:graphql")
