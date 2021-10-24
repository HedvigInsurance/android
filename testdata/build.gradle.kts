plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    // region TODO Extract this to a LibraryExtension.commonConfig() again
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        maybeCreate("staging")

        named("debug") {}
        named("staging") {}
        named("release") {}
    }
    // endregion

    buildFeatures {
        buildConfig = false
        viewBinding = false
        dataBinding = false
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    implementation(project(":apollo"))
}
