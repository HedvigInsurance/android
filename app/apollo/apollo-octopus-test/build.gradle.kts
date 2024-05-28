plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)

  implementation(projects.apolloOctopusPublic)
  implementation(projects.coreMarkdown)
}
