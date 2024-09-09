plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.apollo)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.coroutines.core)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.datetime)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.dataChat)
  implementation(projects.featureFlagsPublic)

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.testParameterInjector)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.databaseTest)
  testImplementation(projects.featureFlagsTest)
}

apollo {
  service("octopus") {
    packageName = "octopus"
    dependsOn(projects.apolloOctopusPublic, true)
  }
}

// // https://issuetracker.google.com/issues/341381075#comment6
// val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
// androidComponents.onVariants(androidComponents.selector().all()) { variant ->
//  (variant as HasUnitTest).unitTest?.let {
//    System.err.println(it.runtimeConfiguration)
//    with(it.runtimeConfiguration.resolutionStrategy.dependencySubstitution) {
//      substitute(module("androidx.sqlite:sqlite-bundled:${libs.versions.sqlite.get()}"))
//        .using(module("androidx.sqlite:sqlite-bundled-jvm:${libs.versions.sqlite.get()}"))
//    }
//  }
// }
