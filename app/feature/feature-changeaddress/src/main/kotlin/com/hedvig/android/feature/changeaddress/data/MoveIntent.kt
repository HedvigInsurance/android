package com.hedvig.android.feature.changeaddress.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class MoveIntentId(val id: String)

data class MoveIntent(
  val id: MoveIntentId,
  val currentHomeAddresses: List<Address>,
  val movingDateRange: ClosedRange<LocalDate>,
  val suggestedNumberInsured: Int,
  val isApartmentAvailableforStudent: Boolean?,
  val maxApartmentNumberCoInsured: Int?,
  val maxHouseNumberCoInsured: Int?,
  val maxApartmentSquareMeters: Int?,
  val maxHouseSquareMeters: Int?,
  val extraBuildingTypes: List<ExtraBuildingType>,
)
