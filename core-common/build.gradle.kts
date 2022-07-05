plugins {
    id("hedvig.android.library")
    id("hedvig.android.ktlint")
}

dependencies {
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.assertK)
    testImplementation(libs.jsonTest)
}
