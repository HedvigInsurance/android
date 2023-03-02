plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  @Suppress("DSL_SCOPE_VIOLATION")
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.feature.cancelinsurance"
}

dependencies {
  implementation(projects.apollo)
  implementation(projects.auth.authAndroid)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.navigation.navigationComposeTypedExt)

  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.viewmodelCompose)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.slimber)
  implementation(libs.timber)
}

// Excludes material2 dependency from leaking through transitive dependencies
// This enables us to avoid the confusion of both m2 and m3 functions popping up
// Can be used in modules which are brand new and don't need to support legacy features-views
// Can consider moving this in a convention plugin, standalone of even a `feature` plugin
configurations.implementation {
  exclude(libs.androidx.compose.material.get().group)
}
