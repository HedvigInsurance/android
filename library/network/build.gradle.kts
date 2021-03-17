plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    commonConfig()

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":apollo"))

    implementation(kotlin("stdlib", Dependencies.Versions.kotlin))

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Slimber
    implementation("com.github.PaulWoitaschek:Slimber:1.0.7")

    // adyen
    implementation("com.adyen.checkout:drop-in:3.8.2")

    // Apollo
    api("com.apollographql.apollo:apollo-runtime:${Dependencies.Versions.apollo}")
    api("com.apollographql.apollo:apollo-android-support:${Dependencies.Versions.apollo}")
    api("com.apollographql.apollo:apollo-coroutines-support:${Dependencies.Versions.apollo}")

    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("junit:junit:4.+")

    implementation("androidx.test.ext:junit:1.1.2")
    implementation("androidx.test.espresso:espresso-core:3.3.0")
}