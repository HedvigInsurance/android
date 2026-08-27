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
    codegenModels = "responseBased"

    generateApolloMetadata = true
    @Suppress("OPT_IN_USAGE")
    generateDataBuilders = true
    // Apollo 5 emits data builders to a separate source set that is not compiled by default.
    // Connect them to commonMain so the generated `octopus.builder.*` type builders are part of
    // this module's published API and available to the (test) code of downstream feature modules.
    dataBuildersOutputDirConnection {
      connectToKotlinSourceSet("commonMain")
    }

    failOnWarnings = true
    // This is the shared schema/fragments module. Fragments defined here are consumed by downstream
    // feature modules, so "unused" from this module's own (operation-less) perspective is expected
    // and not a real signal. Ignore only that issue; every other warning still fails the build.
    issueSeverity("UnusedFragment", "ignore")
    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables = false
    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.adapter.datetime.KotlinxLocalDateAdapter")
    mapScalar("DateTime", "kotlin.time.Instant", "com.apollographql.adapter.core.KotlinInstantAdapter")
    mapScalar("Instant", "kotlin.time.Instant", "com.apollographql.adapter.core.KotlinInstantAdapter")
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
    mapScalarToKotlinString("ClaimIntentStepContentFormFieldValue")
  }
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(libs.apollo.adapters.core)
      api(libs.apollo.adapters.datetime)
      api(libs.apollo.api)
      api(libs.apollo.normalizedCache)
      api(libs.kotlinx.datetime)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreMarkdown)
    }
  }
}
