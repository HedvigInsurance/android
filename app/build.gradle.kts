@file:Suppress("UnstableApiUsage")

// TODO: Remove once https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.googleServices)
  alias(libs.plugins.crashlytics)
  id("kotlin-parcelize")
  alias(libs.plugins.license)
  alias(libs.plugins.serialization)
  alias(libs.plugins.datadog)
}

licenseReport {
  copyHtmlReportToAssets = true
}

android {
  buildFeatures {
    viewBinding = true
    aidl = false
    renderScript = false
  }

  defaultConfig {
    applicationId = "com.hedvig"

    versionCode = 43
    versionName = "7.2.0"

    vectorDrawables.useSupportLibrary = true

    resourceConfigurations.addAll(listOf("en", "en-rNO", "en-rSE", "en-rDK", "nb-rNO", "sv-rSE", "da-rDK"))

    testInstrumentationRunner = "com.hedvig.app.TestRunner"
  }

  lint {
    abortOnError = false
  }

  packagingOptions {
    resources {
      excludes += "javamoney.properties"
      excludes += "README.txt"
      excludes += "META-INF/LGPL2.1"
      excludes += "META-INF/AL2.0"
    }
  }

  buildTypes {
    @Suppress("UNUSED_VARIABLE")
    val debug by getting {
      applicationIdSuffix = ".dev.app"
      manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = false
      isDebuggable = true
    }

    @Suppress("UNUSED_VARIABLE")
    val release by getting {
//      signingConfig = debug.signingConfig // uncomment to run release build locally
      applicationIdSuffix = ".app"
      manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true

      isMinifyEnabled = true
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android.txt"),
          "proguard-rules.pro",
        ),
      )
    }

    @Suppress("UNUSED_VARIABLE")
    val staging by creating {
      applicationIdSuffix = ".test.app"
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
      kotlin.srcDir("src/engineering/kotlin")
      res.srcDir("src/engineering/res")
      manifest.srcFile("src/debug/AndroidManifest.xml")
    }
    named("staging") {
      kotlin.srcDir("src/engineering/kotlin")
      res.srcDir("src/engineering/res")
      manifest.srcFile("src/debug/AndroidManifest.xml")
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
  implementation(projects.apollo)
  implementation(projects.coreCommon)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.featureBusinessmodel)
  implementation(projects.hanalytics.hanalyticsAndroid)
  implementation(projects.hanalytics.hanalyticsCore)
  implementation(projects.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.hedvigLanguage)
  implementation(projects.hedvigMarket)
  implementation(projects.notificationBadgeData)

  testImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)
  androidTestImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)

  implementation(projects.hanalytics.hanalyticsEngineeringApi)
  releaseImplementation(projects.hanalytics.hanalyticsEngineeringNoop)
  debugImplementation(projects.hanalytics.hanalyticsEngineering)
  "stagingImplementation"(projects.hanalytics.hanalyticsEngineering)

  androidTestImplementation(projects.testdata)
  testImplementation(projects.testdata)
  debugImplementation(projects.testdata)
  "stagingImplementation"(projects.testdata)

  implementation(libs.coroutines.core)
  implementation(libs.coroutines.android)
  testImplementation(libs.coroutines.test)

  implementation(libs.serialization.json)

  testImplementation(libs.androidx.arch.testing)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.media)
  implementation(libs.androidx.other.constraintLayout)
  implementation(libs.androidx.other.dynamicAnimation)
  implementation(libs.androidx.other.preference)
  implementation(libs.androidx.other.core)
  implementation(libs.androidx.other.viewPager2)
  implementation(libs.androidx.other.swipeRefreshLayout)
  implementation(libs.androidx.other.recyclerView)
  implementation(libs.androidx.other.fragment)
  implementation(libs.androidx.other.browser)
  implementation(libs.androidx.other.transition)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.liveData)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.other.workManager)
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.other.startup)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.espresso.intents)
  androidTestImplementation(libs.androidx.espresso.contrib)
  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.jsonTest)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.junit)

  implementation(libs.accompanist.pager)
  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.accompanist.insetsUi)
  implementation(libs.accompanist.systemUiController)

  implementation(libs.apollo.adapters)
  implementation(libs.apollo.normalizedCache)
  androidTestImplementation(libs.apollo.idlingResource)
  testImplementation(libs.apollo.mockServer)
  testImplementation(libs.apollo.testingSupport)

  implementation(libs.arrowKt.core)
  implementation(libs.arrowKt.fx)

  implementation(libs.materialComponents)
  implementation(libs.flexbox)

  implementation(libs.playKtx)

  implementation(libs.fragmentViewBindingDelegate)

  implementation(libs.okhttp.core)
  implementation(libs.okhttp.loggingInterceptor)
  androidTestImplementation(libs.okhttp.mockWebServer)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.playServicesBase)
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.dynamicLinks)
  implementation(libs.firebase.config)
  implementation(libs.firebase.messaging)

  implementation(libs.koin.android)
  androidTestImplementation(libs.koin.test)

  implementation(libs.timber)
  implementation(libs.slimber)

  implementation(libs.reactiveX.kotlin)
  implementation(libs.reactiveX.android)

  implementation(libs.svg)

  implementation(libs.coil.coil)
  implementation(libs.coil.svg)
  implementation(libs.coil.gif)
  implementation(libs.coil.compose)

  implementation(libs.tooltip)

  implementation(libs.zXing)

  implementation(libs.insetter)

  implementation(libs.markwon.core)
  implementation(libs.markwon.linkify)

  implementation(libs.adyen)

  implementation(libs.moneta)

  implementation(libs.shimmer)

  implementation(libs.concatAdapterExtension)

  testImplementation(libs.assertK)
  androidTestImplementation(libs.assertK)
  androidTestImplementation(libs.kaspresso)

  androidTestImplementation(libs.mockk.android)
  testImplementation(libs.mockk.jvm)

  debugImplementation(libs.leakCanary)
  debugImplementation(libs.shake)
  "stagingImplementation"(libs.shake)

  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.mdcAdapter)
  debugImplementation(libs.androidx.compose.uiTooling)
  implementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.uiViewBinding)
  implementation(libs.androidx.lifecycle.compose)
  androidTestImplementation(libs.androidx.compose.uiTestJunit)
  debugImplementation(libs.androidx.compose.uiTestManifest)

  implementation(libs.datadog.sdk)
  implementation(libs.odyssey)
}
