plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.core.coreCommon)
  implementation(projects.app.core.coreResources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.android)
}

android {
  namespace = "com.hedvig.android.market"
}
