import java.io.FileInputStream
import java.util.Properties

plugins {
  id("hedvig.android.library")
  id("hedvig.android.lokalise")
  alias(libs.plugins.squareSortDependencies)
}

val lokaliseProperties = Properties()
lokaliseProperties.load(FileInputStream(rootProject.file("lokalise.properties")))

lokalise {
  lokaliseProjectId.set(lokaliseProperties.getProperty("id"))
  lokaliseToken.set(lokaliseProperties.getProperty("token"))
  outputDirectory.set(file("src/main/res"))
}

android {
  namespace = "hedvig.resources"

  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}
