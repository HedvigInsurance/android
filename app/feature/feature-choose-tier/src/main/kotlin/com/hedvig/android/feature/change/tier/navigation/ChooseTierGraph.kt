package com.hedvig.android.feature.change.tier.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
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
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeTierGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onNavigateToNewConversation: () -> Unit,
) {
  navdestination<StartTierFlowDestination> (
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.changeTierWithContractId),
  ) { _ ->
    val viewModel: StartTierFlowViewModel = koinViewModel {
      parametersOf(this.insuranceId)
    }
    StartChangeTierFlowDestination(
      viewModel = viewModel,
      popBackStack = {
        navController.popBackStack()
      },
      launchFlow = { params: InsuranceCustomizationParameters ->
        navController.navigate(ChooseTierGraphDestination(params)) {
          typedPopUpTo<StartTierFlowDestination> {
            inclusive = true
          }
        }
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      navigateUp = navController::navigateUp,
    )
  }

  navdestination<StartTierFlowChooseInsuranceDestination>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.changeTierWithoutContractId),
  ) {
    val viewModel: ChooseInsuranceViewModel = koinViewModel()
    ChooseInsuranceToChangeTierDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      navigateToNextStep = { params: InsuranceCustomizationParameters ->
        navController.navigate(ChooseTierGraphDestination(params)) {
          typedPopUpTo<StartTierFlowChooseInsuranceDestination> {
            inclusive = true
          }
        }
      },
      popBackStack = {
        navController.popBackStack()
      },
      onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
    )
  }

  navgraph<ChooseTierGraphDestination>(
    startDestination = ChooseTierDestination.SelectTierAndDeductible::class,
    destinationNavTypeAware = ChooseTierGraphDestination,
  ) {
    navdestination<ChooseTierDestination.SelectTierAndDeductible> { backStackEntry ->
      val chooseTierGraphDestination = navController
        .getRouteFromBackStack<ChooseTierGraphDestination>(backStackEntry)
      val viewModel: SelectCoverageViewModel = koinViewModel {
        parametersOf(chooseTierGraphDestination.parameters)
      }
      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateToSummary = { quote ->
          navController.navigate(
            ChooseTierDestination.Summary(
              SummaryParameters(
                quoteIdToSubmit = quote.id,
                activationDate = chooseTierGraphDestination.parameters.activationDate,
                insuranceId = chooseTierGraphDestination.parameters.insuranceId,
              ),
            ),
          )
        },
        popBackStack = {
          navController.popBackStack()
        },
        navigateToComparison = { listOfQuotes, selectedTerms ->
          navController.navigate(
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

    navdestination<ChooseTierDestination.Comparison>(
      destinationNavTypeAware = ChooseTierDestination.Comparison.Companion,
    ) { _ ->
      val viewModel: ComparisonViewModel = koinViewModel {
        parametersOf(this.comparisonParameters)
      }
      ComparisonDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<ChooseTierDestination.Summary>(
      destinationNavTypeAware = ChooseTierDestination.Summary.Companion,
    ) {
      val viewModel: SummaryViewModel = koinViewModel {
        parametersOf(this.params)
      }
      ChangeTierSummaryDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onExitTierFlow = {
          navController.typedPopBackStack<ChooseTierGraphDestination>(inclusive = true)
        },
        onFailure = {
          navController.navigate(ChooseTierDestination.SubmitFailure)
        },
        onSuccess = {
          navController.navigate(ChooseTierDestination.SubmitSuccess(this.params.activationDate)) {
            typedPopUpTo<ChooseTierDestination.SelectTierAndDeductible> {
              inclusive = true
            }
          }
        },
      )
    }

    navdestination<ChooseTierDestination.SubmitSuccess>(ChooseTierDestination.SubmitSuccess) {
      SubmitTierSuccessScreen(
        activationDate,
        popBackStack = navController::popBackStack,
      )
    }

    navdestination<ChooseTierDestination.SubmitFailure> { _ ->
      SubmitTierFailureScreen(
        popBackStack = navController::popBackStack,
      )
    }
  }
}
