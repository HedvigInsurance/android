package com.hedvig.app.feature.addressautocompletion.model

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.AddressAutocompleteQuery
import kotlinx.parcelize.Parcelize

@Parcelize
data class DanishAddress(
    val address: String,
    val id: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val streetName: String? = null,
    val streetNumber: String? = null,
    val floor: String? = null,
    val apartment: String? = null,
) : Parcelable {

    private val onlyContainsAddress: Boolean
        get() = id == null &&
            streetName == null &&
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

    val hasAllProperties: Boolean
        get() = isValidFinalSelection &&
            id != null &&
            floor != null &&
            apartment != null

    /**
     * Checks for equality of the address itself, despite type differences like the [address] formatting or its [id]
     */
    fun isSameAddressAs(other: DanishAddress): Boolean {
        return postalCode == other.postalCode &&
            city == other.city &&
            streetName == other.streetName &&
            streetNumber == other.streetNumber &&
            floor == other.floor &&
            apartment == other.apartment
    }

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
                address = dto.address,
                id = dto.id,
                postalCode = dto.postalCode,
                city = dto.city,
                streetName = dto.streetName,
                streetNumber = dto.streetNumber,
                floor = dto.floor,
                apartment = dto.apartment,
            )
        }
    }
}

private fun StringBuilder.appendIfNotNull(input: String?): StringBuilder {
    if (input != null) append(input)
    return this
}
