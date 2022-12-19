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
    maven("https://maven.pkg.github.com/HedvigInsurance/authlib") {
      name = "authlib"
      credentials(PasswordCredentials::class)
    }
  }
}

rootProject.name = "hedvigandroid"

include(":apollo")
include(":app")
include(":auth:auth-core")
include(":auth:auth-test")
include(":core-common")
include(":core-common-android")
include(":core-common-android-test")
include(":core-common-test")
include(":core-datastore")
include(":core-datastore-test")
include(":core-design-system")
include(":core-navigation")
include(":core-resources")
include(":core-ui")
include(":feature-businessmodel")
include(":feature-odyssey")
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
