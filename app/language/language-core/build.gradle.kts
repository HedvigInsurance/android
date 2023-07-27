plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
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
