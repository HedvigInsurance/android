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
            "PaymentMethodsResponse" to "com.adyen.checkout.base.model.PaymentMethodsApiResponse",
            "CheckoutPaymentsAction" to "kotlin.String",
            "JSONString" to "org.json.JSONObject"
        )
    )
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

    implementation(Libs.AndroidX.constraintLayout)

    api(Libs.Apollo.runtime)
    api(Libs.Apollo.android)
    api(Libs.Apollo.coroutines)

    implementation(Libs.adyen)
}
