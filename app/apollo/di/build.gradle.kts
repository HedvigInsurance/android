plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.apollo.giraffe)
  implementation(projects.apollo.octopus)

  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.apollo.di"
}
