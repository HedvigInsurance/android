plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.apolloGiraffePublic)
  implementation(projects.apolloOctopusPublic)

  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.apollo.di"
}
