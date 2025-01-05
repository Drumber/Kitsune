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
include(":widget")
include(":domain")
include(":data")
include(":data:common")
include(":data:source:local")
include(":data:source:jsonapi")
include(":data:source:graphql")
include(":data:source:algolia")
include(":shared")
