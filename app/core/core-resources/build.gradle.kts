import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.compose.resources.ResourcesExtension

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
  androidResources()
}

@Suppress("SpellCheckingInspection")
lokalise {
  val lokaliseProperties = Properties()
  val lokalisePropertiesFile = rootProject.file("lokalise.properties")
  if (lokalisePropertiesFile.exists()) {
    lokaliseProperties.load(FileInputStream(lokalisePropertiesFile))
    lokaliseProjectId.set(lokaliseProperties.getProperty("id"))
    lokaliseToken.set(lokaliseProperties.getProperty("token"))
    outputDirectory.set(file("src/commonMain/composeResources"))
  }
}

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
  }
  sourceSets {
    commonMain.dependencies {
      api(libs.jetbrains.components.resources)
    }
  }
}

compose.resources {
  // todo move inside gradle plugin
  generateResClass = ResourcesExtension.ResourceClassGeneration.Always
  packageOfResClass = "hedvig.resources"
  publicResClass = true
}
