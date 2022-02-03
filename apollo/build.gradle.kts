plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.apollographql.apollo")
}

apollo {
    generateKotlinModels.set(true)
    customTypeMapping.set(
        mapOf(
            "URL" to "kotlin.String",
            "LocalDate" to "java.time.LocalDate",
            "Upload" to "com.apollographql.apollo.api.FileUpload",
            "PaymentMethodsResponse" to "com.adyen.checkout.components.model.PaymentMethodsApiResponse",
            "CheckoutPaymentsAction" to "kotlin.String",
            "JSONString" to "org.json.JSONObject",
            "Instant" to "java.time.Instant"
        )
    )
    sealedClassesForEnumsMatching.set(
        listOf(
            "TypeOfContract",
            "CrossSellType",
            "ClaimStatus",
            "EmbarkExternalRedirectLocation",
        )
    )
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

    implementation(libs.androidx.other.constraintLayout)

    api(libs.apollo.runtime)
    api(libs.apollo.android)
    api(libs.apollo.coroutines)

    implementation(libs.adyen)
}
