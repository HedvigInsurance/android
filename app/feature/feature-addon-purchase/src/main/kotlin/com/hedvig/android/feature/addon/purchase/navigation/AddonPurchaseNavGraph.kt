package com.hedvig.android.feature.addon.purchase.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.ChooseInsuranceToAddAddonDestination
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.CustomizeAddon
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitFailure
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitSuccess
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.Summary
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonDestination
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeTravelAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonDestination
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.success.SubmitAddonFailureScreen
import com.hedvig.android.feature.addon.purchase.ui.success.SubmitAddonSuccessScreen
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryDestination
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.addonPurchaseNavGraph(
  navigator: Navigator,
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
) {
  navgraph<AddonPurchaseGraphDestination>(
    startDestination = ChooseInsuranceToAddAddonDestination::class,
  ) {
    /**
     * Choose insurance to add addon to. Redirects to CustomizeAddon if insuranceIds list has only 1 insurance
     */
    navdestination<ChooseInsuranceToAddAddonDestination> { backStackEntry ->
      val addonPurchaseGraphDestination = navController
        .getRouteFromBackStack<AddonPurchaseGraphDestination>(backStackEntry)
      if (addonPurchaseGraphDestination.insuranceIds.size == 1) {
        LaunchedEffect(Unit) {
          navigator.navigateUnsafe(CustomizeAddon(addonPurchaseGraphDestination.insuranceIds[0]), {
            typedPopUpTo<ChooseInsuranceToAddAddonDestination>({ inclusive = true })
          })
        }
      } else {
        val viewModel: SelectInsuranceForAddonViewModel = koinViewModel {
          parametersOf(addonPurchaseGraphDestination.insuranceIds)
        }
        SelectInsuranceForAddonDestination(
          viewModel = viewModel,
          navigateUp = navigator::navigateUp,
          navigateToCustomizeAddon = { chosenInsuranceId: String ->
            navigator.navigateUnsafe(CustomizeAddon(chosenInsuranceId))
          },
        )
      }
    }

    /**
     * Choose addon option (e.g. 45/60 days)
     */
    navdestination<CustomizeAddon> { backStackEntry ->
      val viewModel: CustomizeTravelAddonViewModel = koinViewModel {
        parametersOf(this.insuranceId)
      }
      CustomizeTravelAddonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        popBackStack = navigator::popBackStack,
        popAddonFlow = {
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
        },
        navigateToSummary = { summaryParameters: SummaryParameters ->
          if (summaryParameters.popCustomizeDestination) {
            navigator.navigateUnsafe(Summary(summaryParameters)) {
              typedPopUpTo<CustomizeAddon> {
                inclusive = true
              }
            }
          } else {
            navigator.navigateUnsafe(Summary(summaryParameters))
          }
        },
        onNavigateToNewConversation = {
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
          onNavigateToNewConversation(backStackEntry)
        },
      )
    }

    /**
     * Summary for the purchase addon flow (not upgrade 45->60)
     */
    navdestination<Summary>(Summary) { backStackEntry ->
      val viewModel: AddonSummaryViewModel = koinViewModel {
        parametersOf(this.params)
      }
      AddonSummaryDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onFailure = {
          navigator.navigateUnsafe(SubmitFailure)
        },
        onSuccess = {
          navigator.navigateUnsafe(SubmitSuccess(this.params.activationDate)) {
            typedPopUpTo<AddonPurchaseGraphDestination> {
              inclusive = true
            }
          }
        },
      )
    }

    navdestination<SubmitFailure> { backStackEntry ->
      SubmitAddonFailureScreen(
        popBackStack = navigator::popBackStack,
      )
    }
  }

  navdestination<SubmitSuccess>(SubmitSuccess) { backStackEntry ->
    SubmitAddonSuccessScreen(
      activationDate = this.activationDate,
      popBackStack = navigator::popBackStack,
    )
  }
}
