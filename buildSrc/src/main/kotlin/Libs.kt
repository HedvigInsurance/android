object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${CommonVersions.kotlin}"
    const val coreLibraryDesugaring = "com.android.tools:desugar_jdk_libs:1.1.5"

    object Coroutines {
        private const val version = "1.4.3"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object AndroidX {
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.4"
        const val appCompat = "androidx.appcompat:appcompat:1.2.0"
        const val media = "androidx.media:media:1.3.0"
        const val dynamicAnimation = "androidx.dynamicanimation:dynamicanimation:1.0.0"
        const val preference = "androidx.preference:preference-ktx:1.1.1"
        const val core = "androidx.core:core-ktx:1.5.0-beta03"
        const val viewPager2 = "androidx.viewpager2:viewpager2:1.0.0"
        const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.2.0"
        const val fragment = "androidx.fragment:fragment-ktx:1.3.3"
        const val browser = "androidx.browser:browser:1.3.0"
        const val workManager = "androidx.work:work-runtime-ktx:2.5.0"
        const val startup = "androidx.startup:startup-runtime:1.0.0"

        object Lifecycle {
            private const val version = "2.3.1"
            const val common = "androidx.lifecycle:lifecycle-common-java8:$version"
            const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
        }

        object Test {
            private const val version = "1.3.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"
            const val junit = "androidx.test.ext:junit:1.1.2"
        }

        object Espresso {
            private const val version = "3.3.0"
            const val core = "androidx.test.espresso:espresso-core:$version"
            const val intents = "androidx.test.espresso:espresso-intents:$version"
            const val contrib = "androidx.test.espresso:espresso-contrib:$version"
        }
    }

    const val materialComponents = "com.google.android.material:material:1.3.0"
    const val flexbox = "com.google.android:flexbox:2.0.1"
    const val combineTuple = "com.github.Zhuinden:livedata-combinetuple-kt:1.2.1"
    const val fragmentViewBindingDelegate = "com.github.Zhuinden:fragmentviewbindingdelegate-kt:1.0.0"

    object OkHttp {
        private const val version = "4.9.1"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:$version"
        const val coroutines = "ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0"
    }

    object Firebase {
        const val playServicesBase = "com.google.android.gms:play-services-base:17.6.0"
        const val crashlytics = "com.google.firebase:firebase-crashlytics:17.4.1"
        const val dynamicLinks = "com.google.firebase:firebase-dynamic-links:19.1.1"
        const val config = "com.google.firebase:firebase-config:20.0.4"
        const val messaging = "com.google.firebase:firebase-messaging:21.1.0"
    }

    const val mixpanel = "com.mixpanel.android:mixpanel-android:5.9.0"

    object Koin {
        private const val version = "3.0.1"
        const val android = "io.insert-koin:koin-android:$version"
        const val test = "io.insert-koin:koin-test:$version"
    }

    const val timber = "com.jakewharton.timber:timber:4.7.1"
    const val slimber = "com.github.PaulWoitaschek:Slimber:1.0.7"
    const val lottie = "com.airbnb.android:lottie:3.7.0"

    object ReactiveX {
        const val android = "io.reactivex.rxjava2:rxandroid:2.1.1"
        const val kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    }

    const val svg = "com.caverock:androidsvg-aar:1.4"

    object Glide {
        private const val version = "4.12.0"
        const val base = "com.github.bumptech.glide:glide:$version"
        const val compiler = "com.github.bumptech.glide:compiler:$version"
        const val recyclerView = "com.github.bumptech.glide:recyclerview-integration:$version"
    }

    const val tooltip = "com.github.florent37:viewtooltip:1.2.2"
    const val ZXing = "com.google.zxing:core:3.4.1"
    const val insetter = "dev.chrisbanes:insetter-ktx:0.3.1"

    object Markwon {
        private const val version = "4.6.2"
        const val core = "io.noties.markwon:core:${version}"
        const val linkify = "io.noties.markwon:linkify:${version}"
    }

    const val moneta = "org.javamoney:moneta:1.4.2"
    const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"
    const val kaspresso = "com.kaspersky.android-components:kaspresso:1.2.0"
    const val assertK = "com.willowtreeapps.assertk:assertk-jvm:0.24"
    const val mockK = "io.mockk:mockk-android:1.11.0"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.7"
    const val shake = "com.shakebugs:shake:14.2.0"

    object Apollo {
        const val runtime = "com.apollographql.apollo:apollo-runtime:${CommonVersions.apollo}"
        const val android = "com.apollographql.apollo:apollo-android-support:${CommonVersions.apollo}"
        const val coroutines = "com.apollographql.apollo:apollo-coroutines-support:${CommonVersions.apollo}"
        const val idlingResource = "com.apollographql.apollo:apollo-idling-resource:${CommonVersions.apollo}"
    }

    const val adyen = "com.adyen.checkout:drop-in:3.8.2"
}
