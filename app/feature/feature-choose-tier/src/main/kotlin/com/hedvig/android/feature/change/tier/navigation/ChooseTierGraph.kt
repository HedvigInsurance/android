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
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.changeTierGraph(navigator: Navigator, onNavigateToNewConversation: () -> Unit) {
  navdestination<StartTierFlowDestination> {
    val insuranceId = this.insuranceId
    val viewModel: StartTierFlowViewModel =
      assistedMetroViewModel<StartTierFlowViewModel, StartTierFlowViewModel.Factory> {
        create(insuranceId)
      }
    StartChangeTierFlowDestination(
      viewModel = viewModel,
      popBackStack = {
        navigator.popBackStack()
      },
      launchFlow = { params: InsuranceCustomizationParameters ->
        navigator.navigate<StartTierFlowDestination>(ChooseTierGraphDestination(params), inclusive = true)
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = navigator::navigateUp,
    )
  }

  navdestination<StartTierFlowChooseInsuranceDestination> {
    val viewModel: ChooseInsuranceViewModel = metroViewModel()
    ChooseInsuranceToChangeTierDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateToNextStep = { params: InsuranceCustomizationParameters ->
        navigator.navigate<StartTierFlowChooseInsuranceDestination>(
          ChooseTierGraphDestination(params),
          inclusive = true,
        )
      },
      popBackStack = {
        navigator.popBackStack()
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }

  navdestination<ChooseTierGraphDestination> {
    val parameters = this.parameters
    val viewModel: SelectCoverageViewModel =
      assistedMetroViewModel<SelectCoverageViewModel, SelectCoverageViewModel.Factory> {
        create(parameters)
      }
    SelectTierDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateToSummary = { quote ->
        navigator.navigate(
          ChooseTierDestination.Summary(
            SummaryParameters(
              quoteIdToSubmit = quote.id,
              activationDate = parameters.activationDate,
              insuranceId = parameters.insuranceId,
            ),
          ),
        )
      },
      popBackStack = {
        navigator.popBackStack()
      },
      navigateToComparison = { listOfQuotes, selectedTerms ->
        navigator.navigate(
          ChooseTierDestination.Comparison(
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

  navdestination<ChooseTierDestination.Comparison> {
    val comparisonParameters = this.comparisonParameters
    val viewModel: ComparisonViewModel =
      assistedMetroViewModel<ComparisonViewModel, ComparisonViewModel.Factory> {
        create(comparisonParameters)
      }
    ComparisonDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
    )
  }

  navdestination<ChooseTierDestination.Summary> {
    val params = this.params
    val viewModel: SummaryViewModel =
      assistedMetroViewModel<SummaryViewModel, SummaryViewModel.Factory> {
        create(params)
      }
    ChangeTierSummaryDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      onExitTierFlow = {
        navigator.popUpTo<ChooseTierGraphDestination>(inclusive = true)
      },
      onFailure = {
        navigator.navigate(ChooseTierDestination.SubmitFailure)
      },
      onSuccess = {
        navigator.navigate<ChooseTierGraphDestination>(
          ChooseTierDestination.SubmitSuccess(this.params.activationDate),
          inclusive = true,
        )
      },
    )
  }

  navdestination<ChooseTierDestination.SubmitSuccess> {
    SubmitTierSuccessScreen(
      activationDate,
      popBackStack = navigator::popBackStack,
    )
  }

  navdestination<ChooseTierDestination.SubmitFailure> {
    SubmitTierFailureScreen(
      popBackStack = navigator::popBackStack,
    )
  }
}
