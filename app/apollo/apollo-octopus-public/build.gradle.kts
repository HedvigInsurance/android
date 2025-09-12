plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apolloSchema {
    introspection {
      endpointUrl = "https://apollo-router.dev.hedvigit.com"
      schemaFile = file("src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls")
    }
    schemaFiles.setFrom(
      file("src/commonMain/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"),
      file("src/commonMain/graphql/com/hedvig/android/apollo/octopus/extra.graphqls"),
    )
    srcDir(file("src/commonMain/graphql/com/hedvig/android/apollo/octopus/graphql"))

    packageName = "octopus"
    codegenModels = com.apollographql.apollo.compiler.MODELS_RESPONSE_BASED

    generateApolloMetadata = true
    @Suppress("OPT_IN_USAGE")
    generateDataBuilders = true

    failOnWarnings = true
    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables = false
    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.adapter.datetime.KotlinxLocalDateAdapter")
    mapScalar("DateTime", "kotlin.time.Instant", "com.apollographql.adapter.core.KotlinInstantAdapter")
    mapScalar("Instant", "kotlin.time.Instant", "com.apollographql.adapter.core.KotlinInstantAdapter")
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
  }
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.apollo.adapters.core)
      api(libs.apollo.adapters.datetime)
      api(libs.apollo.api)
      api(libs.kotlinx.datetime)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreMarkdown)
    }
  }
}
