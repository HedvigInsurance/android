@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.kotlin.dsl.libs
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
  alias(libs.plugins.composeJetbrainsCompilerGradlePlugin)
}

hedvig {
  serialization()
  compose()
}

kotlin {
  sourceSets {
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    commonMain.dependencies {
      implementation(compose.animation)
      implementation(compose.foundation)
      implementation(compose.ui)
      implementation(libs.apollo.api)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
    }
    androidMain.dependencies {
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(libs.androidx.navigation.compose)
      implementation(libs.koin.compose)
      implementation(libs.koin.core)
      implementation(libs.koin.coreViewmodel)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
    }
  }
}
