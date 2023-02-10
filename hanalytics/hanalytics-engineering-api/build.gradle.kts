plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(libs.koin.android)
}

android {
  namespace = "com.hedvig.android.hanalytics.engineering.api"
}
