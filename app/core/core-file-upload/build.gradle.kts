plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
  compose()
}

dependencies {
  api(libs.androidx.compose.foundation)
  implementation(libs.arrow.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.client.core)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.designSystemHedvig)
  implementation(projects.networkClients)
}
