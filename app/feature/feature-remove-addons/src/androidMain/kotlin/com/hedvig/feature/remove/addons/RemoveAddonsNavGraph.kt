package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popBackstack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.feature.remove.addons.ui.RemoveAddonFailureScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSuccessScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryDestination
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination
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

fun EntryProviderScope<HedvigNavKey>.removeAddonsNavGraph(backstack: Backstack) {
  // Flow anchor / insurance picker. When seeded with an insuranceId it jumps straight to the addon
  // picker (popping itself, so back leaves the flow); otherwise the member picks an insurance.
  entry<RemoveAddonsKey> { key ->
    val insuranceId = key.insuranceId
    val preselectedAddonVariant = key.preselectedAddonVariant
    if (insuranceId != null) {
      LaunchedEffect(Unit) {
        backstack.navigateAndPopUpTo<RemoveAddonsKey>(
          ChooseAddonToRemoveKey(
            insuranceId = insuranceId,
            preselectedAddonVariant = preselectedAddonVariant,
          ),
          inclusive = true,
        )
      }
    } else {
      SelectInsuranceToRemoveAddonDestination(
        navigateUp = backstack::navigateUp,
        navigateToChooseAddon = { contractId ->
          backstack.add(
            ChooseAddonToRemoveKey(
              insuranceId = contractId,
              preselectedAddonVariant = null,
            ),
          )
        },
      )
    }
  }

  entry<ChooseAddonToRemoveKey> { key ->
    SelectAddonToRemoveDestination(
      contractId = key.insuranceId,
      preselectedAddonProduct = key.preselectedAddonVariant,
      navigateUp = backstack::navigateUp,
      navigateToSummary = {
        contractId: ContractId,
        addons: List<CurrentlyActiveAddon>,
        activationDate: LocalDate,
        baseCost: ItemCost,
        currentCost: ItemCost,
        productVariant: ProductVariant,
        allAddons: List<CurrentlyActiveAddon>,
        popDestination: Boolean,
        ->
        val summary = RemoveAddonSummaryKey(
          params = SummaryParameters(
            contractId = contractId,
            addonsToRemove = addons,
            activationDate = activationDate,
            baseCost = baseCost,
            currentTotalCost = currentCost,
            productVariant = productVariant,
            existingAddons = allAddons,
          ),
        )
        if (popDestination) {
          backstack.navigateAndPopUpTo<ChooseAddonToRemoveKey>(summary, inclusive = true)
        } else {
          backstack.add(summary)
        }
      },
    )
  }

  entry<RemoveAddonSummaryKey> { key ->
    RemoveAddonSummaryDestination(
      navigateToSuccess = {
        backstack.navigateExitingRemoveAddonFlow(
          RemoveAddonSubmitSuccessKey(key.params.activationDate),
        )
      },
      contractId = key.params.contractId,
      addonsToRemove = key.params.addonsToRemove,
      activationDate = key.params.activationDate,
      baseCost = key.params.baseCost,
      currentTotalCost = key.params.currentTotalCost,
      onFailure = {
        backstack.add(RemoveAddonSubmitFailureKey)
      },
      onCloseFlow = {
        backstack.popUpToRemoveAddonAnchor(inclusive = true)
      },
      navigateUp = backstack::navigateUp,
      existingAddonsToRemove = key.params.existingAddons,
      productVariant = key.params.productVariant,
    )
  }

  entry<RemoveAddonSubmitFailureKey> {
    RemoveAddonFailureScreen(
      popBackstack = backstack::popBackstack,
    )
  }

  entry<RemoveAddonSubmitSuccessKey> { key ->
    RemoveAddonSuccessScreen(
      key.activationDate,
      popBackstack = backstack::popBackstack,
    )
  }
}

/**
 * The flow's exit anchor: [RemoveAddonsKey] when the insurance picker was shown, or
 * [ChooseAddonToRemoveKey] when the flow was seeded with an insuranceId (the anchor having popped
 * itself during the jump to the addon picker).
 */
private fun Backstack.popUpToRemoveAddonAnchor(inclusive: Boolean) {
  if (findLastOrNull<RemoveAddonsKey>() != null) {
    popUpTo<RemoveAddonsKey>(inclusive)
  } else {
    popUpTo<ChooseAddonToRemoveKey>(inclusive)
  }
}

private fun Backstack.navigateExitingRemoveAddonFlow(destination: HedvigNavKey) {
  popUpToRemoveAddonAnchor(inclusive = true)
  add(destination)
}
