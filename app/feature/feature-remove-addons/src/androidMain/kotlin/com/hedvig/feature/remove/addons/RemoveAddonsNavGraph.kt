package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.feature.remove.addons.data.CurrentlyActiveAddon
import com.hedvig.feature.remove.addons.ui.RemoveAddonFailureScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSuccessScreen
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryDestination
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination
import kotlin.reflect.KClass
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
data class AddonRemoveGraphDestination(
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

internal sealed interface AddonRemoveDestination {
  @Serializable
  data class ChooseAddonDestination(
    val insuranceId: ContractId,
    val preselectedAddonVariant: AddonVariant?,
  ) : AddonRemoveDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<ContractId>(),
        typeOf<AddonVariant?>(),
      )
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : AddonRemoveDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : AddonRemoveDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : AddonRemoveDestination, HedvigNavKey
}

fun EntryProviderScope<HedvigNavKey>.removeAddonsNavGraph(navigator: Navigator) {
  // Flow anchor / insurance picker. When seeded with an insuranceId it jumps straight to the addon
  // picker (popping itself, so back leaves the flow); otherwise the member picks an insurance.
  navdestination<AddonRemoveGraphDestination> {
    val insuranceId = this.insuranceId
    val preselectedAddonVariant = this.preselectedAddonVariant
    if (insuranceId != null) {
      LaunchedEffect(Unit) {
        navigator.navigate<AddonRemoveGraphDestination>(
          AddonRemoveDestination.ChooseAddonDestination(
            insuranceId = insuranceId,
            preselectedAddonVariant = preselectedAddonVariant,
          ),
          inclusive = true,
        )
      }
    } else {
      SelectInsuranceToRemoveAddonDestination(
        navigateUp = navigator::navigateUp,
        navigateToChooseAddon = { contractId ->
          navigator.navigate(
            AddonRemoveDestination.ChooseAddonDestination(
              insuranceId = contractId,
              preselectedAddonVariant = null,
            ),
          )
        },
      )
    }
  }

  navdestination<AddonRemoveDestination.ChooseAddonDestination> {
    SelectAddonToRemoveDestination(
      contractId = this.insuranceId,
      preselectedAddonProduct = this.preselectedAddonVariant,
      navigateUp = navigator::navigateUp,
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
        val summary = AddonRemoveDestination.Summary(
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
          navigator.navigate<AddonRemoveDestination.ChooseAddonDestination>(summary, inclusive = true)
        } else {
          navigator.navigate(summary)
        }
      },
    )
  }

  navdestination<AddonRemoveDestination.Summary> {
    RemoveAddonSummaryDestination(
      navigateToSuccess = {
        navigator.navigateExitingRemoveAddonFlow(
          AddonRemoveDestination.SubmitSuccess(this.params.activationDate),
        )
      },
      contractId = this.params.contractId,
      addonsToRemove = this.params.addonsToRemove,
      activationDate = this.params.activationDate,
      baseCost = this.params.baseCost,
      currentTotalCost = this.params.currentTotalCost,
      onFailure = {
        navigator.navigate(AddonRemoveDestination.SubmitFailure)
      },
      onCloseFlow = {
        navigator.popUpTo(navigator.removeAddonFlowAnchorClass(), inclusive = true)
      },
      navigateUp = navigator::navigateUp,
      existingAddonsToRemove = this.params.existingAddons,
      productVariant = this.params.productVariant,
    )
  }

  navdestination<AddonRemoveDestination.SubmitFailure> {
    RemoveAddonFailureScreen(
      popBackStack = navigator::popBackStack,
    )
  }

  navdestination<AddonRemoveDestination.SubmitSuccess> {
    RemoveAddonSuccessScreen(
      this.activationDate,
      popBackStack = navigator::popBackStack,
    )
  }
}

/**
 * The flow's exit anchor: [AddonRemoveGraphDestination] when the insurance picker was shown, or
 * [AddonRemoveDestination.ChooseAddonDestination] when the flow was seeded with an insuranceId (the
 * anchor having popped itself during the jump to the addon picker).
 */
private fun Navigator.removeAddonFlowAnchorClass(): KClass<out HedvigNavKey> =
  if (findLastOrNull<AddonRemoveGraphDestination>() != null) {
    AddonRemoveGraphDestination::class
  } else {
    AddonRemoveDestination.ChooseAddonDestination::class
  }

private fun Navigator.navigateExitingRemoveAddonFlow(destination: HedvigNavKey) {
  navigate(destination, removeAddonFlowAnchorClass(), inclusive = true)
}
