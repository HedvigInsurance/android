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
import com.hedvig.feature.remove.addons.ui.RemoveAddonSummaryDestination
import com.hedvig.feature.remove.addons.ui.SelectAddonToRemoveDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable


@Serializable
internal data class SummaryParameters(
  val contractId: String,
  val addonsToRemove: List<CurrentlyActiveAddon>,
  val activationDate: LocalDate,
  val baseCost: ItemCost,
  val currentTotalCost: ItemCost,
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
        navigateToSummary = { contractId: String, addons: List<CurrentlyActiveAddon>, activationDate: LocalDate, baseCost: ItemCost, currentCost: ItemCost ->
          navigator.navigateUnsafe(
            AddonRemoveDestination.Summary(
              params = SummaryParameters(
                contractId = contractId,
                addonsToRemove = addons,
                activationDate = activationDate,
                baseCost = baseCost,
                currentTotalCost = currentCost,
              ),
            ),
          )
        },
      )
    }

    navdestination<AddonRemoveDestination.Summary>(AddonRemoveDestination.Summary) { backStackEntry ->
      RemoveAddonSummaryDestination()
    }

    navdestination<AddonRemoveDestination.SubmitFailure> { backStackEntry ->
      //TODO
    }

    navdestination<AddonRemoveDestination.SubmitSuccess>(AddonRemoveDestination.SubmitSuccess) { backStackEntry ->
      //TODO
    }
  }
}
