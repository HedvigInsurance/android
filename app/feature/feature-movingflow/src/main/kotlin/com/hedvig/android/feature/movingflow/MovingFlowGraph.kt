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
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractDestination
import com.hedvig.android.feature.movingflow.ui.selectcontract.SelectContractViewModel
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeDestination
import com.hedvig.android.feature.movingflow.ui.start.HousingTypeViewModel
import com.hedvig.android.feature.movingflow.ui.successfulmove.SuccessfulMoveDestination
import com.hedvig.android.feature.movingflow.ui.summary.SummaryDestination
import com.hedvig.android.feature.movingflow.ui.summary.SummaryViewModel
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object MovingFlowGraphDestination : Destination

@Serializable
data object SelectContractForMoving : Destination

internal sealed interface MovingFlowDestinations {
  @Serializable
  data class HousingType(val moveIntentId: String) : MovingFlowDestinations, Destination

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

fun NavGraphBuilder.movingFlowGraph(
  navController: NavController,
  goToChat: () -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navdestination<SelectContractForMoving>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.moveContract),
  ) {
    SelectContractDestination(
      viewModel = metroViewModel<SelectContractViewModel>(),
      navigateUp = navController::navigateUp,
      exitFlow = { navController.typedPopBackStack<SelectContractForMoving>(inclusive = true) },
      onNavigateToNextStep = { moveIntentId, shouldPopUp ->
        navController.navigate(MovingFlowDestinations.HousingType(moveIntentId)) {
          if (shouldPopUp) {
            typedPopUpTo<SelectContractForMoving> {
              inclusive = true
            }
          }
        }
      },
      goToChat = goToChat,
    )
  }

  navgraph<MovingFlowGraphDestination>(
    startDestination = MovingFlowDestinations.HousingType::class,
  ) {
    navdestination<MovingFlowDestinations.HousingType> {
      val moveIntentId = it.toRoute<MovingFlowDestinations.HousingType>().moveIntentId
      HousingTypeDestination(
        viewModel = metroViewModel<HousingTypeViewModel>(),
        navigateUp = navController::navigateUp,
        exitFlow = {
          navController.typedPopBackStack<MovingFlowGraphDestination>(inclusive = true)
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
        },
        onNavigateToNextStep = {
          navController.navigate(EnterNewAddress(moveIntentId))
        },
      )
    }
    navdestination<EnterNewAddress> {
      val moveIntentId = it.toRoute<EnterNewAddress>().moveIntentId
      EnterNewAddressDestination(
        viewModel = metroViewModel<EnterNewAddressViewModel>(),
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        exitFlow = {
          navController.typedPopBackStack<MovingFlowGraphDestination>(inclusive = true)
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
        },
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
        viewModel = metroViewModel<AddHouseInformationViewModel>(),
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        exitFlow = {
          navController.typedPopBackStack<MovingFlowGraphDestination>(inclusive = true)
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
        },
        onNavigateToChoseCoverageLevelAndDeductible = {
          navController.navigate(MovingFlowDestinations.ChoseCoverageLevelAndDeductible(moveIntentId))
        },
      )
    }
    navdestination<MovingFlowDestinations.ChoseCoverageLevelAndDeductible> { backStackEntry ->
      val moveIntentId = backStackEntry.toRoute<MovingFlowDestinations.ChoseCoverageLevelAndDeductible>().moveIntentId
      ChoseCoverageLevelAndDeductibleDestination(
        viewModel = assistedMetroViewModel<
          ChoseCoverageLevelAndDeductibleViewModel,
          ChoseCoverageLevelAndDeductibleViewModel.Factory,
        > {
          create(moveIntentId)
        },
        navigateUp = navController::navigateUp,
        popBackStack = navController::popBackStack,
        exitFlow = {
          navController.typedPopBackStack<MovingFlowGraphDestination>(inclusive = true)
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
        },
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
      val comparisonParameters = this.comparisonParameters
      val viewModel: ComparisonViewModel =
        assistedMetroViewModel<ComparisonViewModel, ComparisonViewModel.Factory> {
          create(comparisonParameters)
        }
      ComparisonDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<MovingFlowDestinations.Summary> { backStackEntry ->
      SummaryDestination(
        viewModel = metroViewModel<SummaryViewModel>(),
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        exitFlow = {
          navController.typedPopBackStack<MovingFlowGraphDestination>(inclusive = true)
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
        },
        onNavigateToFinishedScreen = { moveDate ->
          navController.typedPopBackStack<SelectContractForMoving>(inclusive = true)
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
