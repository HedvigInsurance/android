package com.feature.changeaddress

import ApartmentOwnerType
import com.feature.changeaddress.data.AddressId
import com.feature.changeaddress.data.MoveIntentId
import com.feature.changeaddress.data.MoveQuote
import com.feature.changeaddress.data.MoveResult
import kotlinx.datetime.LocalDate

data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val street: String? = null,
  val postalCode: String? = null,
  val squareMeters: String? = null,
  val moveRange: ClosedRange<LocalDate>? = null,
  val movingDate: LocalDate? = null,
  val numberCoInsured: Int? = null,
  val apartmentOwnerType: ApartmentOwnerType? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
  val moveFromAddressId: AddressId? = null,
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: MoveResult? = null,
)
