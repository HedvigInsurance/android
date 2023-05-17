package com.hedvig.app.feature.addressautocompletion.model

class DanishAddressInput private constructor(
  val rawText: String,
  val selectedAddress: DanishAddress? = null,
) {
  val isEmptyInput: Boolean
    get() = selectedAddress == null && rawText.isEmpty()

  fun withNewText(newText: String): DanishAddressInput {
    return DanishAddressInput(newText)
  }

  fun withSelectedAddress(address: DanishAddress): DanishAddressInput {
    return DanishAddressInput(
      rawText = address.toPresentableTextPair().first,
      selectedAddress = address,
    )
  }

  companion object {
    fun fromDanishAddress(address: DanishAddress?): DanishAddressInput {
      return DanishAddressInput(
        rawText = address?.toPresentableTextPair()?.first ?: "",
        selectedAddress = address,
      )
    }
  }
}
