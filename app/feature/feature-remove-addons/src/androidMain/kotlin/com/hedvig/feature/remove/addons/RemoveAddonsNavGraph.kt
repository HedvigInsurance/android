package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.Navigator
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
import com.hedvig.android.data.productvariant.ProductVariant


@Serializable
internal data class SummaryParameters(
  val contractId: String,
  val addonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
  val productVariant: ProductVariant,
  val existingAddons: List<CurrentlyActiveAddon>,
)

@Serializable
data class AddonRemoveGraphDestination(
  val insuranceId: String?,
  val addonId: String?,
) : Destination


internal sealed interface AddonRemoveDestination {
  @Serializable
  data object ChooseInsuranceDestination : AddonRemoveDestination, Destination

  @Serializable
  data class ChooseAddonDestination(
    val insuranceId: String,
    val addonId: String?,
  ) : AddonRemoveDestination, Destination


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
  navigator: Navigator,
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
) {
  navgraph<AddonRemoveGraphDestination>(
    startDestination = AddonRemoveDestination.ChooseInsuranceDestination::class,
  ) {
    navdestination<AddonRemoveDestination.ChooseInsuranceDestination> { backStackEntry ->
      val graphDestination = navController
        .getRouteFromBackStack<AddonRemoveGraphDestination>(backStackEntry)
      if (graphDestination.insuranceId != null) {
        LaunchedEffect(Unit) {
          navigator.navigateUnsafe(
            AddonRemoveDestination.ChooseAddonDestination(
              insuranceId = graphDestination.insuranceId,
              addonId = graphDestination.addonId,
            ),
          ) {
            typedPopUpTo<AddonRemoveDestination.ChooseInsuranceDestination>(
              { inclusive = true },
            )
          }
        }
      } else {
        SelectInsuranceToRemoveAddonDestination(
          navigateUp = navigator::navigateUp,
          navigateToChooseAddon = { chosenInsuranceId: String ->
            navigator.navigateUnsafe(
              AddonRemoveDestination.ChooseAddonDestination(
                insuranceId = chosenInsuranceId,
                addonId = null,
              ),
            )
          },
        )
      }
    }

    navdestination<AddonRemoveDestination.ChooseAddonDestination> { backStackEntry ->
      SelectAddonToRemoveDestination(
        contractId = this.insuranceId,
        preselectedAddonId = this.addonId,
        navigateUp = navigator::navigateUp,
        navigateToSummary = { contractId: String, addons: List<CurrentlyActiveAddon>,
                              activationDate: LocalDate, baseCost: ItemCost, currentCost: ItemCost,
          productVariant: ProductVariant, allAddons: List<CurrentlyActiveAddon> ->
          navigator.navigateUnsafe(
            AddonRemoveDestination.Summary(
              params = SummaryParameters(
                contractId = contractId,
                addonsToRemove = addons,
                activationDate = activationDate,
                baseCost = baseCost,
                currentTotalCost = currentCost,
                productVariant = productVariant,
                allAddons
              ),
            ),
          )
        },
      )
    }

    navdestination<AddonRemoveDestination.Summary>(AddonRemoveDestination.Summary) { backStackEntry ->
      RemoveAddonSummaryDestination(
        navigateToSuccess = {
          navigator.navigateUnsafe(AddonRemoveDestination.SubmitSuccess(this.params.activationDate)) {
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
          navigator.navigateUnsafe(AddonRemoveDestination.SubmitFailure)
        },
        navigateUp = navigator::navigateUp,
        existingAddonsToRemove = this.params.existingAddons,
        productVariant = this.params.productVariant
      )
    }

    navdestination<AddonRemoveDestination.SubmitFailure> { backStackEntry ->
      RemoveAddonFailureScreen(
        popBackStack = navigator::popBackStack,
      )
    }
  }
  navdestination<AddonRemoveDestination.SubmitSuccess>(AddonRemoveDestination.SubmitSuccess) { backStackEntry ->
    RemoveAddonSuccessScreen(
      this.activationDate,
      popBackStack = navigator::popBackStack,
    )
  }
}
