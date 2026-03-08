plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.poko)
  kotlin("plugin.compose")
}

android {
  namespace = "sungbinland.uikit"

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
  implementation(libs.dev.chrisbanes.haze)

  implementation(libs.compose.foundation)
  implementation(libs.compose.runtime)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.util)
}
