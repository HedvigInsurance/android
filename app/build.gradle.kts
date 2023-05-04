@file:Suppress("UnstableApiUsage")

import com.project.starter.easylauncher.filter.ColorRibbonFilter

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
plugins {
  id("hedvig.android.application")
  id("hedvig.android.application.compose")
  id("hedvig.android.ktlint")
  id("kotlin-parcelize")
  alias(libs.plugins.androidRemoveUnusedResourcesPlugin)
  alias(libs.plugins.appIconBannerGenerator) // Automatically adds the "DEBUG" banner on the debug app icon
  alias(libs.plugins.crashlytics)
  alias(libs.plugins.datadog)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.license)
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.app"

  buildFeatures {
    buildConfig = true
    viewBinding = true
  }

  defaultConfig {
    applicationId = "com.hedvig"

    versionCode = 43
    versionName = "11.0.4"

    vectorDrawables.useSupportLibrary = true

    resourceConfigurations.addAll(listOf("en", "en-rNO", "en-rSE", "en-rDK", "nb-rNO", "sv-rSE", "da-rDK"))

    testInstrumentationRunner = "com.hedvig.app.TestRunner"
  }

  lint {
    abortOnError = false
    checkDependencies = true
    checkGeneratedSources = true
  }

  packaging {
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
      isShrinkResources = true
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

  configurations.all {
    resolutionStrategy.force(
      "org.hamcrest:hamcrest-core:2.2",
      "org.hamcrest:hamcrest-library:2.2",
      "org.hamcrest:hamcrest:2.2",
    )
  }
}

dependencies {
  implementation(projects.apollo.core)
  implementation(projects.apollo.di)
  implementation(projects.apollo.giraffe)
  implementation(projects.apollo.octopus)
  implementation(projects.audioPlayer)
  implementation(projects.auth.authAndroid)
  implementation(projects.auth.authCore)
  implementation(projects.auth.authEventCore)
  implementation(projects.coreCommon)
  implementation(projects.coreCommonAndroid)
  implementation(projects.coreDatastore)
  implementation(projects.coreDesignSystem)
  implementation(projects.coreResources)
  implementation(projects.coreUi)
  implementation(projects.datadog)
  implementation(projects.featureBusinessmodel)
  implementation(projects.featureChangeaddress)
  implementation(projects.featureOdyssey)
  implementation(projects.featureTerminateInsurance)
  implementation(projects.hanalytics.hanalyticsAndroid)
  implementation(projects.hanalytics.hanalyticsCore)
  implementation(projects.hanalytics.hanalyticsFeatureFlags)
  implementation(projects.hedvigLanguage)
  implementation(projects.hedvigMarket)
  implementation(projects.navigation.navigationActivity)
  implementation(projects.notification.firebase)
  implementation(projects.notification.notificationCore)
  implementation(projects.notificationBadgeData)

  testImplementation(projects.auth.authEventTest)
  testImplementation(projects.auth.authTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.coreDatastoreTest)
  testImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)
  testImplementation(projects.hanalytics.hanalyticsTest)

  androidTestImplementation(projects.hanalytics.hanalyticsFeatureFlagsTest)

  androidTestImplementation(projects.testdata)
  testImplementation(projects.testdata)
  debugImplementation(projects.testdata)
  "stagingImplementation"(projects.testdata)

  implementation(platform(libs.firebase.bom))

  implementation(libs.accompanist.insetsUi)
  implementation(libs.accompanist.pagerIndicators)
  implementation(libs.accompanist.systemUiController)
  implementation(libs.adyen)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.uiViewBinding)
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.compose)
  implementation(libs.androidx.lifecycle.liveData)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.lifecycle.viewmodelCompose)
  implementation(libs.androidx.other.activityCompose)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.androidx.other.browser)
  implementation(libs.androidx.other.constraintLayout)
  implementation(libs.androidx.other.core)
  implementation(libs.androidx.other.dynamicAnimation)
  implementation(libs.androidx.other.fragment)
  implementation(libs.androidx.other.media)
  implementation(libs.androidx.other.preference)
  implementation(libs.androidx.other.recyclerView)
  implementation(libs.androidx.other.splashscreen)
  implementation(libs.androidx.other.startup)
  implementation(libs.androidx.other.swipeRefreshLayout)
  implementation(libs.androidx.other.transition)
  implementation(libs.androidx.other.viewPager2)
  implementation(libs.androidx.other.workManager)
  implementation(libs.androidx.profileInstaller)
  implementation(libs.apollo.adapters)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.coil.gif)
  implementation(libs.coil.svg)
  implementation(libs.concatAdapterExtension)
  implementation(libs.coroutines.android)
  implementation(libs.coroutines.core)
  implementation(libs.datadog.sdk)
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.crashlytics)
  implementation(libs.firebase.dynamicLinks)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.playServicesBase)
  implementation(libs.flexbox)
  implementation(libs.fragmentViewBindingDelegate)
  implementation(libs.insetter)
  implementation(libs.koin.android)
  implementation(libs.koin.workManager)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.markwon.core)
  implementation(libs.markwon.linkify)
  implementation(libs.materialComponents)
  implementation(libs.moneta)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.loggingInterceptor)
  implementation(libs.playKtx)
  implementation(libs.reactiveX.android)
  implementation(libs.reactiveX.kotlin)
  implementation(libs.shimmer)
  implementation(libs.slimber)
  implementation(libs.svg)
  implementation(libs.timber)
  implementation(libs.tooltip)
  implementation(libs.zXing)

  debugImplementation(libs.androidx.compose.uiTestManifest)
  debugImplementation(libs.androidx.compose.uiTooling)
  debugImplementation(libs.leakCanary)

  testImplementation(libs.androidx.arch.testing)
  testImplementation(libs.androidx.test.junit)
  testImplementation(libs.apollo.mockServer)
  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.jsonTest)
  testImplementation(libs.koin.test)
  testImplementation(libs.mockk.jvm)
  testImplementation(libs.turbine)

  androidTestImplementation(libs.androidx.compose.uiTestJunit)
  androidTestImplementation(libs.androidx.espresso.contrib)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.espresso.intents)
  androidTestImplementation(libs.androidx.test.junit)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.apollo.idlingResource)
  androidTestImplementation(libs.assertK)
  androidTestImplementation(libs.kaspresso)
  androidTestImplementation(libs.koin.test)
  androidTestImplementation(libs.mockk.android)
  androidTestImplementation(libs.okhttp.mockWebServer)
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
