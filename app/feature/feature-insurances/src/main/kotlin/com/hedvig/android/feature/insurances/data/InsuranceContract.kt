package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import java.time.format.DateTimeFormatter
import kotlin.String
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

sealed interface InsuranceContract {
  val id: String
  val tierName: String?
  val displayName: String
  val contractHolderDisplayName: String
  val contractHolderSSN: String?
  val exposureDisplayName: String
  val productVariant: ProductVariant
  val displayItems: List<DisplayItem>
  val coInsured: List<InsuranceAgreement.CoInsured>
  val supportsEditCoInsured: Boolean
  val supportsAddressChange: Boolean
  val supportsTierChange: Boolean
  val upcomingInsuranceAgreement: InsuranceAgreement?
  val isTerminated: Boolean
  val addons: List<Addon>?

  data class EstablishedInsuranceContract(
    override val id: String,
    override val displayName: String,
    override val contractHolderDisplayName: String,
    override val contractHolderSSN: String?,
    override val exposureDisplayName: String,
    val inceptionDate: LocalDate,
    val terminationDate: LocalDate?,
    val currentInsuranceAgreement: InsuranceAgreement,
    override val upcomingInsuranceAgreement: InsuranceAgreement?,
    val renewalDate: LocalDate?,
    override val supportsAddressChange: Boolean,
    override val supportsEditCoInsured: Boolean,
    override val supportsTierChange: Boolean,
    override val isTerminated: Boolean,
    override val tierName: String?,
  ) : InsuranceContract {
    override val productVariant: ProductVariant = currentInsuranceAgreement.productVariant
    override val displayItems: List<DisplayItem> = currentInsuranceAgreement.displayItems
    override val coInsured: List<InsuranceAgreement.CoInsured> = currentInsuranceAgreement.coInsured
    override val addons: List<Addon>? = currentInsuranceAgreement.addons
  }

  data class PendingInsuranceContract(
    override val id: String,
    override val tierName: String?,
    override val displayName: String,
    override val contractHolderDisplayName: String,
    override val contractHolderSSN: String?,
    override val exposureDisplayName: String,
    override val productVariant: ProductVariant,
    override val displayItems: List<DisplayItem>,
  ) : InsuranceContract {
    override val coInsured: List<InsuranceAgreement.CoInsured> = listOf()
    override val supportsEditCoInsured: Boolean = false
    override val supportsAddressChange: Boolean = false
    override val supportsTierChange: Boolean = false
    override val upcomingInsuranceAgreement: InsuranceAgreement? = null
    override val isTerminated: Boolean = false
    override val addons: List<Addon>? = null
  }
}

data class Addon(
  val addonVariant: AddonVariant,
)

data class MonthlyCost(
  val monthlyGross: UiMoney,
  val monthlyNet: UiMoney,
  val discounts: List<AgreementDiscount>,
)

data class AgreementDiscount(
  val displayName: String,
  val displayValue: String,
  val explanation: String,
  val campaignCode: String,
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
  val cost: MonthlyCost,
) {
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
