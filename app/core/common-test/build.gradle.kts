plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.coroutines.test)
  implementation(libs.junit)
}
