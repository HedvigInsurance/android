plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.memberRemindersPublic)
}
