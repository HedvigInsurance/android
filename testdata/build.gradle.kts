plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    commonConfig()

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
    implementation(Libs.kotlin)
    coreLibraryDesugaring(Libs.coreLibraryDesugaring)

    implementation(project(":apollo"))
}
