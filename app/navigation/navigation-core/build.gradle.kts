plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  serialization()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.navigationCommon)
    }
  }
}
