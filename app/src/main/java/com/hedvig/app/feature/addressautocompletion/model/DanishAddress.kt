package com.hedvig.app.feature.addressautocompletion.model

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import kotlinx.parcelize.Parcelize

@Parcelize
data class DanishAddress(
    val id: String?,
    val address: String,
    val streetName: String?,
    val streetNumber: String?,
    val floor: String?,
    val apartment: String?,
    val postalCode: String?,
    val city: String?,
) : Parcelable {

    val isValidFinalSelection: Boolean
        get() = streetName != null &&
            streetNumber != null &&
            postalCode != null &&
            city != null

    val onlyContainsAddress: Boolean
        get() = id == null &&
            streetName == null &&
            streetNumber == null &&
            floor == null &&
            apartment == null &&
            postalCode == null &&
            city == null

    fun toPresentableTextPair(): Pair<String, String?> {
        if (onlyContainsAddress) return address to null

        val topString = buildString {
            append("$streetName $streetNumber")
            append(
                when {
                    floor != null && apartment != null -> ", $floor. $apartment"
                    floor != null -> ", $floor."
                    apartment != null -> ", $apartment"
                    else -> ""
                }
            )
        }
        val bottomString = "$postalCode $city"
        return topString to bottomString
    }

    fun toQueryString(): String {
        if (onlyContainsAddress) return address
        return buildString {
            appendIfNotNull(streetName)
            appendIfNotNull(" ")
            appendIfNotNull(streetNumber)
            appendIfNotNull(", ")
            appendIfNotNull(floor)
            appendIfNotNull(". ")
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

private fun StringBuilder.appendIfNotNull(input: String?): StringBuilder {
    if (input != null) append(input)
    return this
}
