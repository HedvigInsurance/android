@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  alias(libs.plugins.composeJetbrainsCompilerGradlePlugin)
}

hedvig {
  apollo("octopus")
  compose()
  serialization()
}

kotlin {
  sourceSets {
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    commonMain.dependencies {
      implementation(compose.animation)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.material)
      implementation(compose.ui)
      implementation(libs.androidx.lifecycle.compose)
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
    }
    androidMain.dependencies {
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.koin.compose)
      implementation(libs.koin.core)
      implementation(libs.koin.coreViewmodel)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.designSystemHedvig)
    }
  }
}
