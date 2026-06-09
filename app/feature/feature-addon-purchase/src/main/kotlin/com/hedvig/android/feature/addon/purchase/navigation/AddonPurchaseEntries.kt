package com.hedvig.android.feature.addon.purchase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.data.addons.data.AddonBannerSource
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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.serialization.Serializable

@Serializable
internal data class PerilComparisonParams(
  val whatsIncludedPageTitle: String,
  val whatsIncludedPageDescription: String,
  val perilList: List<Pair<String?, List<TravelInsurancePlusExplanationKey.TravelPerilData>>>,
)

fun EntryProviderScope<HedvigNavKey>.addonPurchaseEntries(
  backstack: Backstack,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToChangeTier: (contractId: String) -> Unit,
) {
  // Deep-link entry: if the URI carried a contractId, jump straight into the flow; otherwise let
  // the member pick an eligible insurance.
  entry<TravelAddonTriageKey> { key ->
    val source = key.source
    val contractId = key.contractId
    if (contractId != null) {
      LaunchedEffect(Unit) {
        backstack.navigateAndPopUpTo<TravelAddonTriageKey>(
          AddonPurchaseKey(
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
        popBackstack = backstack::popBackstack,
        launchFlow = { insuranceIds: List<String> ->
          backstack.navigateAndPopUpTo<TravelAddonTriageKey>(
            AddonPurchaseKey(
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
  entry<AddonPurchaseKey> { key ->
    val insuranceIds = key.insuranceIds
    val preselectedAddonDisplayNames = listOfNotNull(key.preselectedAddonDisplayName)
    if (insuranceIds.size == 1) {
      CustomizeAddonContent(
        backstack = backstack,
        insuranceId = insuranceIds[0],
        preselectedAddonDisplayNames = preselectedAddonDisplayNames,
        onNavigateToChangeTier = onNavigateToChangeTier,
      )
    } else {
      val viewModel: SelectInsuranceForAddonViewModel =
        assistedMetroViewModel<SelectInsuranceForAddonViewModel, SelectInsuranceForAddonViewModel.Factory> {
          create(insuranceIds, preselectedAddonDisplayNames)
        }
      SelectInsuranceForAddonDestination(
        viewModel = viewModel,
        navigateUp = backstack::navigateUp,
        popBackstack = backstack::popBackstack,
      )
    }
  }

  entry<CustomizeAddonKey> { key ->
    CustomizeAddonContent(
      backstack = backstack,
      insuranceId = key.insuranceId,
      preselectedAddonDisplayNames = key.preselectedAddonDisplayNames,
      onNavigateToChangeTier = onNavigateToChangeTier,
    )
  }

  entry<TravelInsurancePlusExplanationKey> { key ->
    TravelInsurancePlusExplanationDestination(
      params = key.perilData,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SummaryKey> { key ->
    val summaryParameters = key.params
    val source = backstack.findLastOrNull<AddonPurchaseKey>()?.source
      ?: AddonBannerSource.TRAVEL_DEEPLINK
    val viewModel: AddonSummaryViewModel =
      assistedMetroViewModel<AddonSummaryViewModel, AddonSummaryViewModel.Factory> {
        create(summaryParameters, source)
      }
    AddonSummaryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
    )
  }

  entry<SubmitFailureKey> {
    SubmitAddonFailureScreen(
      popBackstack = backstack::popBackstack,
    )
  }

  entry<SubmitSuccessKey> { key ->
    SubmitAddonSuccessScreen(
      activationDate = key.activationDate,
      popBackstack = backstack::popBackstack,
    )
  }
}

@Composable
private fun CustomizeAddonContent(
  backstack: Backstack,
  insuranceId: String,
  preselectedAddonDisplayNames: List<String>,
  onNavigateToChangeTier: (contractId: String) -> Unit,
) {
  val viewModel: CustomizeAddonViewModel =
    assistedMetroViewModel<CustomizeAddonViewModel, CustomizeAddonViewModel.Factory> {
      create(insuranceId, preselectedAddonDisplayNames)
    }
  CustomizeAddonDestination(
    viewModel = viewModel,
    navigateUp = backstack::navigateUp,
    popBackstack = backstack::popBackstack,
    popAddonFlow = {
      backstack.popUpTo<AddonPurchaseKey>(inclusive = false)
      backstack.popBackstack()
    },
    onNavigateToTravelInsurancePlusExplanation = { perilData ->
      backstack.add(TravelInsurancePlusExplanationKey(perilData))
    },
    navigateToChangeTier = { contractId ->
      backstack.popUpTo<AddonPurchaseKey>(inclusive = true)
      onNavigateToChangeTier(contractId)
    },
  )
}
