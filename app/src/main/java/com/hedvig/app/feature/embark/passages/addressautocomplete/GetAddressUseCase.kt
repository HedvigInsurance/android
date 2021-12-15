package com.hedvig.app.feature.embark.passages.addressautocomplete

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import com.hedvig.android.owldroid.type.AddressAutocompleteType
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetAddressUseCase(
    private val apolloClient: ApolloClient
) {
    suspend operator fun invoke(input: String, addressType: AddressType) {
        val addressAutocompleteQuery = AddressAutocompleteQuery(input, addressType.toAddressAutoCompleteType())
        when (apolloClient.query(addressAutocompleteQuery).safeQuery()) {
            is QueryResult.Error.QueryError -> TODO()
            is QueryResult.Success -> TODO()
        }
    }
}

sealed class AddressAutoCompleteResult {
    object Success : AddressAutoCompleteResult()
}

private fun AddressType.toAddressAutoCompleteType() = when (this) {
    AddressType.STREET -> AddressAutocompleteType.STREET
    AddressType.BUILDING -> AddressAutocompleteType.BUILDING
    AddressType.APARTMENT -> AddressAutocompleteType.APARTMENT
}
