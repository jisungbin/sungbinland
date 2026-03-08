plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
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
  implementation(projects.core)
  implementation(projects.featureNutrition)
  implementation(projects.featureStudy)
  implementation(projects.featureWorkout)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)

  implementation(libs.compose.material.icons.extended)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.util)

  implementation(libs.hilt.android)

  ksp(libs.hilt.compiler)
}
