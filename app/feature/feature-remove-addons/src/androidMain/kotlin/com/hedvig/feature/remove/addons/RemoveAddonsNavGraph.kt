package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
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
data class AddonRemoveGraphDestination(
  val insuranceId: ContractId?,
  val preselectedAddonVariant: AddonVariant?,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(
      typeOf<ContractId?>(),
      typeOf<AddonVariant?>(),
    )
  }
}

internal sealed interface AddonRemoveDestination {
  @Serializable
  data object ChooseInsuranceDestination : AddonRemoveDestination, Destination

  @Serializable
  data class ChooseAddonDestination(
    val insuranceId: ContractId,
    val preselectedAddonVariant: AddonVariant?,
  ) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<ContractId>(),
        typeOf<AddonVariant?>(),
      )
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : AddonRemoveDestination, Destination
}

fun NavGraphBuilder.removeAddonsNavGraph(
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
) {
  navgraph<AddonRemoveGraphDestination>(
    startDestination = AddonRemoveDestination.ChooseInsuranceDestination::class,
    AddonRemoveGraphDestination,
  ) {
    navdestination<AddonRemoveDestination.ChooseInsuranceDestination> { backStackEntry ->
      val graphDestination = navController
        .getRouteFromBackStack<AddonRemoveGraphDestination>(backStackEntry)
      if (graphDestination.insuranceId != null) {
        LaunchedEffect(Unit) {
          navController.navigate(
            AddonRemoveDestination.ChooseAddonDestination(
              insuranceId = graphDestination.insuranceId,
              preselectedAddonVariant = graphDestination.preselectedAddonVariant,
            ),
          ) {
            typedPopUpTo<AddonRemoveDestination.ChooseInsuranceDestination> {
              inclusive = true
            }
          }
        }
      } else {
        SelectInsuranceToRemoveAddonDestination(
          navigateUp = navController::navigateUp,
          navigateToChooseAddon = { contractId ->
            navController.navigate(
              AddonRemoveDestination.ChooseAddonDestination(
                insuranceId = contractId,
                preselectedAddonVariant = null,
              ),
            )
          },
        )
      }
    }

    navdestination<AddonRemoveDestination.ChooseAddonDestination>(AddonRemoveDestination.ChooseAddonDestination) {
      SelectAddonToRemoveDestination(
        contractId = this.insuranceId,
        preselectedAddonProduct = this.preselectedAddonVariant,
        navigateUp = navController::navigateUp,
        navigateToSummary = {
            contractId: ContractId,
            addons: List<CurrentlyActiveAddon>,
            activationDate: LocalDate,
            baseCost: ItemCost,
            currentCost: ItemCost,
            productVariant: ProductVariant,
            allAddons: List<CurrentlyActiveAddon>,
            popDestination: Boolean
          ->
          navController.navigate(
            AddonRemoveDestination.Summary(
              params = SummaryParameters(
                contractId = contractId,
                addonsToRemove = addons,
                activationDate = activationDate,
                baseCost = baseCost,
                currentTotalCost = currentCost,
                productVariant = productVariant,
                allAddons,
              ),
            ),
          )  {
            if (popDestination) {
              typedPopUpTo<AddonRemoveDestination.ChooseAddonDestination> {
                inclusive = true
              }
            }
          }
        },
      )
    }

    navdestination<AddonRemoveDestination.Summary>(AddonRemoveDestination.Summary) { backStackEntry ->
      RemoveAddonSummaryDestination(
        navigateToSuccess = {
          navController.navigate(AddonRemoveDestination.SubmitSuccess(this.params.activationDate)) {
            typedPopUpTo<AddonRemoveGraphDestination> {
              inclusive = true
            }
          }
        },
        contractId = this.params.contractId,
        addonsToRemove = this.params.addonsToRemove,
        activationDate = this.params.activationDate,
        baseCost = this.params.baseCost,
        currentTotalCost = this.params.currentTotalCost,
        onFailure = {
          navController.navigate(AddonRemoveDestination.SubmitFailure)
        },
        onCloseFlow = {
          navController.typedPopBackStack<AddonRemoveGraphDestination>(true)
        },
        navigateUp = navController::navigateUp,
        existingAddonsToRemove = this.params.existingAddons,
        productVariant = this.params.productVariant,
      )
    }

    navdestination<AddonRemoveDestination.SubmitFailure> { backStackEntry ->
      RemoveAddonFailureScreen(
        popBackStack = navController::popBackStack,
      )
    }
  }
  navdestination<AddonRemoveDestination.SubmitSuccess>(AddonRemoveDestination.SubmitSuccess) { backStackEntry ->
    RemoveAddonSuccessScreen(
      this.activationDate,
      popBackStack = navController::popBackStack,
    )
  }
}
