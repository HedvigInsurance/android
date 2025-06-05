plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.unleash)

  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(projects.authCorePublic)
  implementation(projects.authEventCore)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
}
