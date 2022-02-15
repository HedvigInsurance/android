package com.hedvig.app.feature.addressautocompletion.model

class DanishAddressInput private constructor(
    val rawText: String,
    val selectedDanishAddress: DanishAddress? = null,
) {
    val isEmptyInput: Boolean
        get() = selectedDanishAddress == null && rawText.isEmpty()

    fun withNewText(newText: String): DanishAddressInput {
        return DanishAddressInput(newText)
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
