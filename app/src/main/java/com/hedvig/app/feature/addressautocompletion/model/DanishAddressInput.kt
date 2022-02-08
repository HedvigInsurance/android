package com.hedvig.app.feature.addressautocompletion.model

class DanishAddressInput constructor(
    val rawText: String,
    val selectedDanishAddress: DanishAddress? = null,
) {
    val queryDanishAddress: DanishAddress
        get() = selectedDanishAddress ?: DanishAddress(
            id = null,
            address = rawText,
            streetName = null,
            streetNumber = null,
            floor = null,
            apartment = null,
            postalCode = null,
            city = null,
        )

    fun withNewText(newText: String): DanishAddressInput {
        if (selectedDanishAddress == null) return DanishAddressInput(newText)
        val oldText = rawText
        return DanishAddressInput(
            rawText = newText,
            selectedDanishAddress = if (newText.length < oldText.length) {
                null
            } else {
                selectedDanishAddress
            }
        )
    }

    fun withSelectedAddress(address: DanishAddress): DanishAddressInput {
        return DanishAddressInput(
            rawText = address.toPresentableTextPair().first,
            selectedDanishAddress = address,
        )
    }

    companion object {
        fun fromDanishAddress(danishAddress: DanishAddress?): DanishAddressInput {
            return DanishAddressInput(
                rawText = danishAddress?.toPresentableTextPair()?.first ?: "",
                selectedDanishAddress = danishAddress,
            )
        }
    }
}
