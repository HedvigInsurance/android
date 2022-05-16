import com.apollographql.apollo3.compiler.MODELS_COMPAT

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.apollographql.apollo3")
}

apollo {
    service("giraffe") {
        introspection {
            endpointUrl.set("https://graphql.dev.hedvigit.com/graphql")
            schemaFile.set(file("src/main/graphql/com/hedvig/android/owldroid/schema.graphqls"))
        }
        schemaFile.set(file("src/main/graphql/com/hedvig/android/owldroid/schema.graphqls"))
        srcDir(file("src/main/graphql/com/hedvig/android/owldroid/graphql"))

        packageName.set("com.hedvig.android.owldroid.type")
        codegenModels.set(MODELS_COMPAT)

        // Test builders setup
        generateKotlinModels.set(true)
        generateTestBuilders.set(true)
        testDirConnection {
            // Make test builders available to main (not just test or androidTest) to be used by our mock data
            connectToAndroidSourceSet("main")
        }

        // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
        generateOptionalOperationVariables.set(false)
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
