package com.hedvig.android.feature.insurances.ui

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.CrossSell
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

internal data object InsurancePlaceholderProvider {
  fun providePlaceholderCrossSells(): PersistentList<CrossSell> {
    return persistentListOf(placeHolderCrossSell)
  }

  fun providePlaceholderInsuranceList(): PersistentList<InsuranceContract> {
    return persistentListOf(placeholderInsurance)
  }

  fun providePlaceholderInsurance(): InsuranceContract {
    return placeholderInsurance
  }

  private val placeHolderCrossSell = CrossSell(
    id = "1",
    title = "Home",
    subtitle = "Unlimited home insurance",
    storeUrl = "",
    type = CrossSell.CrossSellType.HOME,
  )

  private val placeholderInsurance = InsuranceContract(
    "1",
    "",
    exposureDisplayName = "",
    inceptionDate = LocalDate.fromEpochDays(200),
    terminationDate = LocalDate.fromEpochDays(400),
    currentInsuranceAgreement = InsuranceAgreement(
      activeFrom = LocalDate.fromEpochDays(240),
      activeTo = LocalDate.fromEpochDays(340),
      displayItems = persistentListOf(),
      productVariant = ProductVariant(
        displayName = "",
        contractGroup = ContractGroup.RENTAL,
        contractType = ContractType.SE_APARTMENT_RENT,
        partner = null,
        perils = persistentListOf(),
        insurableLimits = persistentListOf(),
        documents = persistentListOf(),
      ),
      certificateUrl = null,
      coInsured = persistentListOf(),
      creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
    ),
    upcomingInsuranceAgreement = null,
    renewalDate = LocalDate.fromEpochDays(500),
    supportsAddressChange = false,
    supportsEditCoInsured = true,
    isTerminated = false,
    contractHolderDisplayName = "",
    contractHolderSSN = "",
  )
}
