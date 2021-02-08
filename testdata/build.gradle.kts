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
    implementation(kotlin("stdlib", version = Dependencies.Versions.kotlin))
    coreLibraryDesugaring(Dependencies.coreLibraryDesugaring)

    implementation(project(":apollo"))
}
