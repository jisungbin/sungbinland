plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.ksp) apply false
  kotlin("plugin.compose") version libs.versions.kotlin.compose.get() apply false
  kotlin("plugin.serialization") version libs.versions.kotlin.compose.get() apply false
}
