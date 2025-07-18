import java.io.FileInputStream
import java.util.Properties

plugins {
  id("hedvig.android.library")
  id("hedvig.android.lokalise")
  id("hedvig.gradle.plugin")
}

hedvig {
  androidResources()
}

lokalise {
  val lokaliseProperties = Properties()
  lokaliseProperties.load(FileInputStream(rootProject.file("lokalise.properties")))
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
