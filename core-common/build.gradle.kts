plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.coroutines.core)
  implementation(libs.koin.android)
  implementation(libs.okhttp.core)

  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.assertK)
  testImplementation(libs.jsonTest)
}
