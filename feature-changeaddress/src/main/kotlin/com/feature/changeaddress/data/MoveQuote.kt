package com.feature.changeaddress.data

import java.text.DecimalFormat
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

@Serializable
data class MoveQuote(
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: Premium,
  val startDate: LocalDate,
  val termsVersion: String,
)

@Serializable
data class Premium(
  val amount: Double,
  val currencyCode: CurrencyCode,
)

fun Premium.toDisplayString(): String {
  return StringBuilder()
    .append(DecimalFormat("0.#").format(amount))
    .append(" ")
    .append(
      when (currencyCode) {
        CurrencyCode.SEK -> " kr"
        CurrencyCode.DKK -> " DKK"
        CurrencyCode.NOK -> " NOK"
        CurrencyCode.UNKNOWN__ -> throw IllegalArgumentException("Unknown currency code")
      },
    )
    .toString()
}
