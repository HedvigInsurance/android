plugins {
  id("hedvig.gradle.plugin")
  id("hedvig.kotlin.library")
}

hedvig {
  apolloSchema {
    introspection {
      endpointUrl = "https://apollo-router.dev.hedvigit.com"
      schemaFile = file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls")
    }
    schemaFiles.setFrom(
      file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"),
      file("src/main/graphql/com/hedvig/android/apollo/octopus/extra.graphqls"),
    )
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/octopus/graphql"))

    packageName = "octopus"
    codegenModels = com.apollographql.apollo.compiler.MODELS_RESPONSE_BASED

    generateApolloMetadata = true
    @Suppress("OPT_IN_USAGE")
    generateDataBuilders = true

    failOnWarnings = true
    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables = false
    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.adapter.datetime.KotlinxLocalDateAdapter")
    mapScalar("DateTime", "kotlinx.datetime.Instant", "com.apollographql.adapter.datetime.KotlinxInstantAdapter")
    mapScalar("Instant", "kotlinx.datetime.Instant", "com.apollographql.adapter.datetime.KotlinxInstantAdapter")
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
  }
}

dependencies {
  api(libs.apollo.adapters.datetime)
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreMarkdown)
}
