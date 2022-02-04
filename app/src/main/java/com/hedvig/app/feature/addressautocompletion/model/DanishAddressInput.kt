package com.hedvig.app.feature.addressautocompletion.model

data class DanishAddressInput(
    val rawText: String,
    val selectedDanishAddress: DanishAddress? = null,
) {
    val queryString: String
        get() = when (selectedDanishAddress) {
            null -> rawText
            else -> selectedDanishAddress.toQueryString()
        }

    fun withNewInput(newText: String): DanishAddressInput {
        if (selectedDanishAddress == null) return DanishAddressInput(newText)
        val oldText = rawText
        return DanishAddressInput(
            rawText = newText,
            // todo figure out when to properly clear the existing selected Address. Maybe clear when deleted a whitespace?
            selectedDanishAddress = if (newText.length < oldText.length) {
                null
            } else {
                selectedDanishAddress
            }
        )
    }

    fun withSelectedAddress(address: DanishAddress): DanishAddressInput {
        val newText = if (address.streetName != null && address.streetNumber != null) {
            "${address.streetName} ${address.streetNumber}"
        } else {
            address.address
        }
        return DanishAddressInput(
            rawText = newText,
            selectedDanishAddress = address,
        )
    }
}
