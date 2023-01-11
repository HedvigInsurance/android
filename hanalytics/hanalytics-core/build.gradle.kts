@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.kotlin.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommon)

  api(libs.hAnalytics)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.serialization.core)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=com.hedvig.android.hanalytics.InternalHanalyticsApi"
  }
}
