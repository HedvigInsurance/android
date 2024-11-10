import java.io.FileInputStream
import java.util.Properties

hedvig {
}
plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
  id("hedvig.android.lokalise")
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
