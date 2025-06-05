plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.notificationBadgeDataPublic)
}
