plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "sungbinland.app"
  compileSdk = 37

  signingConfigs {
    create("release") {
      storeFile = rootProject.file("keystore.jks")
      storePassword = "aaaaaa"
      keyAlias = "key0"
      keyPassword = "aaaaaa"
    }
  }

  defaultConfig {
    minSdk = 36
    targetSdk = 37
    versionCode = 8
    versionName = "8"
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
    }
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
