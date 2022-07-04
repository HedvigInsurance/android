plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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
        buildConfig = false
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.assertK)
    testImplementation(libs.jsonTest)
}
