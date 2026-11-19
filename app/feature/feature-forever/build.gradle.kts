plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.metro.viewmodel)
      implementation(libs.metro.viewmodel.compose)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.designSystemHedvig)
      implementation(projects.featureForeverNavigation)
      implementation(projects.foreverUi)
      implementation(projects.languageCore)
      implementation(projects.moleculePublic)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
    }
  }
}
