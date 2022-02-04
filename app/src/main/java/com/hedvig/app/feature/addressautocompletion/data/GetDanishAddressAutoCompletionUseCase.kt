package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import com.hedvig.android.owldroid.type.AddressAutocompleteType
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetDanishAddressAutoCompletionUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(
        input: DanishAddressInput,
    ): Either<QueryResult.Error, AddressAutoCompleteResults> {
        val addressAutocompleteQuery = AddressAutocompleteQuery(
            input.queryString,
            input.toAddressAutocompleteType()
        )
        return apolloClient
            .query(addressAutocompleteQuery)
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

private fun DanishAddressInput.toAddressAutocompleteType(): AddressAutocompleteType {
    val address = selectedDanishAddress
    return when {
        address == null -> AddressAutocompleteType.STREET
        address.onlyContainsAddress -> AddressAutocompleteType.BUILDING
        address.postalCode != null && address.city != null -> AddressAutocompleteType.APARTMENT
        else -> AddressAutocompleteType.STREET
    }
}
