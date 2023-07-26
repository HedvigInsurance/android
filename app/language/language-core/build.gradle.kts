plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.apolloGiraffePublic)
  api(projects.marketCore)

  implementation(projects.coreCommonPublic)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.language"
}
