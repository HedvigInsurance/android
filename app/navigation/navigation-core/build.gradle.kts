plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

hedvig {
  serialization()
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.annotation)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.coreBuildConstants)
  implementation(projects.dataContractPublic)
  implementation(projects.navigationCompose)
}
