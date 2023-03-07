plugins {
  id("hedvig.android.apollo")
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  api(libs.apollo.api)
  implementation(libs.adyen)
  implementation(libs.apollo.adapters)
}

android {
  namespace = "com.hedvig.android.apollo.giraffe"
}

apollo {
  service("giraffe") {
    introspection {
      endpointUrl.set("https://graphql.dev.hedvigit.com/graphql")
      schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/giraffe/schema.graphqls"))
    }
    schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/giraffe/schema.graphqls"))
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/giraffe/graphql"))

    packageName.set("com.hedvig.android.apollo.graphql")
    codegenModels.set(com.apollographql.apollo3.compiler.MODELS_COMPAT)

    generateDataBuilders.set(true)
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

    mapScalar("JSONString", "org.json.JSONObject", "com.hedvig.android.apollo.giraffe.typeadapter.JSONStringAdapter")
    mapScalar(
      "LocalDate",
      "java.time.LocalDate",
      "com.hedvig.android.apollo.giraffe.typeadapter.PromiscuousLocalDateAdapter",
    )
    mapScalar(
      "PaymentMethodsResponse",
      "com.adyen.checkout.components.model.PaymentMethodsApiResponse",
      "com.hedvig.android.apollo.giraffe.typeadapter.PaymentMethodsApiResponseAdapter",
    )

    sealedClassesForEnumsMatching.set(
      listOf(
        "AuthState",
        "ClaimStatus",
        "CrossSellType",
        "EmbarkExternalRedirectLocation",
        "TypeOfContract",
      ),
    )
  }
}
