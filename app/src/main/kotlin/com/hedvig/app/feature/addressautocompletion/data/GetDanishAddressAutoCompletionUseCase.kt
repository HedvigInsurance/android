package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.AddressAutocompleteQuery
import com.hedvig.android.apollo.graphql.type.AddressAutocompleteType
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput

class GetDanishAddressAutoCompletionUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend operator fun invoke(
    input: DanishAddressInput,
  ): Either<OperationResult.Error, AddressAutoCompleteResults> {
    return if (input.selectedAddress != null) {
      invoke(input.selectedAddress)
    } else {
      invoke(input.rawText)
    }
  }

  suspend operator fun invoke(
    address: DanishAddress,
    addressAutocompleteType: AddressAutocompleteType? = null,
  ): Either<OperationResult.Error, AddressAutoCompleteResults> {
    val query = AddressAutocompleteQuery(
      address.toQueryString(),
      addressAutocompleteType ?: address.toAddressAutocompleteType(),
    )
    return runQuery(query)
  }

  private suspend operator fun invoke(
    addressText: String,
  ): Either<OperationResult.Error, AddressAutoCompleteResults> {
    val query = AddressAutocompleteQuery(
      addressText,
      AddressAutocompleteType.STREET,
    )
    return runQuery(query)
  }

  private suspend fun runQuery(
    query: AddressAutocompleteQuery,
  ): Either<OperationResult.Error, AddressAutoCompleteResults> {
    return apolloClient
      .query(query)
      .safeExecute()
      .toEither()
      .map { queryData ->
        AddressAutoCompleteResults(queryData.autoCompleteAddress.map(DanishAddress::fromDto))
      }
  }
}

data class AddressAutoCompleteResults(
  val resultList: List<DanishAddress>,
)

private fun DanishAddress.toAddressAutocompleteType(): AddressAutocompleteType {
  return when {
    isValidFinalSelection -> AddressAutocompleteType.APARTMENT
    else -> AddressAutocompleteType.BUILDING
  }
}
