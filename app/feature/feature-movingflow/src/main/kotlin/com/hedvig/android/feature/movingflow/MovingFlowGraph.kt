package com.hedvig.android.feature.movingflow

import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.navigation.compose.popBackStack
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
data object SelectContractForMovingKey : HedvigNavKey

@Serializable
internal data class HousingTypeKey(val moveIntentId: String) : HedvigNavKey

@Serializable
internal data class EnterNewAddressKey(val moveIntentId: String) : HedvigNavKey

@Serializable
internal data class AddHouseInformationKey(
  val moveIntentId: String,
) : HedvigNavKey

@Serializable
internal data class ChoseCoverageLevelAndDeductibleKey(
  val moveIntentId: String,
) : HedvigNavKey

@Serializable
internal data class CompareCoverageKey(val comparisonParameters: ComparisonParameters) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<ComparisonParameters>())
  }
}

@Serializable
internal data class SummaryKey(
  val moveIntentId: String,
  val homeQuoteId: String,
) : HedvigNavKey

@Serializable
internal data class SuccessfulMoveKey(
  val moveDate: LocalDate,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<LocalDate>())
  }
}

fun EntryProviderScope<HedvigNavKey>.movingFlowGraph(backStack: MutableList<HedvigNavKey>, goToChat: () -> Unit) {
  entry<SelectContractForMovingKey> {
    SelectContractDestination(
      viewModel = metroViewModel<SelectContractViewModel>(),
      navigateUp = backStack::navigateUp,
      exitFlow = { backStack.popUpTo<SelectContractForMovingKey>(inclusive = true) },
      onNavigateToNextStep = { moveIntentId, shouldPopUp ->
        if (shouldPopUp) {
          backStack.navigateAndPopUpTo<SelectContractForMovingKey>(
            HousingTypeKey(moveIntentId),
            inclusive = true,
          )
        } else {
          backStack.add(HousingTypeKey(moveIntentId))
        }
      },
      goToChat = goToChat,
    )
  }

  entry<HousingTypeKey> { key ->
    val moveIntentId = key.moveIntentId
    HousingTypeDestination(
      viewModel = metroViewModel<HousingTypeViewModel>(),
      navigateUp = backStack::navigateUp,
      exitFlow = { backStack.exitMovingFlow() },
      onNavigateToNextStep = {
        backStack.add(EnterNewAddressKey(moveIntentId))
      },
    )
  }
  entry<EnterNewAddressKey> { key ->
    val moveIntentId = key.moveIntentId
    EnterNewAddressDestination(
      viewModel = assistedMetroViewModel<EnterNewAddressViewModel, EnterNewAddressViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = backStack::navigateUp,
      popBackStack = backStack::popBackStack,
      exitFlow = { backStack.exitMovingFlow() },
      onNavigateToAddHouseInformation = {
        backStack.add(AddHouseInformationKey(moveIntentId))
      },
      onNavigateToChoseCoverageLevelAndDeductible = {
        backStack.add(ChoseCoverageLevelAndDeductibleKey(moveIntentId))
      },
    )
  }
  entry<AddHouseInformationKey> { key ->
    val moveIntentId = key.moveIntentId
    AddHouseInformationDestination(
      viewModel = assistedMetroViewModel<AddHouseInformationViewModel, AddHouseInformationViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = backStack::navigateUp,
      popBackStack = backStack::popBackStack,
      exitFlow = { backStack.exitMovingFlow() },
      onNavigateToChoseCoverageLevelAndDeductible = {
        backStack.add(ChoseCoverageLevelAndDeductibleKey(moveIntentId))
      },
    )
  }
  entry<ChoseCoverageLevelAndDeductibleKey> { key ->
    val moveIntentId = key.moveIntentId
    ChoseCoverageLevelAndDeductibleDestination(
      viewModel = assistedMetroViewModel<
        ChoseCoverageLevelAndDeductibleViewModel,
        ChoseCoverageLevelAndDeductibleViewModel.Factory,
      > {
        create(moveIntentId)
      },
      navigateUp = backStack::navigateUp,
      popBackStack = backStack::popBackStack,
      exitFlow = { backStack.exitMovingFlow() },
      onNavigateToSummaryScreen = { homeQuoteId ->
        backStack.add(SummaryKey(moveIntentId, homeQuoteId))
      },
      navigateToComparison = { parameters ->
        backStack.add(CompareCoverageKey(parameters))
      },
    )
  }

  entry<CompareCoverageKey> { key ->
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
    val summaryRoute = key
    SummaryDestination(
      viewModel = assistedMetroViewModel<SummaryViewModel, SummaryViewModel.Factory> {
        create(summaryRoute)
      },
      navigateUp = backStack::navigateUp,
      navigateBack = backStack::popBackStack,
      exitFlow = { backStack.exitMovingFlow() },
      onNavigateToFinishedScreen = { moveDate ->
        backStack.popUpTo<SelectContractForMovingKey>(inclusive = true)
        backStack.navigateAndPopUpTo<HousingTypeKey>(
          SuccessfulMoveKey(moveDate),
          inclusive = true,
        )
      },
    )
  }

  entry<SuccessfulMoveKey> { key ->
    SuccessfulMoveDestination(
      moveDate = key.moveDate,
      navigateUp = backStack::navigateUp,
      popBackStack = backStack::popBackStack,
    )
  }
}

/**
 * Exits the moving flow regardless of which entry seeded it: the flow is rooted either at
 * [SelectContractForMovingKey] (deep-link entry) or at [HousingTypeKey] (direct
 * entry), so both are popped inclusively to leave nothing behind.
 */
private fun MutableList<HedvigNavKey>.exitMovingFlow() {
  popUpTo<HousingTypeKey>(inclusive = true)
  popUpTo<SelectContractForMovingKey>(inclusive = true)
}
