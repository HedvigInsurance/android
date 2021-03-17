plugins {
    id("com.android.dynamic-feature")
    id("com.google.firebase.crashlytics")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    commonConfig()

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures.viewBinding = true

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        named("debug") {
            java.srcDir("src/engineering/java")
            res.srcDir("src/engineering/res")
            manifest.srcFile("src/debug/AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation(project(":app"))
    implementation(project(":apollo"))

    testImplementation(project(":app"))

    androidTestImplementation(project(":test:testutil"))
    androidTestImplementation(project(":testdata"))

    debugImplementation(project(":testdata"))

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
    implementation("androidx.fragment:fragment-ktx:1.3.0-rc01")
    implementation("androidx.browser:browser:1.3.0")

    implementation("com.google.android.gms:play-services-instantapps:17.0.0")

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
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")

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
    testImplementation("junit:junit:4.13")
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
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.22")
    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:0.22")
    androidTestImplementation("org.koin:koin-test:$koin_version")
    androidTestImplementation("io.mockk:mockk-android:1.10.6")
    androidTestImplementation("com.kaspersky.android-components:kaspresso:1.2.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
}
