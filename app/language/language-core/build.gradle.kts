plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  api(projects.app.apollo.giraffe)
  api(projects.app.market.marketCore)

  implementation(projects.app.core.common)

  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.preference)
  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.language"
}
