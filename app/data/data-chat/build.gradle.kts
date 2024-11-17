plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  serialization()
}

dependencies {
  api(libs.paging.common)
  implementation(libs.kotlinx.datetime)
  implementation(libs.room.common)
  implementation(libs.uuid)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreUiData)
  implementation(projects.dataContractPublic)
  implementation(projects.dataProductVariantPublic)
}

