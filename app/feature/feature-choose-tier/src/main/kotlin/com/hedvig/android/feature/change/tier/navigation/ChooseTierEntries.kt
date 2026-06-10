package com.hedvig.android.feature.change.tier.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceToChangeTierDestination
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectTierDestination
import com.hedvig.android.feature.change.tier.ui.stepstart.StartChangeTierFlowDestination
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierFlowViewModel
import com.hedvig.android.feature.change.tier.ui.stepsummary.ChangeTierSummaryDestination
import com.hedvig.android.feature.change.tier.ui.stepsummary.SubmitTierFailureScreen
import com.hedvig.android.feature.change.tier.ui.stepsummary.SubmitTierSuccessScreen
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.changeTierEntries(backstack: Backstack, onNavigateToNewConversation: () -> Unit) {
  entry<StartTierFlowKey> { key ->
    val insuranceId = key.insuranceId
    val viewModel: StartTierFlowViewModel =
      assistedMetroViewModel<StartTierFlowViewModel, StartTierFlowViewModel.Factory> {
        create(insuranceId)
      }
    StartChangeTierFlowDestination(
      viewModel = viewModel,
      popBackstack = {
        backstack.popBackstack()
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = backstack::navigateUp,
    )
  }

  entry<StartTierFlowChooseInsuranceKey> {
    val viewModel: ChooseInsuranceViewModel = metroViewModel()
    ChooseInsuranceToChangeTierDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      popBackstack = {
        backstack.popBackstack()
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }

  entry<ChooseTierKey> { key ->
    val parameters = key.parameters
    val viewModel: SelectCoverageViewModel =
      assistedMetroViewModel<SelectCoverageViewModel, SelectCoverageViewModel.Factory> {
        create(parameters)
      }
    SelectTierDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      popBackstack = {
        backstack.popBackstack()
      },
    )
  }

  entry<ComparisonKey> { key ->
    val comparisonParameters = key.comparisonParameters
    val viewModel: ComparisonViewModel =
      assistedMetroViewModel<ComparisonViewModel, ComparisonViewModel.Factory> {
        create(comparisonParameters)
      }
    ComparisonDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SummaryKey> { key ->
    val params = key.params
    val viewModel: SummaryViewModel =
      assistedMetroViewModel<SummaryViewModel, SummaryViewModel.Factory> {
        create(params)
      }
    ChangeTierSummaryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onExitTierFlow = {
        backstack.popUpTo<ChooseTierKey>(inclusive = true)
      },
    )
  }

  entry<SubmitSuccessKey> { key ->
    SubmitTierSuccessScreen(
      key.activationDate,
      popBackstack = backstack::popBackstack,
    )
  }

  entry<SubmitFailureKey> {
    SubmitTierFailureScreen(
      popBackstack = backstack::popBackstack,
    )
  }
}
