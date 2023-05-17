plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
}

android {
  namespace = "com.hedvig.android.notification.firebase"
}

dependencies {
  implementation(projects.app.apollo.core)
  implementation(projects.app.apollo.di)
  implementation(projects.app.apollo.giraffe)
  implementation(projects.app.auth.authCore)
  implementation(projects.app.auth.authEventCore)
  implementation(projects.app.core.coreCommon)
  implementation(projects.app.notification.notificationCore)

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
