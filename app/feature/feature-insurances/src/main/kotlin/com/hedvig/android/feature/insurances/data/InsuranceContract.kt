package com.hedvig.android.feature.insurances.data

import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val exposureDisplayName: String,
  val inceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val currentInsuranceAgreement: InsuranceAgreement,
  val upcomingInsuranceAgreement: InsuranceAgreement?,
  val renewalDate: LocalDate?,
  val supportsAddressChange: Boolean,
  val isTerminated: Boolean,
)

data class InsuranceAgreement(
  val activeFrom: LocalDate,
  val activeTo: LocalDate,
  val displayItems: List<DisplayItem>,
  val productVariant: ProductVariant,
  val certificateUrl: String?,
) {
  data class DisplayItem(
    val title: String,
    val value: String,
  )
}
