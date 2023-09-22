package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.insurance.Product
import com.hedvig.android.core.insurance.ProductVariant
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

data class MoveQuote(
  val id: String,
  val insuranceName: String,
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: UiMoney,
  val startDate: LocalDate,
  val productVariant: ProductVariant,
  val isExpanded: Boolean = false,
) {
  companion object {
    fun PreviewData(index: Int = 0): MoveQuote {
      @Suppress("NAME_SHADOWING")
      val index = index + 1
      return MoveQuote(
        id = index.toString(),
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
        isExpanded = index == 1,
        productVariant = ProductVariant(
          displayName = "Test",
          typeOfContract = "testTpe",
          partner = "test",
          product = Product(
            displayNameFull = "Test",
            pillowImageUrl = "",
          ),
          perils = emptyList(),
          insurableLimits = emptyList(),
          documents = emptyList(),
        )
      )
    }
  }
}
