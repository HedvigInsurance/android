enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
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
    maven("https://maven.pkg.github.com/HedvigInsurance/odyssey") {
      name = "odyssey"
      credentials(PasswordCredentials::class)
    }
    maven("https://maven.pkg.github.com/HedvigInsurance/authlib") {
      name = "authlib"
      credentials(PasswordCredentials::class)
    }
    maven("https://jitpack.io")
  }
}

rootProject.name = "hedvigandroid"

include(":apollo")
include(":apollo:core")
include(":apollo:di")
include(":apollo:giraffe")
include(":apollo:octopus")
include(":app")
include(":audio-player")
include(":auth:auth-android")
include(":auth:auth-core")
include(":auth:auth-event-core")
include(":auth:auth-event-test")
include(":auth:auth-test")
include(":core-common")
include(":core-common-android")
include(":core-common-android-test")
include(":core-common-test")
include(":core-datastore")
include(":core-datastore-test")
include(":core-design-system")
include(":core-resources")
include(":core-ui")
include(":datadog")
include(":feature-businessmodel")
include(":feature-changeaddress")
include(":feature-odyssey")
include(":feature-terminate-insurance")
include(":hanalytics:hanalytics-android")
include(":hanalytics:hanalytics-core")
include(":hanalytics:hanalytics-feature-flags")
include(":hanalytics:hanalytics-feature-flags-test")
include(":hanalytics:hanalytics-test")
include(":hedvig-language")
include(":hedvig-market")
include(":micro-apps:design-showcase")
include(":navigation:navigation-activity")
include(":navigation:navigation-compose-typed")
include(":notification-badge-data")
include(":notification:notification-core")
include(":notification:firebase")
include(":testdata")
