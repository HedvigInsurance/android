import java.io.FileInputStream
import java.util.Properties

plugins {
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
  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}

dependencies {
  implementation(libs.materialComponents)
}
