plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.androidx.navigation.common)
  implementation(libs.koin.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.navigationCommon)
}
