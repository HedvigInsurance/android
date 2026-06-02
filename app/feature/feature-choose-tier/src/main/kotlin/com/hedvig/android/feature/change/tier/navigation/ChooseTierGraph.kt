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
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.changeTierGraph(
  backStack: MutableList<HedvigNavKey>,
  onNavigateToNewConversation: () -> Unit,
) {
  entry<StartTierFlowKey> { key ->
    val insuranceId = key.insuranceId
    val viewModel: StartTierFlowViewModel =
      assistedMetroViewModel<StartTierFlowViewModel, StartTierFlowViewModel.Factory> {
        create(insuranceId)
      }
    StartChangeTierFlowDestination(
      viewModel = viewModel,
      popBackStack = {
        backStack.popBackStack()
      },
      launchFlow = { params: InsuranceCustomizationParameters ->
        backStack.navigateAndPopUpTo<StartTierFlowKey>(ChooseTierKey(params), inclusive = true)
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = backStack::navigateUp,
    )
  }

  entry<StartTierFlowChooseInsuranceKey> {
    val viewModel: ChooseInsuranceViewModel = metroViewModel()
    ChooseInsuranceToChangeTierDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateToNextStep = { params: InsuranceCustomizationParameters ->
        backStack.navigateAndPopUpTo<StartTierFlowChooseInsuranceKey>(
          ChooseTierKey(params),
          inclusive = true,
        )
      },
      popBackStack = {
        backStack.popBackStack()
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
      navigateUp = backStack::navigateUp,
      navigateToSummary = { quote ->
        backStack.add(
          SummaryKey(
            SummaryParameters(
              quoteIdToSubmit = quote.id,
              activationDate = parameters.activationDate,
              insuranceId = parameters.insuranceId,
            ),
          ),
        )
      },
      popBackStack = {
        backStack.popBackStack()
      },
      navigateToComparison = { listOfQuotes, selectedTerms ->
        backStack.add(
          ComparisonKey(
            ComparisonParameters(
              termsIds = listOfQuotes.map {
                it.productVariant.termsVersion
              },
              selectedTermsVersion = selectedTerms,
            ),
          ),
        )
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
      navigateUp = backStack::navigateUp,
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
      navigateUp = backStack::navigateUp,
      onExitTierFlow = {
        backStack.popUpTo<ChooseTierKey>(inclusive = true)
      },
      onFailure = {
        backStack.add(SubmitFailureKey)
      },
      onSuccess = {
        backStack.navigateAndPopUpTo<ChooseTierKey>(
          SubmitSuccessKey(key.params.activationDate),
          inclusive = true,
        )
      },
    )
  }

  entry<SubmitSuccessKey> { key ->
    SubmitTierSuccessScreen(
      key.activationDate,
      popBackStack = backStack::popBackStack,
    )
  }

  entry<SubmitFailureKey> {
    SubmitTierFailureScreen(
      popBackStack = backStack::popBackStack,
    )
  }
}
