pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.android.settings") version "9.2.0-alpha02"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "sungbinland"

android {
  compileSdk = 36
  minSdk = 36
}

include(
  ":app",
  ":core",
  ":feature-nutrition",
  ":feature-workout",
  ":feature-study",
)
