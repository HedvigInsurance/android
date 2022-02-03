package com.hedvig.app.feature.addressautocompletion.model

import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import java.util.UUID

data class DanishAddress(
    val id: String?,
    val address: String,
    val streetName: String?,
    val streetNumber: String?,
    val floor: String?,
    val apartment: String?,
    val postalCode: String?,
    val city: String?,
) {

    private val onlyAddressIsNotNull: Boolean
        get() = streetName == null &&
            streetNumber == null &&
            floor == null &&
            apartment == null &&
            postalCode == null &&
            city == null

    val isValidFinalSelection: Boolean
        get() = streetName != null &&
            streetNumber != null &&
            postalCode != null &&
            city != null

    fun toPresentableResult(): Pair<String, String> {
        val topString = buildString {
            append("$streetName $streetNumber")
            if (floor == null) return@buildString
            append(", $floor.")
            if (apartment == null) return@buildString
            append(" $apartment")
        }.trim()
        val bottomString = "$postalCode $city"
        return topString to bottomString
    }

    fun toFlatQueryString(): String {
        if (onlyAddressIsNotNull) {
            return address
        }
        return buildString {
            appendIfNotNull(streetName)
            appendIfNotNull(" ")
            appendIfNotNull(streetNumber)
            appendIfNotNull(", ")
            appendIfNotNull(floor)
            appendIfNotNull(apartment)
            appendIfNotNull(", ")
            appendIfNotNull(postalCode)
            appendIfNotNull(" ")
            appendIfNotNull(city)
        }
    }

    companion object {
        fun fromDto(dto: AddressAutocompleteQuery.AutoCompleteAddress): DanishAddress {
            return DanishAddress(
                id = dto.id,
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

fun StringBuilder.appendIfNotNull(input: String?): StringBuilder {
    if (input != null) append(input)
    return this
}
