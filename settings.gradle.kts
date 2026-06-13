pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PixelVibe"

include(":app")
include(":core:common")
include(":core:data")
include(":core:player")
include(":core:ui")
include(":feature:home")
include(":feature:recent")
include(":feature:network")
include(":feature:player")
include(":feature:settings")
