plugins {
    id("hedvig.android.library")
    id("hedvig.android.ktlint")
}

dependencies {
    implementation(projects.apollo)
    implementation(libs.adyen)
}
