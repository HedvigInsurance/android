plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.notification.core"
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.other.core)
  implementation(libs.firebase.messaging)
  implementation(libs.slimber)
}
