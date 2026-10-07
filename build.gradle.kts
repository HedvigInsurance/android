plugins {
  // Every other plugin's classpath is supplied by the `hedvig.settings` convention plugin (see
  // build-logic/convention/build.gradle.kts), so project scripts apply them by id without a version.
  // Only plugins applied to the root project itself are declared here.
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doctor)
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}

tasks.register("accessibilityChecks") {
  group = "verification"
  description = "Runs the design system accessibility checks against a connected device or emulator. " +
    "Start one first: the task fails with \"No online devices found\" otherwise."
  dependsOn(":accessibility-test:connectedAndroidTest")
}

dependencyAnalysis {
  issues {
    all {
      onUsedTransitiveDependencies {
        severity("ignore")
      }
    }
  }
}
