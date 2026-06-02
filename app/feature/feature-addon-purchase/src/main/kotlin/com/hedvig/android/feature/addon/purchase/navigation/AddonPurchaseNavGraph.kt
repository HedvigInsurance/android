package com.hedvig.android.feature.addon.purchase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.CustomizeAddon
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitFailure
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.SubmitSuccess
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.Summary
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation
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
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.serialization.Serializable

@Serializable
internal data class PerilComparisonParams(
  val whatsIncludedPageTitle: String,
  val whatsIncludedPageDescription: String,
  val perilList: List<Pair<String?, List<TravelInsurancePlusExplanation.TravelPerilData>>>,
)

fun EntryProviderScope<HedvigNavKey>.addonPurchaseNavGraph(
  navigator: Navigator,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToChangeTier: (contractId: String) -> Unit,
) {
  // Deep-link entry: if the URI carried a contractId, jump straight into the flow; otherwise let
  // the member pick an eligible insurance.
  navdestination<TravelAddonTriage> {
    val source = this.source
    val contractId = this.contractId
    if (contractId != null) {
      LaunchedEffect(Unit) {
        navigator.navigate<TravelAddonTriage>(
          AddonPurchaseGraphDestination(
            insuranceIds = listOf(contractId),
            preselectedAddonDisplayName = null,
            source = source,
          ),
          inclusive = true,
        )
      }
    } else {
      val viewModel: TravelAddonTriageViewModel =
        assistedMetroViewModel<TravelAddonTriageViewModel, TravelAddonTriageViewModel.Factory> {
          create(source)
        }
      TravelAddonTriageDestination(
        viewModel = viewModel,
        popBackStack = navigator::popBackStack,
        launchFlow = { insuranceIds: List<String> ->
          navigator.navigate<TravelAddonTriage>(
            AddonPurchaseGraphDestination(
              insuranceIds = insuranceIds,
              preselectedAddonDisplayName = null,
              source = source,
            ),
            inclusive = true,
          )
        },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      )
    }
  }

  // Flow anchor. With a single eligible insurance it renders CustomizeAddon inline (so the anchor
  // stays on the back stack as the flow's exit/source reference); otherwise it picks an insurance.
  navdestination<AddonPurchaseGraphDestination> {
    val insuranceIds = this.insuranceIds
    val preselectedAddonDisplayNames = listOfNotNull(this.preselectedAddonDisplayName)
    if (insuranceIds.size == 1) {
      CustomizeAddonContent(
        navigator = navigator,
        insuranceId = insuranceIds[0],
        preselectedAddonDisplayNames = preselectedAddonDisplayNames,
        popBackStack = popBackStack,
        finishApp = finishApp,
        onNavigateToChangeTier = onNavigateToChangeTier,
      )
    } else {
      val viewModel: SelectInsuranceForAddonViewModel =
        assistedMetroViewModel<SelectInsuranceForAddonViewModel, SelectInsuranceForAddonViewModel.Factory> {
          create(insuranceIds)
        }
      SelectInsuranceForAddonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        popBackStack = popBackStack,
        navigateToCustomizeAddon = { chosenInsuranceId: String ->
          navigator.navigate(CustomizeAddon(chosenInsuranceId, preselectedAddonDisplayNames))
        },
      )
    }
  }

  navdestination<CustomizeAddon> {
    CustomizeAddonContent(
      navigator = navigator,
      insuranceId = this.insuranceId,
      preselectedAddonDisplayNames = this.preselectedAddonDisplayNames,
      popBackStack = popBackStack,
      finishApp = finishApp,
      onNavigateToChangeTier = onNavigateToChangeTier,
    )
  }

  navdestination<TravelInsurancePlusExplanation> {
    TravelInsurancePlusExplanationDestination(
      params = this.perilData,
      navigateUp = navigator::navigateUp,
    )
  }

  navdestination<Summary> {
    val summaryParameters = this.params
    val source = navigator.findLastOrNull<AddonPurchaseGraphDestination>()?.source
      ?: AddonBannerSource.TRAVEL_DEEPLINK
    val viewModel: AddonSummaryViewModel =
      assistedMetroViewModel<AddonSummaryViewModel, AddonSummaryViewModel.Factory> {
        create(summaryParameters, source)
      }
    AddonSummaryDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateBack = popBackStack,
      onFailure = {
        navigator.navigate(SubmitFailure)
      },
      onSuccess = {
        navigator.navigate<AddonPurchaseGraphDestination>(
          SubmitSuccess(this.params.activationDate),
          inclusive = true,
        )
      },
    )
  }

  navdestination<SubmitFailure> {
    SubmitAddonFailureScreen(
      popBackStack = popBackStack,
    )
  }

  navdestination<SubmitSuccess> {
    SubmitAddonSuccessScreen(
      activationDate = this.activationDate,
      popBackStack = popBackStack,
    )
  }
}

@Composable
private fun CustomizeAddonContent(
  navigator: Navigator,
  insuranceId: String,
  preselectedAddonDisplayNames: List<String>,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  onNavigateToChangeTier: (contractId: String) -> Unit,
) {
  val viewModel: CustomizeAddonViewModel =
    assistedMetroViewModel<CustomizeAddonViewModel, CustomizeAddonViewModel.Factory> {
      create(insuranceId, preselectedAddonDisplayNames)
    }
  CustomizeAddonDestination(
    viewModel = viewModel,
    navigateUp = navigator::navigateUp,
    popBackStack = popBackStack,
    popAddonFlow = {
      // Drop everything above the anchor, then the anchor itself; if it was the root there is
      // nothing left to show, so finish.
      navigator.popUpTo<AddonPurchaseGraphDestination>(inclusive = false)
      if (!navigator.popBackStack()) {
        finishApp()
      }
    },
    navigateToSummary = { summaryParameters: SummaryParameters ->
      navigator.navigate(Summary(summaryParameters))
    },
    onNavigateToTravelInsurancePlusExplanation = { perilData ->
      navigator.navigate(TravelInsurancePlusExplanation(perilData))
    },
    navigateToChangeTier = { contractId ->
      navigator.popUpTo<AddonPurchaseGraphDestination>(inclusive = true)
      onNavigateToChangeTier(contractId)
    },
  )
}
