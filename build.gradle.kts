plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  // AGP 내장 Kotlin(2.2.10) 대신 최신 KGP를 클래스패스에 고정하는 버전 앵커.
  alias(libs.plugins.kotlin.android) apply false
}
