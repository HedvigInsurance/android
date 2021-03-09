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
        versionName = "5.0.3"

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

    coreLibraryDesugaring(Dependencies.coreLibraryDesugaring)
    implementation(kotlin("stdlib", Dependencies.Versions.kotlin))

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.media:media:1.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("com.google.android:flexbox:2.0.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.browser:browser:1.3.0")

    // Android lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")

    implementation("com.github.Zhuinden:livedata-combinetuple-kt:1.2.1")

    // WorkManager
    val workmanager_version = "2.5.0"
    implementation("androidx.work:work-runtime:$workmanager_version")
    implementation("androidx.work:work-runtime-ktx:$workmanager_version")

    // Okhttp
    val okhttp3_version = "4.9.1"
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp3_version")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:$okhttp3_version")

    // Firebase
    implementation("com.google.android.gms:play-services-base:17.6.0")
    implementation("com.google.firebase:firebase-crashlytics:17.3.1")

    implementation("com.google.firebase:firebase-dynamic-links:19.1.1")
    implementation("com.google.firebase:firebase-config:20.0.3")
    implementation("com.google.firebase:firebase-messaging:21.0.1")

    implementation("com.mixpanel.android:mixpanel-android:5.8.6")

    // Koin
    val koin_version = "2.2.2"
    implementation("org.koin:koin-android:$koin_version")
    implementation("org.koin:koin-android-viewmodel:$koin_version")

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Slimber
    implementation("com.github.PaulWoitaschek:Slimber:1.0.7")

    // Lottie
    implementation("com.airbnb.android:lottie:3.6.1")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    // ReactiveX
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    // Svg
    implementation("com.caverock:androidsvg-aar:1.4")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.bumptech.glide:recyclerview-integration:4.12.0") {
        isTransitive = false
    }

    // Tooltip
    implementation("com.github.florent37:viewtooltip:1.2.2")

    // ZXing
    implementation("com.google.zxing:core:3.4.1")

    // insetter
    implementation("dev.chrisbanes:insetter:0.3.1")
    implementation("dev.chrisbanes:insetter-ktx:0.3.1")

    // markwon
    implementation("io.noties.markwon:core:4.6.2")

    // adyen
    implementation("com.adyen.checkout:drop-in:3.8.2")

    // JSR-354
    implementation("org.javamoney:moneta:1.4.2")

    // shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // test
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    androidTestImplementation("com.agoda.kakao:kakao:2.4.0")
    androidTestImplementation(
        "com.apollographql.apollo:apollo-idling-resource:${Dependencies.Versions.apollo}"
    )
    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:0.22")
    androidTestImplementation("org.koin:koin-test:$koin_version")
    androidTestImplementation("io.mockk:mockk-android:1.10.6")
    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.2.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
}

apply(plugin = "com.google.gms.google-services")

val lokaliseProperties = Properties()
lokaliseProperties.load(FileInputStream(rootProject.file("lokalise.properties")))

lokalise {
    id = lokaliseProperties.getProperty("id")
    token = lokaliseProperties.getProperty("token")

    downloadConfig = com.likandr.gradle.config.DownloadConfig()
}
