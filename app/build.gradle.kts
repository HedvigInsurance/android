import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("com.hedvig.android.lokalise")
}

apply(plugin = "com.jaredsburrows.license")
configure<com.jaredsburrows.license.LicenseReportExtension> {
    copyHtmlReportToAssets = true
}

android {
    commonConfig()

    buildFeatures {
        viewBinding = true
        aidl = false
        renderScript = false
    }

    defaultConfig {
        applicationId = "com.hedvig"

        versionCode = 43
        versionName = "5.1.6"

        vectorDrawables.useSupportLibrary = true

        resConfigs("en", "en-rNO", "en-rSE", "en-rDK", "nb-rNO", "sv-rSE", "da-rDK")

        testInstrumentationRunner = "com.hedvig.app.TestRunner"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lintOptions {
        isAbortOnError = false
    }

    packagingOptions {
        exclude("javamoney.properties")
        exclude("README.txt")
    }

    buildTypes {
        maybeCreate("staging")
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

    androidTestImplementation(project(":testdata"))
    debugImplementation(project(":testdata"))

    "stagingImplementation"(project(":testdata"))

    coreLibraryDesugaring(Libs.coreLibraryDesugaring)
    implementation(Libs.kotlin)

    implementation(Libs.Coroutines.core)
    implementation(Libs.Coroutines.android)
    testImplementation(Libs.Coroutines.test)

    implementation(Libs.AndroidX.appCompat)
    implementation(Libs.AndroidX.media)
    implementation(Libs.AndroidX.constraintLayout)
    implementation(Libs.AndroidX.dynamicAnimation)
    implementation(Libs.AndroidX.preference)
    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.viewPager2)
    implementation(Libs.AndroidX.swipeRefreshLayout)
    implementation(Libs.AndroidX.recyclerView)
    implementation(Libs.AndroidX.fragment)
    implementation(Libs.AndroidX.browser)
    implementation(Libs.AndroidX.Lifecycle.common)
    implementation(Libs.AndroidX.Lifecycle.liveData)
    implementation(Libs.AndroidX.Lifecycle.runtime)
    implementation(Libs.AndroidX.Lifecycle.viewModel)
    implementation(Libs.AndroidX.workManager)
    debugImplementation(Libs.AndroidX.startup)
    "stagingImplementation"(Libs.AndroidX.startup)
    androidTestImplementation(Libs.AndroidX.Espresso.core)
    androidTestImplementation(Libs.AndroidX.Espresso.intents)
    androidTestImplementation(Libs.AndroidX.Espresso.contrib)
    testImplementation(Libs.AndroidX.Test.junit)
    testImplementation(Libs.jsonTest)
    androidTestImplementation(Libs.AndroidX.Test.runner)
    androidTestImplementation(Libs.AndroidX.Test.rules)
    androidTestImplementation(Libs.AndroidX.Test.junit)

    implementation(Libs.materialComponents)
    implementation(Libs.flexbox)

    implementation(Libs.combineTuple)
    implementation(Libs.fragmentViewBindingDelegate)

    implementation(Libs.OkHttp.loggingInterceptor)
    implementation(Libs.OkHttp.coroutines)
    androidTestImplementation(Libs.OkHttp.mockWebServer)

    implementation(Libs.Firebase.playServicesBase)
    implementation(Libs.Firebase.crashlytics)
    implementation(Libs.Firebase.dynamicLinks)
    implementation(Libs.Firebase.config)
    implementation(Libs.Firebase.messaging)

    implementation(Libs.mixpanel)

    implementation(Libs.Koin.android)
    androidTestImplementation(Libs.Koin.test)

    implementation(Libs.timber)
    implementation(Libs.slimber)

    implementation(Libs.lottie)

    implementation(Libs.ReactiveX.kotlin)
    implementation(Libs.ReactiveX.android)

    implementation(Libs.svg)

    implementation(Libs.Glide.base)
    kapt(Libs.Glide.compiler)
    implementation(Libs.Glide.recyclerView) {
        isTransitive = false
    }

    implementation(Libs.tooltip)

    implementation(Libs.ZXing)

    implementation(Libs.insetter)

    implementation(Libs.Markwon.core)
    implementation(Libs.Markwon.linkify)

    implementation(Libs.adyen)

    implementation(Libs.moneta)

    implementation(Libs.shimmer)

    androidTestImplementation(Libs.Apollo.idlingResource)

    testImplementation(Libs.assertK)
    androidTestImplementation(Libs.assertK)
    androidTestImplementation(Libs.kaspresso)

    androidTestImplementation(Libs.MockK.android)
    testImplementation(Libs.MockK.jvm)

    debugImplementation(Libs.leakCanary)
    debugImplementation(Libs.shake)
    "stagingImplementation"(Libs.shake)
}

apply(plugin = "com.google.gms.google-services")

val lokaliseProperties = Properties()
lokaliseProperties.load(FileInputStream(rootProject.file("lokalise.properties")))

lokalise {
    id = lokaliseProperties.getProperty("id")
    token = lokaliseProperties.getProperty("token")

    downloadConfig = com.likandr.gradle.config.DownloadConfig()
}
