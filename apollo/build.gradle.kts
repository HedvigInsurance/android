import com.apollographql.apollo3.compiler.MODELS_COMPAT

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("hedvig.android.library")
    alias(libs.plugins.apollo)
}

apollo {
    service("giraffe") {
        introspection {
            endpointUrl.set("https://graphql.dev.hedvigit.com/graphql")
            schemaFile.set(file("src/main/graphql/com/hedvig/android/owldroid/schema.graphqls"))
        }
        schemaFile.set(file("src/main/graphql/com/hedvig/android/owldroid/schema.graphqls"))
        srcDir(file("src/main/graphql/com/hedvig/android/owldroid/graphql"))

        packageName.set("com.hedvig.android.owldroid.graphql")
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

        mapScalarToKotlinString("URL")
        mapScalarToKotlinString("CheckoutPaymentsAction")
        mapScalarToKotlinString("CheckoutPaymentAction")
        mapScalarToUpload("Upload")
        mapScalar("Instant", "java.time.Instant", "com.apollographql.apollo3.adapter.JavaInstantAdapter")

        mapScalar("JSONString", "org.json.JSONObject", "com.hedvig.android.typeadapter.JSONStringAdapter")
        mapScalar("LocalDate", "java.time.LocalDate", "com.hedvig.android.typeadapter.PromiscuousLocalDateAdapter")
        mapScalar(
            "PaymentMethodsResponse",
            "com.adyen.checkout.components.model.PaymentMethodsApiResponse",
            "com.hedvig.android.typeadapter.PaymentMethodsApiResponseAdapter"
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

dependencies {
    implementation(project(":core-common"))

    api(libs.apollo.runtime)
    implementation(libs.apollo.adapters)

    implementation(libs.adyen)
}
