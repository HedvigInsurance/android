package com.hedvig.android.feature.movingflow

import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
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

fun EntryProviderScope<Destination>.movingFlowGraph(navigator: Navigator, goToChat: () -> Unit) {
  navdestination<SelectContractForMoving> {
    SelectContractDestination(
      viewModel = metroViewModel<SelectContractViewModel>(),
      navigateUp = navigator::navigateUp,
      exitFlow = { navigator.popUpTo<SelectContractForMoving>(inclusive = true) },
      onNavigateToNextStep = { moveIntentId, shouldPopUp ->
        if (shouldPopUp) {
          navigator.navigate<SelectContractForMoving>(
            MovingFlowDestinations.HousingType(moveIntentId),
            inclusive = true,
          )
        } else {
          navigator.navigate(MovingFlowDestinations.HousingType(moveIntentId))
        }
      },
      goToChat = goToChat,
    )
  }

  navdestination<MovingFlowDestinations.HousingType> {
    val moveIntentId = this.moveIntentId
    HousingTypeDestination(
      viewModel = metroViewModel<HousingTypeViewModel>(),
      navigateUp = navigator::navigateUp,
      exitFlow = { navigator.exitMovingFlow() },
      onNavigateToNextStep = {
        navigator.navigate(EnterNewAddress(moveIntentId))
      },
    )
  }
  navdestination<EnterNewAddress> {
    val moveIntentId = this.moveIntentId
    EnterNewAddressDestination(
      viewModel = assistedMetroViewModel<EnterNewAddressViewModel, EnterNewAddressViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = navigator::navigateUp,
      popBackStack = navigator::popBackStack,
      exitFlow = { navigator.exitMovingFlow() },
      onNavigateToAddHouseInformation = {
        navigator.navigate(MovingFlowDestinations.AddHouseInformation(moveIntentId))
      },
      onNavigateToChoseCoverageLevelAndDeductible = {
        navigator.navigate(MovingFlowDestinations.ChoseCoverageLevelAndDeductible(moveIntentId))
      },
    )
  }
  navdestination<MovingFlowDestinations.AddHouseInformation> {
    val moveIntentId = this.moveIntentId
    AddHouseInformationDestination(
      viewModel = assistedMetroViewModel<AddHouseInformationViewModel, AddHouseInformationViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = navigator::navigateUp,
      popBackStack = navigator::popBackStack,
      exitFlow = { navigator.exitMovingFlow() },
      onNavigateToChoseCoverageLevelAndDeductible = {
        navigator.navigate(MovingFlowDestinations.ChoseCoverageLevelAndDeductible(moveIntentId))
      },
    )
  }
  navdestination<MovingFlowDestinations.ChoseCoverageLevelAndDeductible> {
    val moveIntentId = this.moveIntentId
    ChoseCoverageLevelAndDeductibleDestination(
      viewModel = assistedMetroViewModel<
        ChoseCoverageLevelAndDeductibleViewModel,
        ChoseCoverageLevelAndDeductibleViewModel.Factory,
      > {
        create(moveIntentId)
      },
      navigateUp = navigator::navigateUp,
      popBackStack = navigator::popBackStack,
      exitFlow = { navigator.exitMovingFlow() },
      onNavigateToSummaryScreen = { homeQuoteId ->
        navigator.navigate(MovingFlowDestinations.Summary(moveIntentId, homeQuoteId))
      },
      navigateToComparison = { parameters ->
        navigator.navigate(MovingFlowDestinations.CompareCoverage(parameters))
      },
    )
  }

  navdestination<MovingFlowDestinations.CompareCoverage> {
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

  navdestination<MovingFlowDestinations.Summary> {
    val summaryRoute = this
    SummaryDestination(
      viewModel = assistedMetroViewModel<SummaryViewModel, SummaryViewModel.Factory> {
        create(summaryRoute)
      },
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
      exitFlow = { navigator.exitMovingFlow() },
      onNavigateToFinishedScreen = { moveDate ->
        navigator.popUpTo<SelectContractForMoving>(inclusive = true)
        navigator.navigate<MovingFlowDestinations.HousingType>(
          MovingFlowDestinations.SuccessfulMove(moveDate),
          inclusive = true,
        )
      },
    )
  }

  navdestination<MovingFlowDestinations.SuccessfulMove> {
    SuccessfulMoveDestination(
      moveDate = moveDate,
      navigateUp = navigator::navigateUp,
      popBackStack = navigator::popBackStack,
    )
  }
}

/**
 * Exits the moving flow regardless of which entry seeded it: the flow is rooted either at
 * [SelectContractForMoving] (deep-link entry) or at [MovingFlowDestinations.HousingType] (direct
 * entry), so both are popped inclusively to leave nothing behind.
 */
private fun Navigator.exitMovingFlow() {
  popUpTo<MovingFlowDestinations.HousingType>(inclusive = true)
  popUpTo<SelectContractForMoving>(inclusive = true)
}
