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

include(":app:apollo")
include(":app:apollo:core")
include(":app:apollo:di")
include(":app:apollo:giraffe")
include(":app:apollo:giraffe-test")
include(":app:apollo:octopus")
include(":app:app")
include(":app:audio-player")
include(":app:auth:auth-android")
include(":app:auth:auth-core")
include(":app:auth:auth-event-core")
include(":app:auth:auth-event-test")
include(":app:auth:auth-test")
include(":app:core:common")
include(":app:core:common-android")
include(":app:core:common-android-test")
include(":app:core:common-test")
include(":app:core:datastore")
include(":app:core:datastore-test")
include(":app:core:design-system")
include(":app:core:resources")
include(":app:core:ui")
include(":app:core:ui-data")
include(":app:data:claim-flow")
include(":app:data:claim-triaging")
include(":app:data:travel-certificate")
include(":app:datadog")
include(":app:feature:businessmodel")
include(":app:feature:changeaddress")
include(":app:feature:claim-triaging")
include(":app:feature:home")
include(":app:feature:legacy-claim-triaging")
include(":app:feature:odyssey")
include(":app:feature:terminate-insurance")
include(":app:feature:travel-certificate")
include(":app:hanalytics:hanalytics-android")
include(":app:hanalytics:hanalytics-core")
include(":app:hanalytics:hanalytics-feature-flags")
include(":app:hanalytics:hanalytics-feature-flags-test")
include(":app:hanalytics:hanalytics-test")
include(":app:language:language-core")
include(":app:language:language-test")
include(":app:market:market-core")
include(":app:market:market-test")
include(":app:navigation:core")
include(":app:navigation:navigation-activity")
include(":app:navigation:navigation-compose-typed")
include(":app:notification-badge-data")
include(":app:notification:firebase")
include(":app:notification:notification-core")
include(":app:testdata")
include(":micro-apps:design-showcase")
