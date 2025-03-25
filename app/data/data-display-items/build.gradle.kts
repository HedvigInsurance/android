plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(libs.kotlinx.datetime)
}
