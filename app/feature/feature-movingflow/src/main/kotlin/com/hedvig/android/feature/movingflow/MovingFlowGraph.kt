package com.hedvig.android.feature.movingflow

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.feature.movingflow.MovingFlowDestinations.EnterNewAddress
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationDestination
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationViewModel
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleDestination
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleViewModel
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressDestination
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressViewModel
import com.hedvig.android.feature.movingflow.ui.start.StartDestination
import com.hedvig.android.feature.movingflow.ui.start.StartViewModel
import com.hedvig.android.feature.movingflow.ui.successfulmove.SuccessfulMoveDestination
import com.hedvig.android.feature.movingflow.ui.summary.SummaryDestination
import com.hedvig.android.feature.movingflow.ui.summary.SummaryViewModel
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object MovingFlowGraphDestination : Destination

internal sealed interface MovingFlowDestinations {
  @Serializable
  data object Start : MovingFlowDestinations, Destination

  @Serializable
  data class EnterNewAddress(val moveIntentId: String) : MovingFlowDestinations, Destination

  @Serializable
  data class AddHouseInformation(
    val moveIntentId: String,
  ) : MovingFlowDestinations, Destination

  @Serializable
  data class ChoseCoverageLevelAndDeductible(
    val moveIntentId: String,
  ) : MovingFlowDestinations, Destination

  @Serializable
  data class CompareCoverage(val comparisonParameters: ComparisonParameters) : MovingFlowDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<ComparisonParameters>())
    }
  }

  @Serializable
  data class Summary(
    val moveIntentId: String,
    val homeQuoteId: String,
  ) : MovingFlowDestinations, Destination

  @Serializable
  data class SuccessfulMove(
    val moveDate: LocalDate,
  ) : MovingFlowDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }
}

fun NavGraphBuilder.movingFlowGraph(navController: NavController, onNavigateToNewConversation: () -> Unit) {
  navgraph<MovingFlowGraphDestination>(
    startDestination = MovingFlowDestinations.Start::class,
  ) {
    navdestination<MovingFlowDestinations.Start> {
      StartDestination(
        viewModel = koinViewModel<StartViewModel>(),
        navigateUp = navController::navigateUp,
        onNavigateToNextStep = { moveIntentId ->
          navController.navigate(EnterNewAddress(moveIntentId))
        },
      )
    }
    navdestination<MovingFlowDestinations.EnterNewAddress> {
      val moveIntentId = it.toRoute<MovingFlowDestinations.EnterNewAddress>().moveIntentId
      EnterNewAddressDestination(
        viewModel = koinViewModel<EnterNewAddressViewModel>(),
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        onNavigateToAddHouseInformation = {
          navController.navigate(MovingFlowDestinations.AddHouseInformation(moveIntentId))
        },
        onNavigateToChoseCoverageLevelAndDeductible = {
          navController.navigate(MovingFlowDestinations.ChoseCoverageLevelAndDeductible(moveIntentId))
        },
      )
    }
    navdestination<MovingFlowDestinations.AddHouseInformation> {
      AddHouseInformationDestination(
        viewModel = koinViewModel<AddHouseInformationViewModel>(),
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        onNavigateToChoseCoverageLevelAndDeductible = {
          navController.navigate(MovingFlowDestinations.ChoseCoverageLevelAndDeductible(moveIntentId))
        },
      )
    }
    navdestination<MovingFlowDestinations.ChoseCoverageLevelAndDeductible> { backStackEntry ->
      val moveIntentId = backStackEntry.toRoute<MovingFlowDestinations.ChoseCoverageLevelAndDeductible>().moveIntentId
      ChoseCoverageLevelAndDeductibleDestination(
        viewModel = koinViewModel<ChoseCoverageLevelAndDeductibleViewModel>(),
        navigateUp = navController::navigateUp,
        onNavigateToSummaryScreen = { homeQuoteId ->
          navController.navigate(MovingFlowDestinations.Summary(moveIntentId, homeQuoteId))
        },
        navigateToComparison = { parameters ->
          navController.navigate(MovingFlowDestinations.CompareCoverage(parameters))
        },
      )
    }

    navdestination<MovingFlowDestinations.CompareCoverage>(
      destinationNavTypeAware = MovingFlowDestinations.CompareCoverage.Companion,
    ) { _ ->
      val viewModel: ComparisonViewModel = koinViewModel {
        parametersOf(this.comparisonParameters)
      }
      ComparisonDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<MovingFlowDestinations.Summary> {
      SummaryDestination(
        viewModel = koinViewModel<SummaryViewModel>(),
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        onNavigateToNewConversation = onNavigateToNewConversation,
        onNavigateToFinishedScreen = { moveDate ->
          navController.navigate(MovingFlowDestinations.SuccessfulMove(moveDate)) {
            typedPopUpTo<MovingFlowGraphDestination> {
              inclusive = true
            }
          }
        },
      )
    }
  }
  navdestination<MovingFlowDestinations.SuccessfulMove>(
    MovingFlowDestinations.SuccessfulMove,
  ) { backStackEntry ->
    SuccessfulMoveDestination(
      moveDate = backStackEntry.toRoute<MovingFlowDestinations.SuccessfulMove>().moveDate,
      navigateUp = navController::navigateUp,
      popBackStack = navController::popBackStack,
    )
  }
}
