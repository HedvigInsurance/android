plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.giraffe)
  implementation(libs.adyen)
}

android {
  namespace = "com.hedvig.lib.testdata"
}
