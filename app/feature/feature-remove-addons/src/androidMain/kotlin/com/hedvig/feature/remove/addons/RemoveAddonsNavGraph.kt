package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.feature.remove.addons.ui.RemoveAddonFailureScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSuccessScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryDestination
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination
import kotlin.reflect.KType
import kotlin.reflect.typeOf
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
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<ContractId?>(),
      typeOf<AddonVariant?>(),
    )
  }
}

@Serializable
internal data class ChooseAddonToRemoveKey(
  val insuranceId: ContractId,
  val preselectedAddonVariant: AddonVariant?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<ContractId>(),
      typeOf<AddonVariant?>(),
    )
  }
}

@Serializable
internal data class RemoveAddonSummaryKey(
  val params: SummaryParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
  }
}

@Serializable
internal data class RemoveAddonSubmitSuccessKey(val activationDate: LocalDate) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<LocalDate>(),
    )
  }
}

@Serializable
internal data object RemoveAddonSubmitFailureKey : HedvigNavKey

fun EntryProviderScope<HedvigNavKey>.removeAddonsNavGraph(backStack: MutableList<HedvigNavKey>) {
  // Flow anchor / insurance picker. When seeded with an insuranceId it jumps straight to the addon
  // picker (popping itself, so back leaves the flow); otherwise the member picks an insurance.
  navdestination<RemoveAddonsKey> {
    val insuranceId = this.insuranceId
    val preselectedAddonVariant = this.preselectedAddonVariant
    if (insuranceId != null) {
      LaunchedEffect(Unit) {
        backStack.navigateAndPopUpTo<RemoveAddonsKey>(
          ChooseAddonToRemoveKey(
            insuranceId = insuranceId,
            preselectedAddonVariant = preselectedAddonVariant,
          ),
          inclusive = true,
        )
      }
    } else {
      SelectInsuranceToRemoveAddonDestination(
        navigateUp = backStack::navigateUp,
        navigateToChooseAddon = { contractId ->
          backStack.add(
            ChooseAddonToRemoveKey(
              insuranceId = contractId,
              preselectedAddonVariant = null,
            ),
          )
        },
      )
    }
  }

  navdestination<ChooseAddonToRemoveKey> {
    SelectAddonToRemoveDestination(
      contractId = this.insuranceId,
      preselectedAddonProduct = this.preselectedAddonVariant,
      navigateUp = backStack::navigateUp,
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
          backStack.navigateAndPopUpTo<ChooseAddonToRemoveKey>(summary, inclusive = true)
        } else {
          backStack.add(summary)
        }
      },
    )
  }

  navdestination<RemoveAddonSummaryKey> {
    RemoveAddonSummaryDestination(
      navigateToSuccess = {
        backStack.navigateExitingRemoveAddonFlow(
          RemoveAddonSubmitSuccessKey(this.params.activationDate),
        )
      },
      contractId = this.params.contractId,
      addonsToRemove = this.params.addonsToRemove,
      activationDate = this.params.activationDate,
      baseCost = this.params.baseCost,
      currentTotalCost = this.params.currentTotalCost,
      onFailure = {
        backStack.add(RemoveAddonSubmitFailureKey)
      },
      onCloseFlow = {
        backStack.popUpToRemoveAddonAnchor(inclusive = true)
      },
      navigateUp = backStack::navigateUp,
      existingAddonsToRemove = this.params.existingAddons,
      productVariant = this.params.productVariant,
    )
  }

  navdestination<RemoveAddonSubmitFailureKey> {
    RemoveAddonFailureScreen(
      popBackStack = backStack::popBackStack,
    )
  }

  navdestination<RemoveAddonSubmitSuccessKey> {
    RemoveAddonSuccessScreen(
      this.activationDate,
      popBackStack = backStack::popBackStack,
    )
  }
}

/**
 * The flow's exit anchor: [RemoveAddonsKey] when the insurance picker was shown, or
 * [ChooseAddonToRemoveKey] when the flow was seeded with an insuranceId (the anchor having popped
 * itself during the jump to the addon picker).
 */
private fun MutableList<HedvigNavKey>.popUpToRemoveAddonAnchor(inclusive: Boolean) {
  if (findLastOrNull<RemoveAddonsKey>() != null) {
    popUpTo<RemoveAddonsKey>(inclusive)
  } else {
    popUpTo<ChooseAddonToRemoveKey>(inclusive)
  }
}

private fun MutableList<HedvigNavKey>.navigateExitingRemoveAddonFlow(destination: HedvigNavKey) {
  popUpToRemoveAddonAnchor(inclusive = true)
  add(destination)
}
