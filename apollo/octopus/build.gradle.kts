plugins {
  id("hedvig.android.apollo")
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreCommon)

  api(libs.apollo.api)
  api(libs.kotlinx.datetime)
  implementation(libs.apollo.adapters)
  implementation(libs.apollo.runtime)
  implementation(libs.koin.core)
}

android {
  namespace = "com.hedvig.android.apollo.octopus"
}

apollo {
  service("octopus") {
    introspection {
      endpointUrl.set("https://apollo-router.dev.hedvigit.com")
      schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"))
    }
    schemaFile.set(file("src/main/graphql/com/hedvig/android/apollo/octopus/schema.graphqls"))
    srcDir(file("src/main/graphql/com/hedvig/android/apollo/octopus/graphql"))

    packageName.set("octopus")
    codegenModels.set(com.apollographql.apollo3.compiler.MODELS_RESPONSE_BASED)

    mapScalar("Date", "kotlinx.datetime.LocalDate", "com.apollographql.apollo3.adapter.KotlinxLocalDateAdapter")
  }
}
