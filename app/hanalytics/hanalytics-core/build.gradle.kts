@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.serialization)
}

dependencies {
  implementation(projects.coreCommonPublic)

  api(libs.hAnalytics)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=com.hedvig.android.hanalytics.InternalHanalyticsApi"
  }
}
