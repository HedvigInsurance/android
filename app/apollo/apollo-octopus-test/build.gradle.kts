plugins {
  id("hedvig.kotlin.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)

  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreMarkdown)
}
