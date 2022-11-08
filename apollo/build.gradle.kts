import com.apollographql.apollo3.compiler.MODELS_COMPAT

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.apollo)
}

dependencies {
  implementation(projects.coreCommon)

  api(libs.apollo.runtime)

  implementation(libs.adyen)
  implementation(libs.apollo.adapters)
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrowKt.core)
}

apollo {
  service("giraffe") {
    introspection {
      endpointUrl.set("https://graphql.dev.hedvigit.com/graphql")
      schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/schema.graphqls"))
    }
    schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/schema.graphqls"))
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/graphql"))

    packageName.set("com.hedvig.android.apollo.graphql")
    codegenModels.set(MODELS_COMPAT)

    generateKotlinModels.set(true)
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

    mapScalar("JSONString", "org.json.JSONObject", "com.hedvig.android.apollo.typeadapter.JSONStringAdapter")
    mapScalar("LocalDate", "java.time.LocalDate", "com.hedvig.android.apollo.typeadapter.PromiscuousLocalDateAdapter")
    mapScalar(
      "PaymentMethodsResponse",
      "com.adyen.checkout.components.model.PaymentMethodsApiResponse",
      "com.hedvig.android.apollo.typeadapter.PaymentMethodsApiResponseAdapter",
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

tasks.withType<com.apollographql.apollo3.gradle.internal.ApolloDownloadSchemaTask>().configureEach {
  doLast {
    val schemaPath = schema.get()
    val schemaFile = file(schemaPath)
    val textWithoutDoubleLineBreaks = schemaFile.readText().replace("\n\n", "\n")
    schemaFile.writeText(textWithoutDoubleLineBreaks)
  }
}
