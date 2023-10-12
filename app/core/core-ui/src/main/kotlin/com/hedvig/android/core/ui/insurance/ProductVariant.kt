package com.hedvig.android.core.ui.insurance

import androidx.compose.runtime.saveable.listSaver
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import octopus.fragment.ProductVariantFragment
import octopus.type.InsurableLimitType
import octopus.type.InsuranceDocumentType

data class ProductVariant(
  val displayName: String,
  val contractType: ContractType,
  val partner: String?,
  val perils: ImmutableList<Peril>,
  val insurableLimits: ImmutableList<InsurableLimit>,
  val documents: ImmutableList<Document>,
)

data class Peril(
  val id: String,
  val title: String,
  val description: String,
  val info: String,
  val covered: List<String>,
  val exceptions: List<String>,
  val colorCode: String?,
)

data class InsurableLimit(
  val label: String,
  val limit: String,
  val description: String,
  val type: InsurableLimitType,
) {
  enum class InsurableLimitType {
    DEDUCTIBLE,
    DEDUCTIBLE_NATURE_DAMAGE,
    DEDUCTIBLE_ALL_RISK,
    INSURED_AMOUNT,
    GOODS_INDIVIDUAL,
    GOODS_FAMILY,
    TRAVEL_DAYS,
    MEDICAL_EXPENSES,
    LOST_LUGGAGE,
    BIKE,
    PERMANENT_INJURY,
    TREATMENT,
    DENTAL_TREATMENT,
    TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME,
    TRAVEL_DELAYED_ON_TRIP,
    TRAVEL_DELAYED_LUGGAGE,
    TRAVEL_CANCELLATION,
    UNKNOWN,
  }

  companion object {
    val Saver = listSaver<InsurableLimit?, String>(
      save = {
        if (it == null) {
          emptyList()
        } else {
          listOf(
            it.label,
            it.limit,
            it.description,
            it.type.name,
          )
        }
      },
      restore = {
        if (it.size != 3) {
          null
        } else {
          InsurableLimit(
            label = it[0],
            limit = it[1],
            description = it[2],
            type = InsurableLimitType.valueOf(it[3]),
          )
        }
      },
    )
  }
}

data class Document(
  val displayName: String,
  val url: String,
  val type: InsuranceDocumentType,
) {
  enum class InsuranceDocumentType {
    TERMS_AND_CONDITIONS,
    PRE_SALE_INFO_EU_STANDARD,
    PRE_SALE_INFO,
    GENERAL_TERMS,
    PRIVACY_POLICY,
    CERTIFICATE,
    UNKNOWN__,
    ;

    fun getStringRes() = when (this) {
      TERMS_AND_CONDITIONS -> R.string.MY_DOCUMENTS_INSURANCE_TERMS
      PRE_SALE_INFO_EU_STANDARD -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
      PRE_SALE_INFO -> R.string.MY_DOUMENTS_INSURANCE_PREPURCHASE
      GENERAL_TERMS -> R.string.MY_DOCUMENTS_GENERAL_TERMS
      PRIVACY_POLICY -> R.string.MY_DOCUMENTS_PRIVACY_POLICY
      CERTIFICATE -> R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE
      UNKNOWN__ -> null
    }
  }
}

fun ProductVariantFragment.toProductVariant() = ProductVariant(
  displayName = this.displayName,
  contractType = this.typeOfContract.toContractType(),
  partner = this.partner,
  perils = this.perils.map { peril ->
    Peril(
      id = peril.id,
      title = peril.title,
      description = peril.description,
      info = peril.info,
      covered = peril.covered,
      exceptions = peril.exceptions,
      colorCode = peril.colorCode,
    )
  }.toImmutableList(),
  insurableLimits = this.insurableLimits.map { insurableLimit ->
    InsurableLimit(
      label = insurableLimit.label,
      limit = insurableLimit.limit,
      description = insurableLimit.description,
      type = when (insurableLimit.type) {
        InsurableLimitType.DEDUCTIBLE -> InsurableLimit.InsurableLimitType.DEDUCTIBLE
        InsurableLimitType.DEDUCTIBLE_NATURE_DAMAGE -> InsurableLimit.InsurableLimitType.DEDUCTIBLE_NATURE_DAMAGE
        InsurableLimitType.DEDUCTIBLE_ALL_RISK -> InsurableLimit.InsurableLimitType.DEDUCTIBLE_ALL_RISK
        InsurableLimitType.INSURED_AMOUNT -> InsurableLimit.InsurableLimitType.INSURED_AMOUNT
        InsurableLimitType.GOODS_INDIVIDUAL -> InsurableLimit.InsurableLimitType.GOODS_INDIVIDUAL
        InsurableLimitType.GOODS_FAMILY -> InsurableLimit.InsurableLimitType.GOODS_FAMILY
        InsurableLimitType.TRAVEL_DAYS -> InsurableLimit.InsurableLimitType.TRAVEL_DAYS
        InsurableLimitType.MEDICAL_EXPENSES -> InsurableLimit.InsurableLimitType.MEDICAL_EXPENSES
        InsurableLimitType.LOST_LUGGAGE -> InsurableLimit.InsurableLimitType.LOST_LUGGAGE
        InsurableLimitType.BIKE -> InsurableLimit.InsurableLimitType.BIKE
        InsurableLimitType.PERMANENT_INJURY -> InsurableLimit.InsurableLimitType.PERMANENT_INJURY
        InsurableLimitType.TREATMENT -> InsurableLimit.InsurableLimitType.TREATMENT
        InsurableLimitType.DENTAL_TREATMENT -> InsurableLimit.InsurableLimitType.DENTAL_TREATMENT
        InsurableLimitType.TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME ->
          InsurableLimit.InsurableLimitType.TRAVEL_ILLNESS_INJURY_TRANSPORTATION_HOME
        InsurableLimitType.TRAVEL_DELAYED_ON_TRIP -> InsurableLimit.InsurableLimitType.TRAVEL_DELAYED_ON_TRIP
        InsurableLimitType.TRAVEL_DELAYED_LUGGAGE -> InsurableLimit.InsurableLimitType.TRAVEL_DELAYED_LUGGAGE
        InsurableLimitType.TRAVEL_CANCELLATION -> InsurableLimit.InsurableLimitType.TRAVEL_CANCELLATION
        InsurableLimitType.UNKNOWN__ -> InsurableLimit.InsurableLimitType.UNKNOWN
      },
    )
  }.toImmutableList(),
  documents = this.documents.map { document ->
    Document(
      displayName = document.displayName,
      url = document.url,
      type = when (document.type) {
        InsuranceDocumentType.TERMS_AND_CONDITIONS -> Document.InsuranceDocumentType.TERMS_AND_CONDITIONS
        InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD -> Document.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD
        InsuranceDocumentType.PRE_SALE_INFO -> Document.InsuranceDocumentType.PRE_SALE_INFO
        InsuranceDocumentType.GENERAL_TERMS -> Document.InsuranceDocumentType.GENERAL_TERMS
        InsuranceDocumentType.PRIVACY_POLICY -> Document.InsuranceDocumentType.PRIVACY_POLICY
        InsuranceDocumentType.UNKNOWN__ -> Document.InsuranceDocumentType.UNKNOWN__
      },
    )
  }.toImmutableList(),
)
