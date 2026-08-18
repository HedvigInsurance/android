package com.hedvig.feature.remove.addons

import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class SummaryParameters(
  val contractId: ContractId,
  val addonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
  val productVariant: ProductVariant,
  val existingAddons: List<CurrentlyActiveAddon>,
)

@Serializable
data class RemoveAddonsKey(
  val insuranceId: ContractId?,
  val preselectedAddonVariant: AddonVariant?,
) : HedvigNavKey

@Serializable
internal data class ChooseAddonToRemoveKey(
  val insuranceId: ContractId,
  val preselectedAddonVariant: AddonVariant?,
) : HedvigNavKey

@Serializable
internal data class RemoveAddonSummaryKey(
  val params: SummaryParameters,
) : HedvigNavKey

@Serializable
internal data class RemoveAddonSubmitSuccessKey(val activationDate: LocalDate) : HedvigNavKey

@Serializable
internal data object RemoveAddonSubmitFailureKey : HedvigNavKey

/**
 * The flow's exit anchor: [RemoveAddonsKey] when the insurance picker was shown, or
 * [ChooseAddonToRemoveKey] when the flow was seeded with an insuranceId (the anchor having popped
 * itself during the jump to the addon picker).
 */
internal fun Backstack.popUpToRemoveAddonAnchor(inclusive: Boolean) {
  if (findLastOrNull<RemoveAddonsKey>() != null) {
    popUpTo<RemoveAddonsKey>(inclusive)
  } else {
    popUpTo<ChooseAddonToRemoveKey>(inclusive)
  }
}

internal fun Backstack.navigateExitingRemoveAddonFlow(destination: HedvigNavKey) {
  popUpToRemoveAddonAnchor(inclusive = true)
  add(destination)
}
