object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${CommonVersions.kotlin}"
    const val coreLibraryDesugaring = "com.android.tools:desugar_jdk_libs:1.1.5"

    object Coroutines {
        private const val version = "1.5.2"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object Accompanist {
        private const val version = "0.19.0"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerIndicators = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
    }

    object AndroidX {
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
        const val appCompat = "androidx.appcompat:appcompat:1.3.1"
        const val media = "androidx.media:media:1.4.2"
        const val dynamicAnimation = "androidx.dynamicanimation:dynamicanimation:1.0.0"
        const val preference = "androidx.preference:preference-ktx:1.1.1"
        const val core = "androidx.core:core-ktx:1.6.0"
        const val viewPager2 = "androidx.viewpager2:viewpager2:1.0.0"
        const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
        const val fragment = "androidx.fragment:fragment-ktx:1.3.6"
        const val browser = "androidx.browser:browser:1.3.0"
        const val workManager = "androidx.work:work-runtime-ktx:2.7.0"
        const val startup = "androidx.startup:startup-runtime:1.1.0"
        const val activityCompose = "androidx.activity:activity-compose:1.3.1"

        object DataStore {
            const val preferences = "androidx.datastore:datastore-preferences:1.0.0"
            const val core = "androidx.datastore:datastore-core:1.0.0"
        }

        object Lifecycle {
            private const val version = "2.4.0-rc01"
            const val common = "androidx.lifecycle:lifecycle-common-java8:$version"
            const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val compose = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
        }

        object Test {
            private const val version = "1.4.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"
            const val junit = "androidx.test.ext:junit:1.1.3"
        }

        object Espresso {
            private const val version = "3.4.0"
            const val core = "androidx.test.espresso:espresso-core:$version"
            const val intents = "androidx.test.espresso:espresso-intents:$version"
            const val contrib = "androidx.test.espresso:espresso-contrib:$version"
        }

        object Compose {
            private const val version = "1.0.3"
            const val material = "androidx.compose.material:material:$version"
            const val animation = "androidx.compose.animation:animation:$version"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
            const val uiTestJunit = "androidx.compose.ui:ui-test-junit4:$version"
            const val mdcAdapter = "com.google.android.material:compose-theme-adapter:$version"
            const val uiTestManifest = "androidx.compose.ui:ui-test-manifest:$version"
        }
    }

    const val materialComponents = "com.google.android.material:material:1.4.0"
    const val flexbox = "com.google.android.flexbox:flexbox:3.0.0"
    const val combineTuple = "com.github.Zhuinden:livedata-combinetuple-kt:1.2.1"
    const val fragmentViewBindingDelegate = "com.github.Zhuinden:fragmentviewbindingdelegate-kt:1.0.0"

    const val playKtx = "com.google.android.play:core-ktx:1.8.1"

    object OkHttp {
        private const val version = "4.9.2"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:$version"
        const val coroutines = "ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0"
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:28.4.2"
        const val playServicesBase = "com.google.android.gms:play-services-base"
        const val crashlytics = "com.google.firebase:firebase-crashlytics"
        const val dynamicLinks = "com.google.firebase:firebase-dynamic-links"
        const val config = "com.google.firebase:firebase-config"
        const val messaging = "com.google.firebase:firebase-messaging"
        const val tracking = "com.google.firebase:firebase-analytics-ktx"
    }

    const val mixpanel = "com.mixpanel.android:mixpanel-android:5.9.4"

    object Koin {
        private const val version = "3.1.2"
        const val android = "io.insert-koin:koin-android:$version"
        const val test = "io.insert-koin:koin-test:$version"
    }

    const val timber = "com.jakewharton.timber:timber:5.0.1"
    const val slimber = "com.github.PaulWoitaschek:Slimber:1.0.7"
    const val lottie = "com.airbnb.android:lottie:4.2.0"

    object ReactiveX {
        const val android = "io.reactivex.rxjava2:rxandroid:2.1.1"
        const val kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    }

    const val svg = "com.caverock:androidsvg-aar:1.4"

    object Coil {
        private const val version = "1.4.0"
        const val svg = "io.coil-kt:coil-svg:$version"
        const val gif = "io.coil-kt:coil-gif:$version"
        const val coil = "io.coil-kt:coil:$version"
        const val compose = "io.coil-kt:coil-compose:$version"

        const val transformations = "com.github.Commit451.coil-transformations:transformations:1.0.0"
    }

    const val tooltip = "com.github.hansemannn:viewtooltip:1.2.2"
    const val ZXing = "com.google.zxing:core:3.4.1"
    const val insetter = "dev.chrisbanes.insetter:insetter:0.6.0"

    object Markwon {
        private const val version = "4.6.2"
        const val core = "io.noties.markwon:core:$version"
        const val linkify = "io.noties.markwon:linkify:$version"
    }

    const val moneta = "org.javamoney:moneta:1.4.2"
    const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"
    const val kaspresso = "com.kaspersky.android-components:kaspresso:1.2.1"
    const val assertK = "com.willowtreeapps.assertk:assertk-jvm:0.25"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.7"
    const val shake = "com.shakebugs:shake:15.0.0"

    object MockK {
        private const val version = "1.12.0"
        const val android = "io.mockk:mockk-android:$version"
        const val jvm = "io.mockk:mockk:$version"
    }

    object Apollo {
        const val runtime = "com.apollographql.apollo:apollo-runtime:${CommonVersions.apollo}"
        const val android = "com.apollographql.apollo:apollo-android-support:${CommonVersions.apollo}"
        const val coroutines = "com.apollographql.apollo:apollo-coroutines-support:${CommonVersions.apollo}"
        const val idlingResource = "com.apollographql.apollo:apollo-idling-resource:${CommonVersions.apollo}"
    }

    object Showkase {
        private const val version = "1.0.0-beta05"
        const val showkase = "com.airbnb.android:showkase:$version"
        const val processor = "com.airbnb.android:showkase-processor:$version"
    }

    const val adyen = "com.adyen.checkout:drop-in:4.2.0"
    const val jsonTest = "org.json:json:20210307"
    const val concatAdapterExtension = "com.github.carousell:ConcatAdapterExtension:1.2.1"
}
