plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.notificationBadgeDataPublic)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.notification.badge.data.test"
}
