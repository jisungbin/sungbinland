enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "sungbinland"

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

android {
  compileSdk = 36
  minSdk = 36
}

include(
  ":app",
  ":core-alarm",
  ":core-database",
  ":core-database-fixture",
  ":feature-nutrition",
  ":feature-workout",
  ":feature-study",
  ":uikit",
)
