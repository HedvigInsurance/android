package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.ui.insurance.ProductVariant
import kotlinx.datetime.LocalDate

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val exposureDisplayName: String,
  val inceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val currentAgreement: Agreement,
  val upcomingAgreement: Agreement?,
  val renewalDate: LocalDate?,
  val supportsAddressChange: Boolean,
  val isTerminated: Boolean,
)

data class Agreement(
  val activeFrom: LocalDate,
  val activeTo: LocalDate,
  val displayItems: List<DisplayItem>,
  val productVariant: ProductVariant,
) {
  data class DisplayItem(
    val title: String,
    val value: String,
  )
}
