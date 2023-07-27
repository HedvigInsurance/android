plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.app.notificationBadgeData.public)

  implementation(libs.turbine)
}

android {
  namespace = "com.hedvig.android.notification.badge.data.test"
}
