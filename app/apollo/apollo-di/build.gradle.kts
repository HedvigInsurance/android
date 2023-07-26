plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.koin.core)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloOctopusPublic)
}

android {
  namespace = "com.hedvig.android.apollo.di"
}
