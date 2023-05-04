package com.feature.changeaddress.data

@JvmInline
value class AddressId(val id: String)

data class Address(
  val id: AddressId,
  val apartmentNumber: String? = null,
  val bbrId: String? = null,
  val city: String? = null,
  val floor: String? = null,
  val postalCode: String,
  val street: String,
)
