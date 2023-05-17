plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
}

dependencies {
  implementation(projects.coreResources)

  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.other.core)
  implementation(libs.slimber)

  testImplementation(libs.assertK)
  testImplementation(libs.jsonTest)
  testImplementation(libs.junit)
}

android {
  namespace = "com.hedvig.android.core.common.android"
}
