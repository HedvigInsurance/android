package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.InsuranceAgreement.DisplayItem
import com.hedvig.android.logger.logcat
import java.time.format.DateTimeFormatter
import kotlin.String
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

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
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as InsuranceContract

    logcat { "Stelios I1" }
    if (supportsAddressChange != other.supportsAddressChange) return false
    logcat { "Stelios I2" }
    if (supportsEditCoInsured != other.supportsEditCoInsured) return false
    logcat { "Stelios I3" }
    if (supportsTierChange != other.supportsTierChange) return false
    logcat { "Stelios I4" }
    if (isTerminated != other.isTerminated) return false
    logcat { "Stelios I5" }
    if (id != other.id) return false
    logcat { "Stelios I6" }
    if (displayName != other.displayName) return false
    logcat { "Stelios I7" }
    if (contractHolderDisplayName != other.contractHolderDisplayName) return false
    logcat { "Stelios I8" }
    if (contractHolderSSN != other.contractHolderSSN) return false
    logcat { "Stelios I9" }
    if (exposureDisplayName != other.exposureDisplayName) return false
    logcat { "Stelios I10" }
    if (inceptionDate != other.inceptionDate) return false
    logcat { "Stelios I11" }
    if (terminationDate != other.terminationDate) return false
    logcat { "Stelios I12" }
    if (currentInsuranceAgreement != other.currentInsuranceAgreement) return false
    logcat { "Stelios I13" }
    if (upcomingInsuranceAgreement != other.upcomingInsuranceAgreement) return false
    logcat { "Stelios I14" }
    if (renewalDate != other.renewalDate) return false
    logcat { "Stelios I15" }
    if (tierName != other.tierName) return false
    logcat { "Stelios I16" }

    return true
  }

  override fun hashCode(): Int {
    var result = supportsAddressChange.hashCode()
    result = 31 * result + supportsEditCoInsured.hashCode()
    result = 31 * result + supportsTierChange.hashCode()
    result = 31 * result + isTerminated.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + contractHolderDisplayName.hashCode()
    result = 31 * result + (contractHolderSSN?.hashCode() ?: 0)
    result = 31 * result + exposureDisplayName.hashCode()
    result = 31 * result + inceptionDate.hashCode()
    result = 31 * result + (terminationDate?.hashCode() ?: 0)
    result = 31 * result + currentInsuranceAgreement.hashCode()
    result = 31 * result + (upcomingInsuranceAgreement?.hashCode() ?: 0)
    result = 31 * result + (renewalDate?.hashCode() ?: 0)
    result = 31 * result + (tierName?.hashCode() ?: 0)
    return result
  }
}

data class Addon(
  val addonVariant: AddonVariant,
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as InsuranceAgreement

    logcat { "Stelios IA1" }
    if (activeFrom != other.activeFrom) return false
    logcat { "Stelios IA2" }
    if (activeTo != other.activeTo) return false
    logcat { "Stelios IA3" }
    if (displayItems != other.displayItems) return false
    logcat { "Stelios IA4" }
    if (productVariant != other.productVariant) return false
    logcat { "Stelios IA5: certificateUrl$certificateUrl | ${other.certificateUrl}" }
    if (certificateUrl != other.certificateUrl) return false
    logcat { "Stelios IA6" }
    if (coInsured != other.coInsured) return false
    logcat { "Stelios IA7" }
    if (creationCause != other.creationCause) return false
    logcat { "Stelios IA8" }
    if (addons != other.addons) return false
    logcat { "Stelios IA9" }

    return true
  }

  override fun hashCode(): Int {
    var result = activeFrom.hashCode()
    result = 31 * result + activeTo.hashCode()
    result = 31 * result + displayItems.hashCode()
    result = 31 * result + productVariant.hashCode()
    result = 31 * result + (certificateUrl?.hashCode() ?: 0)
    result = 31 * result + coInsured.hashCode()
    result = 31 * result + creationCause.hashCode()
    result = 31 * result + (addons?.hashCode() ?: 0)
    return result
  }
}
