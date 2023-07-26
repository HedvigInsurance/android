plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.android.notification.firebase"
}

dependencies {
  implementation(projects.apolloCore)
  implementation(projects.apolloGiraffePublic)
  implementation(projects.authCore)
  implementation(projects.authEventCore)
  implementation(projects.notificationCore)

  implementation(platform(libs.firebase.bom))

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.other.workManager)
  implementation(libs.apollo.runtime)
  implementation(libs.firebase.messaging)
  implementation(libs.koin.android)
  implementation(libs.koin.workManager)
  implementation(libs.slimber)
}
