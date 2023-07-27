package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

@Serializable
data class MoveQuote(
  val insuranceName: String,
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: UiMoney,
  val startDate: LocalDate,
  val termsVersion: String,
  val isExpanded: Boolean = false,
) {
  companion object {
    fun PreviewData(index: Int = 0): MoveQuote {
      @Suppress("NAME_SHADOWING")
      val index = index + 1
      return MoveQuote(
        insuranceName = "Insurance #$index",
        moveIntentId = MoveIntentId(""),
        address = Address(
          id = AddressId(""),
          apartmentNumber = "1$index",
          bbrId = null,
          city = null,
          floor = null,
          postalCode = "124$index",
          street = "Froedingsvaegen $index",
        ),
        numberCoInsured = index,
        premium = UiMoney(99.0 * index, CurrencyCode.SEK),
        startDate = LocalDate(2023, 5, 13),
        termsVersion = "$index",
        isExpanded = index == 1,
      )
    }
  }
}
