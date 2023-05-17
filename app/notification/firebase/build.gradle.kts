plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

android {
  namespace = "com.hedvig.android.notification.firebase"
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.di)
  implementation(projects.apollo.giraffe)
  implementation(projects.auth.authCore)
  implementation(projects.auth.authEventCore)
  implementation(projects.coreCommon)
  implementation(projects.notification.notificationCore)

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
