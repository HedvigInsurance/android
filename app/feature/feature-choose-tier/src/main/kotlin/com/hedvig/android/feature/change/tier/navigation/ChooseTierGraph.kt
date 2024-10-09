package com.hedvig.android.feature.change.tier.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.common.android.sharePDF
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
import com.hedvig.android.feature.change.tier.ui.sucess.SuccessScreen
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.Navigator
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeTierGraph(
  navigator: Navigator,
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  applicationId: String,
) {
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

  navgraph<ChooseTierGraphDestination>(
    startDestination = ChooseTierDestination.SelectTierAndDeductible::class,
    destinationNavTypeAware = object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<InsuranceCustomizationParameters>())
    },
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
          navigator.navigateUnsafe(ChooseTierDestination.Summary(
            SummaryParameters(
              quoteIdToSubmit = quote.id,
              activationDateEpochDays = chooseTierGraphDestination.parameters.activationDateEpochDays,
              insuranceId = chooseTierGraphDestination.parameters.insuranceId)

          ))
        },
        navigateToComparison = { listOfQuotes ->
          navigator.navigateUnsafe(ChooseTierDestination.Comparison(listOfQuotes.map { it.id }))
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

    navdestination<ChooseTierDestination.Summary> { backStackEntry ->
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
          navigator.navigateUnsafe(ChooseTierDestination.SubmitSuccess(this.params.activationDateEpochDays)) {
            typedPopUpTo<ChooseTierDestination.Summary> {
              inclusive = true
            }
          }
        },
      )
    }

    navdestination<ChooseTierDestination.SubmitSuccess> { backStackEntry ->
      SubmitTierSuccessScreen(
        activationDate,
        navigateUp = {
          // todo: we don't need to pop up anything more here, right?
          //  we did it before in ChooseTierDestination.Summary
          navigator.navigateUp()
        },
      )
    }

    navdestination<ChooseTierDestination.SubmitFailure> { _ ->
      SubmitTierFailureScreen(
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
