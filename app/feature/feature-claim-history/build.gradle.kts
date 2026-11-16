plugins {
  id("hedvig.multiplatform.library.ios")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
  navKeys()
  sharedWithIos()
  viewModels()
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.jetbrains.compose.runtime)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.moleculePublic)
    }
    getByName("jvmAndAndroidMain").dependencies {
      implementation(libs.jetbrains.compose.ui.tooling.preview)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.coreResources)
      implementation(projects.designSystemHedvig)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
    commonTest.dependencies {
      implementation(libs.apollo.engine.ktor)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.ktor.client.mock)
    }
    nativeTest.dependencies {
      implementation(libs.apollo.normalizedCache)
      implementation(projects.coreBuildConstants)
      implementation(projects.networkClients)
    }
  }
}
