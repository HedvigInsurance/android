plugins {
  id("hedvig.android.apollo")
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.adapters.datetime)
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)
  implementation(projects.coreBuildConstants)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreMarkdown)
}

apollo {
  // Octopus client
  service("octopus") {
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
    generateDataBuilders = true

    failOnWarnings = true
    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables = false
    outputDirConnection {
      // main is by default but setting this explicitly fixed the warning "Duplicate content roots detected.
      // connectToKotlinSourceSet("main")
      connectToAllAndroidVariants()
    }
    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.adapter.datetime.KotlinxLocalDateAdapter")
    mapScalar("DateTime", "kotlinx.datetime.Instant", "com.apollographql.adapter.datetime.KotlinxInstantAdapter")
    mapScalar("Instant", "kotlinx.datetime.Instant", "com.apollographql.adapter.datetime.KotlinxInstantAdapter")
    mapScalar(
      "Markdown",
      "com.hedvig.android.core.markdown.MarkdownString",
      "com.hedvig.android.apollo.octopus.MarkdownStringAdapter",
    )
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
  }
}
