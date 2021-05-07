import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.hedvig.android.lokalise")
}

apply(plugin = "com.jaredsburrows.license")
configure<com.jaredsburrows.license.LicenseReportExtension> {
    copyHtmlReportToAssets = true
}

android {
    commonConfig()

    androidExtensions {
        isExperimental = true
    }
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.hedvig"

        versionCode = 43
        versionName = "5.1.1"

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

            buildConfigField("String", "APP_ID", "\"com.hedvig.app\"")
            buildConfigField("String", "GRAPHQL_URL", "\"https://giraffe.hedvig.com/graphql\"")
            buildConfigField(
                "String",
                "WS_GRAPHQL_URL",
                "\"wss://giraffe.hedvig.com/subscriptions\""
            )
            buildConfigField("String", "BASE_URL", "\"https://giraffe.hedvig.com\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://www.hedvig.com\"")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true
            resValue("string", "file_provider_authority", "\"com.hedvig.android.file.provider\"")

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

            buildConfigField("String", "APP_ID", "\"com.hedvig.test.app\"")
            buildConfigField(
                "String",
                "GRAPHQL_URL",
                "\"https://graphql.dev.hedvigit.com/graphql\""
            )
            buildConfigField(
                "String",
                "WS_GRAPHQL_URL",
                "\"wss://graphql.dev.hedvigit.com/subscriptions\""
            )
            buildConfigField("String", "BASE_URL", "\"https://graphql.dev.hedvigit.com\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://www.dev.hedvigit.com\"")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = true
            resValue(
                "string",
                "file_provider_authority",
                "\"com.hedvig.android.test.file.provider\""
            )

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

            buildConfigField("String", "APP_ID", "\"com.hedvig.dev.app\"")
            buildConfigField(
                "String",
                "GRAPHQL_URL",
                "\"https://graphql.dev.hedvigit.com/graphql\""
            )
            buildConfigField(
                "String",
                "WS_GRAPHQL_URL",
                "\"wss://graphql.dev.hedvigit.com/subscriptions\""
            )
            buildConfigField("String", "BASE_URL", "\"https://graphql.dev.hedvigit.com\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://www.dev.hedvigit.com\"")
            manifestPlaceholders["firebaseCrashlyticsCollectionEnabled"] = false

            resValue(
                "string",
                "file_provider_authority",
                "\"com.hedvig.android.dev.file.provider\""
            )

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
    implementation(Libs.AndroidX.Lifecycle.viewModel)
    implementation(Libs.AndroidX.workManager)
    debugImplementation(Libs.AndroidX.startup)
    "stagingImplementation"(Libs.AndroidX.startup)
    androidTestImplementation(Libs.AndroidX.Espresso.core)
    androidTestImplementation(Libs.AndroidX.Espresso.intents)
    androidTestImplementation(Libs.AndroidX.Espresso.contrib)
    testImplementation(Libs.AndroidX.Test.junit)
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
    androidTestImplementation(Libs.mockK)
    androidTestImplementation(Libs.kaspresso)

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
