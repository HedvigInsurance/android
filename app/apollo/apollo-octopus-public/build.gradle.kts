plugins {
  id("hedvig.android.apollo")
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.apollo.api)
  api(libs.kotlinx.datetime)

  implementation(libs.apollo.adapters)
  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
  implementation(projects.coreCommonPublic)
}

android {
  namespace = "com.hedvig.android.apollo.octopus"
}

apollo {
  generateSourcesDuringGradleSync.set(false)
  service("octopus") {
    introspection {
      endpointUrl.set("https://apollo-router.dev.hedvigit.com")
      schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"))
    }
    schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"))
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/octopus/graphql"))

    packageName.set("octopus")
    codegenModels.set(com.apollographql.apollo3.compiler.MODELS_RESPONSE_BASED)

    // https://www.apollographql.com/docs/android/advanced/operation-variables/#make-nullable-variables-non-optional
    generateOptionalOperationVariables.set(false)

    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.apollo3.adapter.KotlinxLocalDateAdapter")
    mapScalarToKotlinString("UUID")
    mapScalarToKotlinString("Url")
    mapScalarToKotlinString("FlowContext")
  }
}
