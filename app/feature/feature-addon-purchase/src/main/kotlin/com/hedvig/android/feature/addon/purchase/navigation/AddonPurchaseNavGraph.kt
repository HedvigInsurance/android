package com.hedvig.android.feature.addon.purchase.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.ChooseInsuranceToAddAddonDestination
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.CustomizeAddon
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitFailure
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitSuccess
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.Summary
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack

import com.hedvig.android.navigation.core.Navigator

import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.addonPurchaseNavGraph(navigator: Navigator, navController: NavController) {
  navgraph<AddonPurchaseGraphDestination>(
    startDestination = ChooseInsuranceToAddAddonDestination::class,
  ) {
    navdestination<ChooseInsuranceToAddAddonDestination> { backStackEntry ->
      val addonPurchaseGraphDestination = navController
        .getRouteFromBackStack<AddonPurchaseGraphDestination>(backStackEntry)
      val viewModel: SelectInsuranceForAddonViewModel = koinViewModel {
        parametersOf(addonPurchaseGraphDestination.insuranceIds)
      }
      SelectInsuranceForAddonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        popBackStack = navigator::popBackStack,
        navigateToCustomizeAddon = {
          TODO()
        }
      )
    }
    navdestination<CustomizeAddon> { backStackEntry ->
      TODO()
//      navigateToSummary = { summaryParameters: SummaryParameters ->
//        navigator.navigateUnsafe(Summary(summaryParameters))
//      },
    }
    navdestination<Summary> { backStackEntry ->
      TODO()
    }

    navdestination<SubmitSuccess> { backStackEntry ->
      TODO()
    }

    navdestination<SubmitFailure> { backStackEntry ->
      TODO()
    }
  }
}
