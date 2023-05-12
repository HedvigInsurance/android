package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.ui.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

@Serializable
data class MoveQuote(
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: UiMoney,
  val startDate: LocalDate,
  val termsVersion: String,
  val isExpanded: Boolean = false,
)

@Serializable
data class Premium(
  val amount: Double,
  val currencyCode: CurrencyCode,
)
