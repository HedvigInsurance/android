package com.hedvig.android.feature.change.tier.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceToChangeTierDestination
import com.hedvig.android.feature.change.tier.ui.chooseinsurance.ChooseInsuranceViewModel
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonDestination
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectTierDestination
import com.hedvig.android.feature.change.tier.ui.stepstart.StartChangeTierFlowDestination
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierFlowViewModel
import com.hedvig.android.feature.change.tier.ui.stepsummary.ChangeTierSummaryDestination
import com.hedvig.android.feature.change.tier.ui.stepsummary.SubmitTierFailureScreen
import com.hedvig.android.feature.change.tier.ui.stepsummary.SubmitTierSuccessScreen
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeTierGraph(navigator: Navigator, navController: NavController, applicationId: String) {
  navdestination<StartTierFlowDestination> { _ ->
    val viewModel: StartTierFlowViewModel = koinViewModel {
      parametersOf(this.insuranceId)
    }
    StartChangeTierFlowDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      launchFlow = { params: InsuranceCustomizationParameters ->
        navigator.navigateUnsafe(ChooseTierGraphDestination(params)) {
          typedPopUpTo<StartTierFlowDestination> {
            inclusive = true
          }
        }
      },
    )
  }

  navdestination<StartTierFlowChooseInsuranceDestination> {
    val viewModel: ChooseInsuranceViewModel = koinViewModel()
    ChooseInsuranceToChangeTierDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      navigateToNextStep = { params: InsuranceCustomizationParameters ->
        navigator.navigateUnsafe(ChooseTierGraphDestination(params)) {
          typedPopUpTo<StartTierFlowChooseInsuranceDestination> {
            inclusive = true
          }
        }
      },
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
        navigateUp = navigator::navigateUp,
        navigateToSummary = { quote ->
          navigator.navigateUnsafe(
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
          navigator.popBackStack()
        },
        navigateToComparison = { listOfQuotes ->
          navigator.navigateUnsafe(
            ChooseTierDestination.Comparison(
              listOfQuotes.map {
                it.productVariant.termsVersion
              },
            ),
          )
        },
      )
    }

    navdestination<ChooseTierDestination.Comparison> { _ ->
      val viewModel: ComparisonViewModel = koinViewModel {
        parametersOf(this.quoteIds)
      }
      ComparisonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<ChooseTierDestination.Summary>(
      destinationNavTypeAware = ChooseTierDestination.Summary.Companion,
    ) { backStackEntry ->
      val viewModel: SummaryViewModel = koinViewModel {
        parametersOf(this.params)
      }
      val context = LocalContext.current
      ChangeTierSummaryDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onFailure = {
          navigator.navigateUnsafe(ChooseTierDestination.SubmitFailure)
        },
        sharePdf = {
          context.sharePDF(it, applicationId)
        },
        onSuccess = {
          navigator.navigateUnsafe(ChooseTierDestination.SubmitSuccess(this.params.activationDate)) {
            typedPopUpTo<ChooseTierDestination.SelectTierAndDeductible> {
              inclusive = true
            }
          }
        },
      )
    }

    navdestination<ChooseTierDestination.SubmitSuccess>(ChooseTierDestination.SubmitSuccess) { backStackEntry ->
      SubmitTierSuccessScreen(
        activationDate,
        popBackStack = navigator::popBackStack,
      )
    }

    navdestination<ChooseTierDestination.SubmitFailure> { _ ->
      SubmitTierFailureScreen(
        popBackStack = navigator::popBackStack,
      )
    }
  }
}
