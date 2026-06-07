package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popBackstack
import com.hedvig.feature.remove.addons.ui.RemoveAddonFailureScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSuccessScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryDestination
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination

fun EntryProviderScope<HedvigNavKey>.removeAddonsEntries(backstack: Backstack) {
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
      )
    }
  }

  entry<ChooseAddonToRemoveKey> { key ->
    SelectAddonToRemoveDestination(
      contractId = key.insuranceId,
      preselectedAddonProduct = key.preselectedAddonVariant,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<RemoveAddonSummaryKey> { key ->
    RemoveAddonSummaryDestination(
      contractId = key.params.contractId,
      addonsToRemove = key.params.addonsToRemove,
      activationDate = key.params.activationDate,
      baseCost = key.params.baseCost,
      currentTotalCost = key.params.currentTotalCost,
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
