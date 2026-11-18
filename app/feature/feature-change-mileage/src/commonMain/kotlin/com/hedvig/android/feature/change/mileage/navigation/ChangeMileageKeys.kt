package com.hedvig.android.feature.change.mileage.navigation

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.change.mileage.data.MileageQuote
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Starts the flow for a known contract, landing on the screen where the new mileage is picked. */
@Serializable
data class ChangeMileageKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.changeMileageWithContractId] */
  val contractId: String,
) : HedvigNavKey

/** Starts the flow without a contract, letting the member pick which of their eligible contracts to change. */
@Serializable
data object ChangeMileageChooseInsuranceKey : HedvigNavKey

@Serializable
internal data class ChangeMileageSummaryKey(val params: ChangeMileageSummaryParameters) : HedvigNavKey

@Serializable
internal data class ChangeMileageSuccessKey(
  val newMileageDisplayName: String,
  val newMonthlyCost: UiMoney,
) : HedvigNavKey

@Serializable
internal data object ChangeMileageFailureKey : HedvigNavKey

@Serializable
internal data class ChangeMileageSummaryParameters(
  val contractDisplayName: String,
  val contractGroup: ContractGroup,
  val activationDate: LocalDate,
  val currentMonthlyCost: UiMoney,
  val quote: MileageQuote,
)
