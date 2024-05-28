package com.hedvig.android.feature.changeaddress.data

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
internal value class AddressId(val id: String)

@Serializable
internal data class Address(
  val id: AddressId,
  val apartmentNumber: String? = null,
  val bbrId: String? = null,
  val city: String? = null,
  val floor: String? = null,
  val postalCode: String,
  val street: String,
)

data class AddressInput(
  val apartmentNumber: String? = null,
  val bbrId: String? = null,
  val city: String? = null,
  val floor: String? = null,
  val postalCode: String,
  val street: String,
)
