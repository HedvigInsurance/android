import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.composeKotlinCompilerGradlePlugin)
  alias(libs.plugins.composeJetbrainsCompilerGradlePlugin)
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.android.lokalise")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
  androidResources(resourcesNamespace = "hedvig.resources", publicRes = true)
}

@Suppress("SpellCheckingInspection")
lokalise {
  val lokaliseProperties = Properties()
  val lokalisePropertiesFile = rootProject.file("lokalise.properties")
  if (lokalisePropertiesFile.exists()) {
    lokaliseProperties.load(FileInputStream(lokalisePropertiesFile))
    lokaliseProjectId.set(lokaliseProperties.getProperty("id"))
    lokaliseToken.set(lokaliseProperties.getProperty("token"))
    outputDirectories.setFrom(
      file("src/androidMain/res"),
      file("src/commonMain/composeResources"),
    )
  }
}

// https://issuetracker.google.com/issues/463591200
//android {
//  namespace = "hedvig.resources"
//
//  defaultConfig {
//    vectorDrawables.useSupportLibrary = true
//  }
//}

kotlin {
  androidLibrary {
    namespace = "hedvig.resources"
    @Suppress("UnstableApiUsage")
    androidResources.enable = true
  }
  sourceSets {
    commonMain.dependencies {
      api(libs.jetbrains.components.resources)
      implementation(libs.jetbrains.compose.runtime)
    }
  }
}
