package com.hedvig.app.feature.embark.passages.addressautocomplete

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import com.hedvig.app.util.apollo.safeQuery
import java.util.UUID

class GetDanishAddressAutoCompletionUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(input: String): Either<Error, AddressAutoCompleteResults> {
        val addressAutocompleteQuery = AddressAutocompleteQuery(input)
        return apolloClient
            .query(addressAutocompleteQuery)
            .safeQuery()
            .toEither { Error.NetworkError }
            .map { queryData ->
                AddressAutoCompleteResults(queryData.autoCompleteAddress.map(AddressResult::fromDto))
            }
    }
}

data class AddressAutoCompleteResults(
    val results: List<AddressResult>,
)

data class AddressResult(
    val id: String,
    val address: String,
    val streetName: String?,
    val streetNumber: String?,
    val floor: String?,
    val apartment: String?,
    val postalCode: String?,
    val city: String?,
) {
    companion object {
        fun fromDto(dto: AddressAutocompleteQuery.AutoCompleteAddress): AddressResult {
            return AddressResult(
                id = dto.id ?: UUID.randomUUID().toString(),
                address = dto.address,
                streetName = dto.streetName,
                streetNumber = dto.streetNumber,
                floor = dto.floor,
                apartment = dto.apartment,
                postalCode = dto.postalCode,
                city = dto.city,
            )
        }
    }
}

sealed interface Error {
    object NetworkError : Error
}
