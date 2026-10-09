plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.other.coreKtx)
  implementation(libs.firebase.messaging)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.initializable)
  implementation(projects.languageCore)
  implementation(projects.permissionCore)
}
