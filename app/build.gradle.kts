import java.io.FileInputStream
import java.util.Properties

// TODO: Remove once https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application")
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.lokalise)
    alias(libs.plugins.license)
    alias(libs.plugins.serialization)
}

licenseReport {
    copyHtmlReportToAssets = true
}

android {
    commonConfig(
        AndroidVersions(
            libs.versions.compileSdkVersion.get().toInt(),
            libs.versions.minSdkVersion.get().toInt(),
            libs.versions.targetSdkVersion.get().toInt(),
        )
    )

    buildFeatures {
        viewBinding = true
        aidl = false
        renderScript = false
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
    }

    defaultConfig {
        applicationId = "com.hedvig"

        versionCode = 43
        versionName = "6.7.5"

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
        maybeCreate("staging")
        maybeCreate("pullrequest")
        named("release") {
            applicationIdSuffix = ".app"

            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true

            isMinifyEnabled = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }

        named("staging") {
            applicationIdSuffix = ".test.app"

            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true

            isMinifyEnabled = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }

        named("pullrequest") {
            applicationIdSuffix = ".test.app"

            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true

            isMinifyEnabled = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro",
                    "proguard-rules-showkase.pro"
                )
            )
        }

        named("debug") {
            applicationIdSuffix = ".dev.app"

            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = false

            isDebuggable = true
        }
    }

    sourceSets {
        named("debug") {
            java.srcDir("src/engineering/java")
            res.srcDir("src/engineering/res")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
        named("staging") {
            java.srcDir("src/engineering/java")
            res.srcDir("src/engineering/res")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
        named("pullrequest") {
            java.srcDir("src/engineering/java")
            res.srcDir("src/engineering/res")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
    }

    configurations.all {
        resolutionStrategy.force(
            "org.hamcrest:hamcrest-core:2.1",
            "org.hamcrest:hamcrest-library:2.1",
            "org.hamcrest:hamcrest:2.1"
        )
    }
}

dependencies {
    implementation(project(":apollo"))
    implementation(project(":core-common"))

    androidTestImplementation(project(":testdata"))
    testImplementation(project(":testdata"))
    debugImplementation(project(":testdata"))
    "stagingImplementation"(project(":testdata"))
    "pullrequestImplementation"(project(":testdata"))

    coreLibraryDesugaring(libs.coreLibraryDesugaring)
    implementation(libs.kotlin.stdlib)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    testImplementation(libs.coroutines.test)

    implementation(libs.serialization)

    testImplementation(libs.androidx.arch.testing)

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
    implementation(libs.androidx.datastore.preferences)
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
    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.insetsUi)
    implementation(libs.accompanist.systemUiController)

    implementation(libs.apollo.adapters)
    implementation(libs.apollo.normalizedCache)
    androidTestImplementation(libs.apollo.idlingResource)
    testImplementation(libs.apollo.mockServer)
    testImplementation(libs.apollo.testingSupport)

    implementation(libs.arrowKt.core)

    implementation(libs.materialComponents)
    implementation(libs.flexbox)

    implementation(libs.playKtx)

    implementation(libs.fragmentViewBindingDelegate)

    implementation(libs.okhttp.core)
    implementation(libs.okhttp.loggingInterceptor)
    androidTestImplementation(libs.okhttp.mockWebServer)

    // Todo: Look into if this is the proper way to use boms with version catalogs
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.playServicesBase)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.dynamicLinks)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)

    implementation(libs.hAnalytics)

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
    implementation(libs.coil.transformations)

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
    "pullrequestImplementation"(libs.shake)

    implementation(libs.androidx.other.activityCompose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.materialIconsExtended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.mdcAdapter)
    implementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiViewBinding)
    implementation(libs.androidx.lifecycle.compose)
    androidTestImplementation(libs.androidx.compose.uiTestJunit)
    debugImplementation(libs.androidx.compose.uiTestManifest)

    implementation(libs.showkase.annotation)
    debugImplementation(libs.showkase.showkase)
    "stagingImplementation"(libs.showkase.showkase)
    "pullrequestImplementation"(libs.showkase.showkase)
    kaptDebug(libs.showkase.processor)
    "kaptStaging"(libs.showkase.processor)
    "kaptPullrequest"(libs.showkase.processor)
}

val lokaliseProperties = Properties()
lokaliseProperties.load(FileInputStream(rootProject.file("lokalise.properties")))

lokalise {
    id = lokaliseProperties.getProperty("id")
    token = lokaliseProperties.getProperty("token")

    downloadConfig = com.likandr.gradle.config.DownloadConfig()
}
