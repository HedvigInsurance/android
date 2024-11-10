hedvig {}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.other.coreKtx)
  implementation(libs.firebase.messaging)
  implementation(projects.coreResources)
}
