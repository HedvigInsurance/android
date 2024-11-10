plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.android.library")
}

dependencies {
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)

  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreMarkdown)
}
