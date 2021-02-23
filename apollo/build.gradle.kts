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
            "CheckoutPaymentsAction" to "kotlin.String"
        )
    )
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

    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    // Apollo
    api("com.apollographql.apollo:apollo-runtime:${Dependencies.Versions.apollo}")
    api("com.apollographql.apollo:apollo-android-support:${Dependencies.Versions.apollo}")
    api("com.apollographql.apollo:apollo-coroutines-support:${Dependencies.Versions.apollo}")

    // adyen
    implementation("com.adyen.checkout:drop-in:3.8.2")
}
