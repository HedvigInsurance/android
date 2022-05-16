plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.apollographql.apollo3")
}

apollo {
    generateKotlinModels.set(true)
    customTypeMapping.set(
        mapOf(
            "URL" to "kotlin.String",
            "LocalDate" to "java.time.LocalDate",
            "Upload" to "com.apollographql.apollo3.api.FileUpload",
            "PaymentMethodsResponse" to "com.adyen.checkout.components.model.PaymentMethodsApiResponse",
            "CheckoutPaymentsAction" to "kotlin.String",
            "CheckoutPaymentAction" to "kotlin.String",
            "JSONString" to "org.json.JSONObject",
            "Instant" to "java.time.Instant",
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

    api(libs.apollo.runtime)

    implementation(libs.adyen)
}
