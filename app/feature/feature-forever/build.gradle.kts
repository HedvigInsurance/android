plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.feature")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  alias(libs.plugins.serialization)
}

dependencies {
  api(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.koin.compose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreDesignSystem)
  implementation(projects.foreverUi)
  implementation(projects.languageCore)
  implementation(projects.moleculeAndroid)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
}
