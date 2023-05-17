plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

android {
  namespace = "com.hedvig.android.navigation.activity"
}

dependencies {
  implementation(libs.koin.core)
}
