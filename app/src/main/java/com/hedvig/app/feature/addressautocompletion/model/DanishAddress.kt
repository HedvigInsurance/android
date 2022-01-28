package com.hedvig.app.feature.addressautocompletion.model

import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import java.util.UUID

data class DanishAddress(
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
        fun fromDto(dto: AddressAutocompleteQuery.AutoCompleteAddress): DanishAddress {
            return DanishAddress(
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
