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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonDestination
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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
internal data class CompareCoverageKey(val comparisonParameters: ComparisonParameters) : HedvigNavKey

@Serializable
internal data class SummaryKey(
  val moveIntentId: String,
  val homeQuoteId: String,
) : HedvigNavKey

@Serializable
internal data class SuccessfulMoveKey(
  val moveDate: LocalDate,
) : HedvigNavKey

fun EntryProviderScope<HedvigNavKey>.movingFlowEntries(backstack: Backstack, goToChat: () -> Unit) {
  entry<SelectContractForMovingKey> {
    SelectContractDestination(
      viewModel = metroViewModel<SelectContractViewModel>(),
      navigateUp = backstack::navigateUp,
      exitFlow = { backstack.popUpTo<SelectContractForMovingKey>(inclusive = true) },
      goToChat = goToChat,
    )
  }

  entry<HousingTypeKey> { key ->
    HousingTypeDestination(
      viewModel = assistedMetroViewModel<HousingTypeViewModel, HousingTypeViewModel.Factory> {
        create(key.moveIntentId)
      },
      navigateUp = backstack::navigateUp,
      exitFlow = { backstack.exitMovingFlow() },
    )
  }
  entry<EnterNewAddressKey> { key ->
    val moveIntentId = key.moveIntentId
    EnterNewAddressDestination(
      viewModel = assistedMetroViewModel<EnterNewAddressViewModel, EnterNewAddressViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
      exitFlow = { backstack.exitMovingFlow() },
    )
  }
  entry<AddHouseInformationKey> { key ->
    val moveIntentId = key.moveIntentId
    AddHouseInformationDestination(
      viewModel = assistedMetroViewModel<AddHouseInformationViewModel, AddHouseInformationViewModel.Factory> {
        create(moveIntentId)
      },
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
      exitFlow = { backstack.exitMovingFlow() },
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
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
      exitFlow = { backstack.exitMovingFlow() },
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
      navigateUp = backstack::navigateUp,
    )
  }

  entry<SummaryKey> { key ->
    val summaryRoute = key
    SummaryDestination(
      viewModel = assistedMetroViewModel<SummaryViewModel, SummaryViewModel.Factory> {
        create(summaryRoute)
      },
      navigateUp = backstack::navigateUp,
      navigateBack = backstack::popBackstack,
      exitFlow = { backstack.exitMovingFlow() },
    )
  }

  entry<SuccessfulMoveKey> { key ->
    SuccessfulMoveDestination(
      moveDate = key.moveDate,
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
    )
  }
}

/**
 * Exits the moving flow regardless of which entry seeded it: the flow is rooted either at
 * [SelectContractForMovingKey] (deep-link entry) or at [HousingTypeKey] (direct
 * entry), so both are popped inclusively to leave nothing behind.
 */
private fun Backstack.exitMovingFlow() {
  popUpTo<HousingTypeKey>(inclusive = true)
  popUpTo<SelectContractForMovingKey>(inclusive = true)
}
