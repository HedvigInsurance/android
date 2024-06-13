package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

internal data class MoveQuote(
  val id: String,
  val insuranceName: String,
  val moveIntentId: MoveIntentId,
  val premium: UiMoney,
  val startDate: LocalDate,
  val productVariant: ProductVariant,
  val isExpanded: Boolean = false,
  val displayItems: List<Pair<String, String>>,
) {
  companion object {
    @Suppress("ktlint:standard:function-naming")
    fun PreviewData(index: Int = 0): MoveQuote {
      @Suppress("NAME_SHADOWING")
      val index = index + 1
      return MoveQuote(
        id = index.toString(),
        insuranceName = "Insurance #$index",
        moveIntentId = MoveIntentId(""),
        premium = UiMoney(99.0 * index, CurrencyCode.SEK),
        startDate = LocalDate(2023, 5, 13),
        isExpanded = index == 1,
        productVariant = ProductVariant(
          displayName = "Test",
          contractGroup = ContractGroup.RENTAL,
          contractType = ContractType.SE_APARTMENT_RENT,
          partner = "test",
          perils = listOf(),
          insurableLimits = listOf(
            InsurableLimit(
              label = "test",
              description = "long".repeat(10),
              limit = "long".repeat(10),
              type = InsurableLimit.InsurableLimitType.BIKE,
            ),
          ),
          documents = listOf(),
        ),
        displayItems = listOf(),
      )
    }
  }
}
