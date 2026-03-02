package com.hedvig.android.feature.addon.purchase.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.addons.data.AddonBannerSource
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
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
internal data class PerilComparisonParams(
  val whatsIncludedPageTitle: String,
  val whatsIncludedPageDescription: String,
  val perilList: List<Pair<String?, List<TravelPerilData>>>,
)

fun NavGraphBuilder.addonPurchaseNavGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToChangeTier: (contractId: String) -> Unit,
) {
  // Destination to get eligible insuranceIds if member comes to the feature using the deeplink
  navdestination<TravelAddonTriage>(
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.travelAddon,
      hedvigDeepLinkContainer.carAddon,
      hedvigDeepLinkContainer.travelAddonWithContractId,
      hedvigDeepLinkContainer.carAddonWithContractId
    ),
  ) { backStackEntry ->

    val deepLinkInfo = getDeepLinkInfoFromBackStackEntry(backStackEntry)
    logcat { "Mariia: deepLinkInfo: $deepLinkInfo" }
    if (deepLinkInfo.contractId!=null) {
      LaunchedEffect(Unit) {
        navController.navigate(
          AddonPurchaseGraphDestination(
            insuranceIds = listOf(deepLinkInfo.contractId),
            preselectedAddonDisplayName = null,
            source = deepLinkInfo.source,
          ),
        ) {
          typedPopUpTo<TravelAddonTriage>({ inclusive = true })
        }
      }
    } else {
      val viewModel: TravelAddonTriageViewModel = koinViewModel {
        parametersOf(deepLinkInfo.source)
      }
      TravelAddonTriageDestination(
        viewModel = viewModel,
        popBackStack = navController::popBackStack,
        launchFlow = { insuranceIds: List<String> ->
          navController.navigate(
            AddonPurchaseGraphDestination(
              insuranceIds = insuranceIds,
              preselectedAddonDisplayName = null,
              source = deepLinkInfo.source,
            ),
          ) {
            typedPopUpTo<TravelAddonTriage>({ inclusive = true })
          }
        },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      )
    }
  }

  navgraph<AddonPurchaseGraphDestination>(
    startDestination = ChooseInsuranceToAddAddonDestination::class,
  ) {
    // Choose insurance to add addon to. Redirects to CustomizeAddon if insuranceIds list has only 1 insurance
    navdestination<ChooseInsuranceToAddAddonDestination> { backStackEntry ->
      val addonPurchaseGraphDestination = navController
        .getRouteFromBackStack<AddonPurchaseGraphDestination>(backStackEntry)
      val preselectedAddonDisplayNames = listOfNotNull(addonPurchaseGraphDestination.preselectedAddonDisplayName)
      if (addonPurchaseGraphDestination.insuranceIds.size == 1) {
        LaunchedEffect(Unit) {
          navController.navigate(
            CustomizeAddon(addonPurchaseGraphDestination.insuranceIds[0], preselectedAddonDisplayNames),
          ) {
            typedPopUpTo<ChooseInsuranceToAddAddonDestination>({ inclusive = true })
          }
        }
      } else {
        val viewModel: SelectInsuranceForAddonViewModel = koinViewModel {
          parametersOf(addonPurchaseGraphDestination.insuranceIds)
        }
        SelectInsuranceForAddonDestination(
          viewModel = viewModel,
          navigateUp = navController::navigateUp,
          navigateToCustomizeAddon = { chosenInsuranceId: String ->
            navController.navigate(CustomizeAddon(chosenInsuranceId, preselectedAddonDisplayNames))
          },
        )
      }
    }

    // Choose addon option (e.g. 45/60 days for travel addon or different car plus options)
    navdestination<CustomizeAddon> {
      val viewModel: CustomizeAddonViewModel = koinViewModel {
        parametersOf(this.insuranceId, this.preselectedAddonDisplayNames)
      }
      CustomizeAddonDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        popAddonFlow = {
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
        },
        navigateToSummary = { summaryParameters: SummaryParameters ->
          navController.navigate(Summary(summaryParameters))
        },
        onNavigateToTravelInsurancePlusExplanation = { perilData ->
          navController.navigate(
            TravelInsurancePlusExplanation(perilData),
          )
        },
        navigateToChangeTier = { contractId ->
          navController.typedPopBackStack<AddonPurchaseGraphDestination>(inclusive = true)
          onNavigateToChangeTier(contractId)
        },
      )
    }

    navdestination<TravelInsurancePlusExplanation>(TravelInsurancePlusExplanation) { backStackEntry ->
      val perilData = backStackEntry.toRoute<TravelInsurancePlusExplanation>().perilData
      TravelInsurancePlusExplanationDestination(
        params = perilData,
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<Summary>(Summary) { backStackEntry ->
      val source = navController
        .getRouteFromBackStack<AddonPurchaseGraphDestination>(backStackEntry).source
      val viewModel: AddonSummaryViewModel = koinViewModel {
        parametersOf(this.params, source)
      }
      AddonSummaryDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        onFailure = {
          navController.navigate(SubmitFailure)
        },
        onSuccess = {
          navController.navigate(SubmitSuccess(this.params.activationDate)) {
            typedPopUpTo<AddonPurchaseGraphDestination> {
              inclusive = true
            }
          }
        },
      )
    }

    navdestination<SubmitFailure> {
      SubmitAddonFailureScreen(
        popBackStack = navController::popBackStack,
      )
    }
  }

  navdestination<SubmitSuccess>(SubmitSuccess) {
    SubmitAddonSuccessScreen(
      activationDate = this.activationDate,
      popBackStack = navController::popBackStack,
    )
  }
}

private fun getDeepLinkInfoFromBackStackEntry(backStackEntry: NavBackStackEntry): DeepLinkInfo {

  val intent = if (android.os.Build.VERSION.SDK_INT >= 33) {
    backStackEntry.arguments?.getParcelable(
      "android-support-nav:controller:deepLinkIntent",
      android.content.Intent::class.java,
    )
  } else {
    @Suppress("DEPRECATION")
    backStackEntry.arguments?.getParcelable(
      "android-support-nav:controller:deepLinkIntent",
    )
  }
  val deepLinkPath = intent?.data?.path
  val contractId = intent?.data?.getQueryParameter("contractId")?.removeSurrounding("{", "}")


  val source: AddonBannerSource = when {
    deepLinkPath?.contains("travel-addon") == true -> AddonBannerSource.TRAVEL_DEEPLINK
    deepLinkPath?.contains("car-plus-addon") == true -> AddonBannerSource.CAR_ADDON_DEEPLINK
    else -> AddonBannerSource.TRAVEL_DEEPLINK
  }

  return DeepLinkInfo(source, contractId)
}


private data class DeepLinkInfo(
  val source: AddonBannerSource,
  val contractId: String?,
)
