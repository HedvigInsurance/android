package com.hedvig.app.feature.embark.passages.externalinsurer

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.InsuranceProvidersQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.app.LanguageService
import com.hedvig.app.isDebug

sealed class InsuranceProvidersResult {
  data class Success(val providers: List<InsuranceProvider>) : InsuranceProvidersResult()
  sealed class Error : InsuranceProvidersResult() {
    object NetworkError : Error()
  }
}

data class InsuranceProvider(
  val id: String,
  val name: String,
  val collectionId: String? = null,
)

class GetInsuranceProvidersUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun getInsuranceProviders(): InsuranceProvidersResult {
    val insuranceProviders = InsuranceProvidersQuery(languageService.getGraphQLLocale())
    return when (val result = apolloClient.query(insuranceProviders).safeExecute()) {
      is OperationResult.Success -> createSuccessResult(result)
      is OperationResult.Error -> InsuranceProvidersResult.Error.NetworkError
    }
  }

  private fun createSuccessResult(
    result: OperationResult.Success<InsuranceProvidersQuery.Data>,
  ): InsuranceProvidersResult.Success {
    var providers = result.data.insuranceProviders.map {
      InsuranceProvider(
        it.id,
        it.name,
        it.externalCollectionId,
      )
    }
    if (isDebug()) {
      providers = providers + InsuranceProvider(
        "se-demo",
        "Demo",
        "se-demo",
      )
    }
    return InsuranceProvidersResult.Success(providers)
  }
}
