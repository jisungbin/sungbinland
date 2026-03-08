plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "sungbinland.core.alarm"

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
