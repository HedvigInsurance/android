plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.core.common)
  implementation(projects.app.core.resources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.market"
}
