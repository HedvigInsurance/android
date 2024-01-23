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
    versionName = "12.4.0"

    vectorDrawables.useSupportLibrary = true

    resourceConfigurations.addAll(listOf("en", "en-rNO", "en-rSE", "en-rDK", "nb-rNO", "sv-rSE", "da-rDK"))
  }

  packaging {
    resources {
      excludes += "javamoney.properties"
      excludes += "README.txt"
      excludes += "META-INF/LGPL2.1"
      excludes += "META-INF/AL2.0"
      excludes += "META-INF/versions/9/previous-compilation-data.bin" // https://github.com/Kotlin/kotlinx-datetime/issues/304
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
      "org.hamcrest:hamcrest-core:2.2",
      "org.hamcrest:hamcrest-library:2.2",
      "org.hamcrest:hamcrest:2.2",
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
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.runtime.livedata)
  implementation(libs.androidx.compose.uiCore)
  implementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.uiUnit)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.liveData)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.navigation.common)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.browser)
  implementation(libs.androidx.other.constraintLayout)
  implementation(libs.androidx.other.core)
  implementation(libs.androidx.other.splashscreen)
  implementation(libs.androidx.other.startup)
  implementation(libs.androidx.other.workManager)
  implementation(libs.androidx.profileInstaller)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.coil.coil)
  implementation(libs.coil.gif)
  implementation(libs.coil.svg)
  implementation(libs.concatAdapterExtension)
  implementation(libs.coroutines.android)
  implementation(libs.coroutines.core)
  implementation(libs.datadog.sdk.core)
  implementation(libs.datadog.sdk.compose)
  implementation(libs.datadog.sdk.rum)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.dynamicLinks)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.playServicesBase)
  implementation(libs.kiwi.navigationCompose)
  implementation(libs.koin.android)
  implementation(libs.koin.compose)
  implementation(libs.koin.workManager)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.moneta)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.loggingInterceptor)
  implementation(libs.playReview)
  implementation(libs.timber)
  implementation(projects.apolloAuthListeners)
  implementation(projects.apolloCore)
  implementation(projects.authCorePublic)
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
  implementation(projects.dataChatReadTimestampPublic)
  implementation(projects.dataClaimFlow)
  implementation(projects.dataSettingsDatastorePublic)
  implementation(projects.dataTravelCertificatePublic)
  implementation(projects.datadogCore)
  implementation(projects.datadogDemoTracking)
  implementation(projects.featureChangeaddress)
  implementation(projects.featureChat)
  implementation(projects.featureClaimDetails)
  implementation(projects.featureClaimTriaging)
  implementation(projects.featureConnectPaymentAdyen)
  implementation(projects.featureConnectPaymentTrustly)
  implementation(projects.featureEditCoinsured)
  implementation(projects.featureForever)
  implementation(projects.featureHelpCenter)
  implementation(projects.featureFlagsPublic)
  implementation(projects.featureHome)
  implementation(projects.featureInsurances)
  implementation(projects.featureLogin)
  implementation(projects.featureOdyssey)
  implementation(projects.featurePayments)
  implementation(projects.featureProfile)
  implementation(projects.featureTerminateInsurance)
  implementation(projects.featureTravelCertificate)
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
  implementation(projects.navigationCore)
  implementation(projects.navigationUi)
  implementation(projects.notificationBadgeDataPublic)
  implementation(projects.notificationCore)
  implementation(projects.notificationFirebase)
  implementation(projects.theme)

  debugImplementation(libs.androidx.compose.uiTestManifest)
  debugImplementation(libs.androidx.compose.uiTooling)
  debugImplementation(libs.leakCanary)

  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.koin.test)
  testImplementation(libs.mockk.jvm)
  testImplementation(libs.turbine)
  testImplementation(projects.authCoreTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.marketTest)
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
