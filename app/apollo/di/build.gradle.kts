plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.apollo.octopus)

  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.apollo.di"
}
