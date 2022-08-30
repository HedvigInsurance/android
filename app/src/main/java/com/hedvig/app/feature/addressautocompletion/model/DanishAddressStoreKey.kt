package com.hedvig.app.feature.addressautocompletion.model

@Suppress("EnumEntryName")
enum class DanishAddressStoreKey {
  fullAddress, // ktlint-disable enum-entry-name-case
  bbrId, // ktlint-disable enum-entry-name-case
  zipCode, // ktlint-disable enum-entry-name-case
  city, // ktlint-disable enum-entry-name-case
  streetName, // ktlint-disable enum-entry-name-case
  streetNumber, // ktlint-disable enum-entry-name-case
  street, // ktlint-disable enum-entry-name-case
  floor, // ktlint-disable enum-entry-name-case
  apartment, // ktlint-disable enum-entry-name-case
  ;

  companion object {
    fun clearDanishAddressRelatedStoreValues(
      putStoreValue: (key: String, value: String?) -> Unit,
    ) {
      values()
        .map(DanishAddressStoreKey::name)
        .forEach { key ->
          putStoreValue(key, null)
        }
    }
  }
}

fun DanishAddress.toValueStoreKeys(): Map<String, String?> {
  return mapOf(
    DanishAddressStoreKey.fullAddress to address,
    DanishAddressStoreKey.bbrId to id,
    DanishAddressStoreKey.zipCode to postalCode,
    DanishAddressStoreKey.city to city,
    DanishAddressStoreKey.streetName to streetName,
    DanishAddressStoreKey.streetNumber to streetNumber,
    DanishAddressStoreKey.street to "$streetName $streetNumber",
    DanishAddressStoreKey.floor to floor,
    DanishAddressStoreKey.apartment to apartment,
  ).mapKeys { entry ->
    entry.key.name
  }
}

fun DanishAddress.Companion.fromValueStoreKeys(
  getValueFromStore: (key: String) -> String?,
): DanishAddress? {
  val fullAddress = getValueFromStore(DanishAddressStoreKey.fullAddress.name) ?: return null
  return DanishAddress(
    address = fullAddress,
    id = getValueFromStore(DanishAddressStoreKey.bbrId.name),
    postalCode = getValueFromStore(DanishAddressStoreKey.zipCode.name),
    city = getValueFromStore(DanishAddressStoreKey.city.name),
    streetName = getValueFromStore(DanishAddressStoreKey.streetName.name),
    streetNumber = getValueFromStore(DanishAddressStoreKey.streetNumber.name),
    floor = getValueFromStore(DanishAddressStoreKey.floor.name),
    apartment = getValueFromStore(DanishAddressStoreKey.apartment.name),
  )
}
