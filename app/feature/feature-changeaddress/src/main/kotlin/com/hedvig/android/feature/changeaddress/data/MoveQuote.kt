package com.hedvig.android.feature.changeaddress.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.fragment.ProductVariantFragment
import octopus.type.CurrencyCode

@Serializable
data class MoveQuote(
  val id: String,
  val insuranceName: String,
  val moveIntentId: MoveIntentId,
  val address: Address,
  val numberCoInsured: Int,
  val premium: UiMoney,
  val startDate: LocalDate,
  val productVariant: ProductVariantFragment,
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
        productVariant = object : ProductVariantFragment {
          override val displayName: String
            get() = "Test"
          override val typeOfContract: String
            get() = "testType"
          override val partner: String
            get() = "test"
          override val product: ProductVariantFragment.Product
            get() = object : ProductVariantFragment.Product {
              override val displayNameFull: String
                get() = ""
              override val pillowImage: ProductVariantFragment.Product.PillowImage
                get() = object : ProductVariantFragment.Product.PillowImage {
                  override val src: String
                    get() = "test"

                }
            }
          override val perils: List<ProductVariantFragment.Peril>
            get() = emptyList()
          override val insurableLimits: List<ProductVariantFragment.InsurableLimit>
            get() = emptyList()
          override val documents: List<ProductVariantFragment.Document>
            get() = emptyList()
          override val highlights: List<ProductVariantFragment.Highlight>
            get() = emptyList()
          override val faq: List<ProductVariantFragment.Faq>
            get() = emptyList()

        }
      )
    }
  }
}
