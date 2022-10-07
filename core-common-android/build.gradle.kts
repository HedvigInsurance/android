plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreResources)

  implementation(libs.androidx.other.core)

  testImplementation(libs.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.jsonTest)
}
