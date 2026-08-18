plugins {
  id("hedvig.jvm.library")
}

dependencies {
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)
  implementation(libs.ksp.symbolProcessingApi)
}
