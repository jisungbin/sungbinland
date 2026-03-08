plugins {
  alias(libs.plugins.android.library)
  kotlin("plugin.compose")
  kotlin("plugin.serialization")
}

android {
  namespace = "sungbinland.nutrition"

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

  implementation(libs.androidx.navigation3.runtime)

  implementation(libs.compose.material3)

  implementation(libs.kotlinx.serialization.core)
}
