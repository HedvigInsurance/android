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
    implementation(Libs.kotlin)
    coreLibraryDesugaring(Libs.coreLibraryDesugaring)

    implementation(project(":apollo"))
}
