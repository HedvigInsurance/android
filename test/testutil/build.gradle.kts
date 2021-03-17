plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    commonConfig()

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":apollo"))
    implementation(project(":library:network"))

    implementation(kotlin("stdlib", Dependencies.Versions.kotlin))

    // Okhttp
    val okhttp3_version = "4.9.1"
    implementation("com.squareup.okhttp3:mockwebserver:$okhttp3_version")

    // Koin
    val koin_version = "2.2.2"
    implementation("org.koin:koin-android:$koin_version")
    implementation("org.koin:koin-test:$koin_version")

    implementation("io.mockk:mockk-android:1.10.6")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("junit:junit:4.+")


    implementation("androidx.test.ext:junit:1.1.2")
    implementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.test.espresso:espresso-intents:3.3.0")
    implementation("com.agoda.kakao:kakao:2.4.0")
}