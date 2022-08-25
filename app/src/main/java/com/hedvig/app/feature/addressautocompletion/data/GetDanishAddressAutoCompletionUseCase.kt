package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import com.hedvig.android.owldroid.graphql.type.AddressAutocompleteType
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither

class GetDanishAddressAutoCompletionUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend operator fun invoke(
    input: DanishAddressInput,
  ): Either<QueryResult.Error, AddressAutoCompleteResults> {
    return if (input.selectedAddress != null) {
      invoke(input.selectedAddress)
    } else {
      invoke(input.rawText)
    }
  }

  suspend operator fun invoke(
    address: DanishAddress,
    addressAutocompleteType: AddressAutocompleteType? = null,
  ): Either<QueryResult.Error, AddressAutoCompleteResults> {
    val query = AddressAutocompleteQuery(
      address.toQueryString(),
      addressAutocompleteType ?: address.toAddressAutocompleteType(),
    )
    return runQuery(query)
  }

  private suspend operator fun invoke(
    addressText: String,
  ): Either<QueryResult.Error, AddressAutoCompleteResults> {
    val query = AddressAutocompleteQuery(
      addressText,
      AddressAutocompleteType.STREET,
    )
    return runQuery(query)
  }

  private suspend fun runQuery(
    query: AddressAutocompleteQuery,
  ): Either<QueryResult.Error, AddressAutoCompleteResults> {
    return apolloClient
      .query(query)
      .safeQuery()
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
