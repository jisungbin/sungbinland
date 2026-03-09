plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "sungbinland.core.fixture"

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

  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.core)
}
