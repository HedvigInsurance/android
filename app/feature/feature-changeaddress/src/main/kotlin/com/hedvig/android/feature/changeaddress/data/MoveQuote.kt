package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.android.core.ui.insurance.InsurableLimit
import com.hedvig.android.core.ui.insurance.ProductVariant
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

data class MoveQuote(
  val id: String,
  val insuranceName: String,
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberInsured: Int?,
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
        numberInsured = index,
        premium = UiMoney(99.0 * index, CurrencyCode.SEK),
        startDate = LocalDate(2023, 5, 13),
        isExpanded = index == 1,
        productVariant = ProductVariant(
          displayName = "Test",
          contractType = ContractType.RENTAL,
          partner = "test",
          perils = persistentListOf(),
          insurableLimits = persistentListOf(
            InsurableLimit(
              label = "test",
              description = "long".repeat(10),
              limit = "long".repeat(10),
              type = InsurableLimit.InsurableLimitType.BIKE,
            ),
          ),
          documents = persistentListOf(),
        ),
      )
    }
  }
}
