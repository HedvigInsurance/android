package com.hedvig.app.feature.offer.usecase.providerstatus

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.ProviderStatusQuery
import com.hedvig.app.util.apollo.OperationResult
import com.hedvig.app.util.apollo.safeExecute

class GetProviderDisplayNameUseCase(
  private val apolloClient: ApolloClient,
) {
  /**
   * [insuranceCompany] is the code name for companies that usually start with a "se" prefix and have dashes instead
   * of spaces.
   * An example input is "se-demo"
   */
  suspend fun invoke(insuranceCompany: String?): String? {
    if (insuranceCompany == null) return null
    val result = apolloClient
      .query(ProviderStatusQuery())
      .safeExecute()
    if (result is OperationResult.Success) {
      return result.data.externalInsuranceProvider
        ?.providerStatusV2
        ?.firstOrNull { it.insuranceProvider == insuranceCompany }
        ?.insuranceProviderDisplayName
    }
    return null
  }
}
