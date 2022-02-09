package com.hedvig.app.feature.addressautocompletion.model

@Suppress("EnumEntryName")
enum class DanishAddressStoreKey {
    bbrId,
    fullAddress,
    street,
    streetName,
    streetNumber,
    zipCode,
    city,
    floor,
    apartment,
    addressSearchTerm,
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
    @Suppress("EnumEntryName")
    return mapOf(
        DanishAddressStoreKey.bbrId to id,
        DanishAddressStoreKey.fullAddress to address,
        DanishAddressStoreKey.street to "$streetName $streetName",
        DanishAddressStoreKey.streetName to streetName,
        DanishAddressStoreKey.streetNumber to streetNumber,
        DanishAddressStoreKey.floor to floor,
        DanishAddressStoreKey.apartment to apartment,
        DanishAddressStoreKey.zipCode to postalCode,
        DanishAddressStoreKey.city to city,
        DanishAddressStoreKey.addressSearchTerm to toQueryString(), // todo why do we need this
    ).mapKeys { entry ->
        entry.key.name
    }
}

fun DanishAddress.Companion.fromValueStoreKeys(
    getValueFromStore: (key: String) -> String?,
): DanishAddress? {
    val fullAddress = getValueFromStore(DanishAddressStoreKey.fullAddress.name) ?: return null
    return DanishAddress(
        id = getValueFromStore(DanishAddressStoreKey.bbrId.name),
        address = fullAddress,
        postalCode = getValueFromStore(DanishAddressStoreKey.zipCode.name),
        city = getValueFromStore(DanishAddressStoreKey.city.name),
        streetName = getValueFromStore(DanishAddressStoreKey.streetName.name),
        streetNumber = getValueFromStore(DanishAddressStoreKey.streetNumber.name),
        floor = getValueFromStore(DanishAddressStoreKey.floor.name),
        apartment = getValueFromStore(DanishAddressStoreKey.apartment.name),
    )
}
