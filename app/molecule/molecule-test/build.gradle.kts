plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

dependencies {
  api(projects.moleculePublic)
  implementation(libs.androidx.annotation)
  implementation(libs.coroutines.test)
  implementation(libs.jetbrains.compose.runtime)
  implementation(libs.molecule)
  implementation(libs.turbine)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
}
