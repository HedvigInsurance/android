package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.data.productvariant.toDocumentType
import com.hedvig.android.feature.insurances.data.InsuranceAgreement.DisplayItem
import java.time.format.DateTimeFormatter
import kotlin.String
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.fragment.AddonVariantFragment

data class InsuranceContract(
  val id: String,
  val displayName: String,
  val contractHolderDisplayName: String,
  val contractHolderSSN: String?,
  val exposureDisplayName: String,
  val inceptionDate: LocalDate,
  val terminationDate: LocalDate?,
  val currentInsuranceAgreement: InsuranceAgreement,
  val upcomingInsuranceAgreement: InsuranceAgreement?,
  val renewalDate: LocalDate?,
  val supportsAddressChange: Boolean,
  val supportsEditCoInsured: Boolean,
  val supportsTierChange: Boolean,
  val isTerminated: Boolean,
  val tierName: String?,
)

data class Addon(
  val addonVariant: AddonVariant,
)

data class AddonVariant(
  val termsVersion: String,
  val displayName: String,
  val product: String,
  val documents: List<InsuranceVariantDocument>,
  val perils: List<ProductVariantPeril>,
  val insurableLimits: List<InsurableLimit>,
)

fun AddonVariantFragment.toAddonVariant() = AddonVariant(
  termsVersion = this.termsVersion,
  displayName = this.displayName,
  product = this.product,
  documents = this.documents.map {
    InsuranceVariantDocument(
      displayName = it.displayName,
      url = it.url,
      type = it.type.toDocumentType(),
    )
  },
  perils = this.perils.map { peril ->
    ProductVariantPeril(
      id = peril.id,
      title = peril.title,
      description = peril.description,
      covered = peril.covered,
      exceptions = peril.exceptions,
      colorCode = peril.colorCode,
    )
  },
  insurableLimits = this.insurableLimits.map { insurableLimit ->
    InsurableLimit(
      label = insurableLimit.label,
      limit = insurableLimit.limit,
      description = insurableLimit.description,
    )
  },
)

data class InsuranceAgreement(
  val activeFrom: LocalDate,
  val activeTo: LocalDate,
  val displayItems: List<DisplayItem>,
  val productVariant: ProductVariant,
  val certificateUrl: String?,
  val coInsured: List<CoInsured>,
  val creationCause: CreationCause,
  val addons: List<Addon>?,
) {
  data class DisplayItem(
    val title: String,
    val value: String,
  )

  data class CoInsured(
    private val ssn: String?,
    private val birthDate: LocalDate?,
    private val firstName: String?,
    private val lastName: String?,
    val activatesOn: LocalDate?,
    val terminatesOn: LocalDate?,
    val hasMissingInfo: Boolean,
  ) {
    fun getDisplayName() = formatName(firstName, lastName)

    fun getSsnOrBirthDate(dateTimeFormatter: DateTimeFormatter) = if (ssn != null) {
      formatSsn(ssn)
    } else {
      birthDate
        ?.toJavaLocalDate()
        ?.let { dateTimeFormatter.format(it) }
    }
  }

  enum class CreationCause {
    UNKNOWN,
    NEW_CONTRACT,
    RENEWAL,
    MIDTERM_CHANGE,
  }
}
