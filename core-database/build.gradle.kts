plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.poko)
}

android {
  namespace = "sungbinland.core"

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

ksp {
  arg("room.generateKotlin", "true")
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.room.runtime)

  implementation(libs.compose.runtime.annotation)
  implementation(libs.compose.ui.util)

  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlinx.coroutines.core)

  ksp(libs.androidx.room.compiler)
}
