plugins {
  alias(libs.plugins.android.library)
  kotlin("plugin.compose")
  kotlin("plugin.serialization")
}

android {
  namespace = "sungbinland.study"

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
  implementation(projects.uikit)

  implementation(libs.molecule.runtime)

  implementation(libs.androidx.navigation3.runtime)

  implementation(libs.compose.foundation)
  implementation(libs.compose.material.icons.extended)
  implementation(libs.compose.runtime.retain)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.util)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.core)
}
