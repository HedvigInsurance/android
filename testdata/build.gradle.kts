plugins {
    id("hedvig.android.library")
}

dependencies {
    implementation(project(":apollo"))
    implementation(libs.adyen)
}
