import com.project.starter.easylauncher.filter.ColorRibbonFilter

plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
  alias(libs.plugins.appIconBannerGenerator) // Automatically adds the "DEBUG" banner on the debug app icon
  alias(libs.plugins.crashlytics)
  alias(libs.plugins.datadog)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.license)
  alias(libs.plugins.serialization)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

android {
  namespace = "com.hedvig.app"

  buildFeatures {
    buildConfig = true
  }

  defaultConfig {
    applicationId = "com.hedvig"

    versionCode = 43
    versionName = "12.8.6"

    vectorDrawables.useSupportLibrary = true

    resourceConfigurations.addAll(listOf("en", "en-rNO", "en-rSE", "en-rDK", "nb-rNO", "sv-rSE", "da-rDK"))
  }

  packaging {
    resources {
      excludes += "javamoney.properties"
      excludes += "README.txt"
      excludes += "META-INF/LGPL2.1"
      excludes += "META-INF/AL2.0"
      // https://github.com/Kotlin/kotlinx-datetime/issues/304
      excludes += "META-INF/versions/9/previous-compilation-data.bin"
    }
  }

  buildTypes {
    val debug by getting {
      applicationIdSuffix = ".dev.app"
      manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = false
      isDebuggable = true
    }

    val release by getting {
//      signingConfig = debug.signingConfig // uncomment to run release build locally
      applicationIdSuffix = ".app"
      manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true

      isMinifyEnabled = true
      isShrinkResources = true
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android.txt"),
          "proguard-rules.pro",
        ),
      )
    }

    val staging by creating {
      applicationIdSuffix = ".test.app"
      matchingFallbacks.add("release")
      manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true
      isMinifyEnabled = true
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android.txt"),
          "proguard-rules.pro",
        ),
      )
    }
  }

  sourceSets {
    named("debug") {
      manifest.srcFile("src/debug/AndroidManifest.xml")
    }
    named("staging") {
      manifest.srcFile("src/debug/AndroidManifest.xml")
    }
  }

  signingConfigs {
    named("debug") {
      storeFile = file("../../debug.keystore")
    }
  }

  configurations.all {
    resolutionStrategy.force(
      "org.hamcrest:hamcrest-core:3.0",
      "org.hamcrest:hamcrest-library:3.0",
      "org.hamcrest:hamcrest:3.0",
    )
  }
}

dependencies {
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.coreKtx)
  implementation(libs.androidx.other.splashscreen)
  implementation(libs.androidx.other.startup)
  runtimeOnly(libs.androidx.profileInstaller)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.coil)
  implementation(libs.coil.gif)
  implementation(libs.coil.svg)
  runtimeOnly(libs.coroutines.android)
  implementation(libs.coroutines.core)
  implementation(libs.datadog.sdk.compose)
  implementation(libs.datadog.sdk.core)
  implementation(libs.datadog.sdk.rum)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.dynamicLinks)
  implementation(libs.firebase.messaging)
  implementation(libs.koin.android)
  implementation(libs.koin.workManager)
  runtimeOnly(libs.kotlin.reflect)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.navigationRecentsUrlSharing)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.loggingInterceptor)
  implementation(libs.playServicesBase)
  implementation(libs.timber)
  implementation(libs.playReview)
  implementation(projects.apolloAuthListeners)
  implementation(projects.apolloCore)
  implementation(projects.apolloNetworkCacheManager)
  implementation(projects.authCorePublic)
  implementation(projects.composeUi)
  implementation(projects.coreAppReview)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonAndroidPublic)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreDatastorePublic)
  implementation(projects.coreDemoMode)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreFileUpload)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.crossSells)
  implementation(projects.dataChangetier)
  implementation(projects.dataChat)
  implementation(projects.dataClaimFlow)
  implementation(projects.dataContractPublic)
  implementation(projects.dataConversations)
  implementation(projects.dataPayingMember)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.dataTermination)
  implementation(projects.datadogCore)
  implementation(projects.datadogDemoTracking)
  implementation(projects.designSystemHedvig)
  implementation(projects.designSystemInternals)
  implementation(projects.featureChangeaddress)
  implementation(projects.featureChat)
  implementation(projects.featureChooseTier)
  implementation(projects.featureClaimDetails)
  implementation(projects.featureClaimTriaging)
  implementation(projects.featureConnectPaymentTrustly)
  implementation(projects.featureDeleteAccount)
  implementation(projects.featureEditCoinsured)
  implementation(projects.featureFlagsPublic)
  implementation(projects.featureForceUpgrade)
  implementation(projects.featureForever)
  implementation(projects.featureHelpCenter)
  implementation(projects.featureHome)
  implementation(projects.featureInsurances)
  implementation(projects.featureLogin)
  implementation(projects.featureMovingflow)
  implementation(projects.featureOdyssey)
  implementation(projects.featurePayments)
  implementation(projects.featureProfile)
  implementation(projects.featureTerminateInsurance)
  implementation(projects.featureTravelCertificate)
  implementation(projects.foreverUi)
  implementation(projects.initializable)
  implementation(projects.languageCore)
  implementation(projects.languageData)
  implementation(projects.languageMigration)
  implementation(projects.loggingAndroid)
  implementation(projects.loggingPublic)
  implementation(projects.marketCore)
  implementation(projects.marketSet)
  implementation(projects.memberRemindersPublic)
  implementation(projects.navigationActivity)
  implementation(projects.navigationCompose)
  implementation(projects.navigationCore)
  implementation(projects.navigationUi)
  implementation(projects.notificationBadgeDataPublic)
  implementation(projects.notificationCore)
  implementation(projects.notificationFirebase)
  implementation(projects.theme)
  implementation(projects.trackingCore)
  implementation(projects.trackingDatadog)

  debugRuntimeOnly(libs.androidx.compose.uiTestManifest)
  debugImplementation(libs.androidx.compose.uiTooling)
}

easylauncher {
  buildTypes.register("staging") {
    setFilters(
      customRibbon(
        label = "staging",
        ribbonColor = "#99606060", // Gray
        gravity = ColorRibbonFilter.Gravity.BOTTOM,
        textSizeRatio = 0.25f,
      ),
    )
  }
  buildTypes.create("debug") {
    setFilters(
      customRibbon(
        label = "debug",
        ribbonColor = "#99FFC423", // Yellow
        textSizeRatio = 0.2f,
      ),
    )
  }
}

licenseReport {
  copyHtmlReportToAssets = true
}
