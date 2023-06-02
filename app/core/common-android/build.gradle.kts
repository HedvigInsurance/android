plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("kotlin-parcelize")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.core.resources)

  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.core)
  implementation(libs.androidx.other.recyclerView)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.slimber)

  testImplementation(libs.assertK)
  testImplementation(libs.jsonTest)
  testImplementation(libs.junit)
}

android {
  namespace = "com.hedvig.android.core.common.android"
}
