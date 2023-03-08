plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.giraffe)
  implementation(projects.coreCommon)
  implementation(projects.coreResources)

  implementation(libs.androidx.other.preference)
  implementation(libs.koin.android)
}

android {
  namespace = "com.hedvig.android.market"
}
