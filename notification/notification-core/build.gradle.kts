plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

android {
  namespace = "com.hedvig.android.notification.core"
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.koin.core)
  implementation(libs.firebase.messaging)
}
