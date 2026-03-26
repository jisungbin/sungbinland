plugins {
  alias(libs.plugins.android.library)
  kotlin("plugin.compose")
  kotlin("plugin.serialization")
}

android {
  namespace = "sungbinland.muscle"

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
  implementation(projects.uikit)

  implementation(libs.coil.compose)
  implementation(libs.androidx.navigation3.runtime)

  implementation(libs.compose.foundation)
  implementation(libs.compose.material.icons.extended)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.util)

  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlinx.serialization.core)
}
