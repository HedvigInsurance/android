plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

android {
  namespace = "com.hedvig.android.data.travelcertificate"
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.octopus)
  implementation(projects.app.core.common)

  implementation(libs.apollo.runtime)
  implementation(libs.arrow.core)
  implementation(libs.koin.compose)
  implementation(libs.slimber)
}
