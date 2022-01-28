package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetDanishAddressAutoCompletionUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(input: String): Either<QueryResult.Error, AddressAutoCompleteResults> {
        val addressAutocompleteQuery = AddressAutocompleteQuery(input)
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
