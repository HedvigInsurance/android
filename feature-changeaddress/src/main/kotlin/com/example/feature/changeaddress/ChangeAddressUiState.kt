package com.example.feature.changeaddress

import com.example.feature.changeaddress.data.MoveIntentId
import com.example.feature.changeaddress.data.MoveQuote
import kotlinx.datetime.LocalDate

data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val moveRange: ClosedRange<LocalDate>? = null,
  val numberCoInsured: Int? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
  val quotes: List<MoveQuote> = emptyList()
)
