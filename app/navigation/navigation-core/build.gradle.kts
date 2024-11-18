plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.androidx.navigation.common)
  implementation(libs.koin.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.navigationCommon)
}
