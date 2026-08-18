plugins {
  id("hedvig.jvm.library")
}

dependencies {
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)
  implementation(libs.ksp.symbolProcessingApi)

  testImplementation(libs.assertK)
  testImplementation(libs.junit)
  testImplementation(libs.kctfork.ksp)
  testImplementation(libs.ksp.symbolProcessingApi)
}
