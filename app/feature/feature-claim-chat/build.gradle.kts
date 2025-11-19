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
      implementation(libs.apollo.normalizedCache)
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.koin.composeViewModel)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.contentNegotiation)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.logging)
      implementation(libs.uri.kmp)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.moleculePublic)
    }
    androidMain.dependencies {
      implementation(libs.bundles.kmpPreviewBugWorkaround)
      implementation(libs.androidx.navigation.compose)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.designSystemHedvig)
    }
  }
}
