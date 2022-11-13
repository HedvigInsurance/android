enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
pluginManagement {
  includeBuild("build-logic")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
    maven("https://jitpack.io")
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.github.com/HedvigInsurance/odyssey") {
      name = "odyssey"
      credentials(PasswordCredentials::class)
    }
  }
}

rootProject.name = "hedvigandroid"

include(":apollo")
include(":app")
include(":core-common")
include(":core-common-android")
include(":core-datastore")
include(":core-design-system")
include(":core-resources")
include(":core-ui")
include(":feature-businessmodel")
include(":hanalytics:hanalytics-android")
include(":hanalytics:hanalytics-core")
include(":hanalytics:hanalytics-engineering")
include(":hanalytics:hanalytics-engineering-api")
include(":hanalytics:hanalytics-engineering-noop")
include(":hanalytics:hanalytics-feature-flags")
include(":hanalytics:hanalytics-feature-flags-test")
include(":hedvig-language")
include(":hedvig-market")
include(":notification-badge-data")
include(":testdata")
