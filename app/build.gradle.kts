plugins {
  alias(libs.plugins.android.application)
  kotlin("plugin.compose")
}

android {
  namespace = "sungbinland.app"

  defaultConfig {
    targetSdk = 36
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
  }
}

kotlin {
  explicitApi()
  compilerOptions {
    allWarningsAsErrors = true
  }
}

dependencies {
  implementation(projects.coreDatabase)
  implementation(projects.coreDatabaseFixture)
  implementation(projects.uikit)
  implementation(projects.featureNutrition)
  implementation(projects.featureStudy)
  implementation(projects.featureWorkout)

  implementation(libs.dev.chrisbanes.haze)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.room.runtime)

  implementation(libs.kotlinx.collections.immutable)

  implementation(libs.compose.animation)
  implementation(libs.compose.foundation)
  implementation(libs.compose.material.icons.extended)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.util)
}
