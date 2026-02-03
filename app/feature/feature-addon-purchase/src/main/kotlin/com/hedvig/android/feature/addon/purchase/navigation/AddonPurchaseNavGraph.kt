package com.hedvig.android.feature.addon.purchase.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.ChooseInsuranceToAddAddonDestination
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.CustomizeAddon
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitFailure
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitSuccess
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.Summary
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelAddonTriage
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation.TravelPerilData
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonDestination
import com.hedvig.android.feature.addon.purchase.ui.customize.CustomizeAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonDestination
import com.hedvig.android.feature.addon.purchase.ui.selectinsurance.SelectInsuranceForAddonViewModel
import com.hedvig.android.feature.addon.purchase.ui.success.SubmitAddonFailureScreen
import com.hedvig.android.feature.addon.purchase.ui.success.SubmitAddonSuccessScreen
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryDestination
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryViewModel
import com.hedvig.android.feature.addon.purchase.ui.travelinsuranceplusexplanation.TravelInsurancePlusExplanationDestination
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageDestination
import com.hedvig.android.feature.addon.purchase.ui.triage.TravelAddonTriageViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.addonPurchaseNavGraph(
  navigator: Navigator,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
) {
  /**
   * Destination to get eligible insuranceIds if member comes to the feature using the deeplink
   */
  navdestination<TravelAddonTriage>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.travelAddon),
  ) { backStackEntry ->
    val viewModel: TravelAddonTriageViewModel = koinViewModel()
    TravelAddonTriageDestination(
      viewModel = viewModel,
      popBackStack = navigator::popBackStack,
      launchFlow = { insuranceIds: List<String> ->
        navigator.navigateUnsafe(
          AddonPurchaseGraphDestination(
            insuranceIds,
            AddonBannerSource.TRAVEL_DEEPLINK,
          ),
        ) {
          typedPopUpTo<TravelAddonTriage>({ inclusive = true })
        }
      },
      onNavigateToNewConversation = {
        onNavigateToNewConversation(backStackEntry)
      },
    )
  }

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
          navigator.navigateUnsafe(CustomizeAddon(addonPurchaseGraphDestination.insuranceIds[0])) {
            typedPopUpTo<ChooseInsuranceToAddAddonDestination>({ inclusive = true })
          }
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
      val viewModel: CustomizeAddonViewModel = koinViewModel {
        parametersOf(this.insuranceId)
      }
      CustomizeAddonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        popBackStack = navigator::popBackStack,
        popAddonFlow = {
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
        },
        navigateToSummary = { summaryParameters: SummaryParameters ->
          navController.navigate(Summary(summaryParameters))
        },
        onNavigateToTravelInsurancePlusExplanation = { perilData ->
          navigator.navigateUnsafe(
            TravelInsurancePlusExplanation(
              perilData.map { item ->
                item.first to item.second.map { peril ->
                  TravelPerilData(
                    title = peril.title,
                    description = peril.description,
                    covered = peril.covered,
                    colorCode = peril.colorCode,
                    isEnabled = peril.isEnabled,
                  )
                }
              },
            ),
          )
        },
        onNavigateToNewConversation = {
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
          onNavigateToNewConversation(backStackEntry)
        },
      )
    }

    navdestination<TravelInsurancePlusExplanation>(TravelInsurancePlusExplanation) { backStackEntry ->
      val perilData = backStackEntry.toRoute<TravelInsurancePlusExplanation>().perilData
      TravelInsurancePlusExplanationDestination(
        travelPerilData = perilData,
        navigateUp = navController::navigateUp,
      )
    }

    /**
     * Summary for the purchase addon flow (not upgrade 45->60)
     */
    navdestination<Summary>(Summary) { backStackEntry ->
      val source = navController
        .getRouteFromBackStack<AddonPurchaseGraphDestination>(backStackEntry).source
      val viewModel: AddonSummaryViewModel = koinViewModel {
        parametersOf(this.params, source)
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
