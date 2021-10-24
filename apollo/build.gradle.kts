
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
            "JSONString" to "org.json.JSONObject"
        )
    )
    sealedClassesForEnumsMatching.set(
        listOf(
            "TypeOfContract",
        )
    )
}

android {
    // region TODO Extract this to a LibraryExtension.commonConfig() again
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        maybeCreate("staging")

        named("debug") {}
        named("staging") {}
        named("release") {}
    }
    // endregion

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
    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    implementation(libs.androidx.other.constraintLayout)

    api(libs.apollo.runtime)
    api(libs.apollo.android)
    api(libs.apollo.coroutines)

    implementation(libs.adyen)
}
