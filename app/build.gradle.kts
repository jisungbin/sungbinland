plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "sungbinland.app"

  signingConfigs {
    create("release") {
      storeFile = rootProject.file("keystore.jks")
      storePassword = "aaaaaa"
      keyAlias = "key0"
      keyPassword = "aaaaaa"
    }
  }

  defaultConfig {
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

dependencies {
  implementation(libs.rxjava)
  implementation(libs.rxandroid)
}
