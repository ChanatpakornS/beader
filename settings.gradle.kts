pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
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

rootProject.name = "Beader"

// ---------------------------------------------------------------------------
// App
// ---------------------------------------------------------------------------
include(":app")

// ---------------------------------------------------------------------------
// Core modules — shared, feature-agnostic building blocks.
// Dependency direction: core:* modules never depend on :feature:* or :app.
// ---------------------------------------------------------------------------
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")

// ---------------------------------------------------------------------------
// Feature modules — one per user-facing feature. Feature modules may depend
// on core:* modules but must never depend on each other or on :app.
// ---------------------------------------------------------------------------
include(":feature:sample")
