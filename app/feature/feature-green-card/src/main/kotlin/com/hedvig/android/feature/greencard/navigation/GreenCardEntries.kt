package com.hedvig.android.feature.greencard.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.greencard.ui.choose.ChooseContractForGreenCardDestination
import com.hedvig.android.feature.greencard.ui.choose.ChooseContractForGreenCardViewModel
import com.hedvig.android.feature.greencard.ui.dateinput.GreenCardDateInputDestination
import com.hedvig.android.feature.greencard.ui.dateinput.GreenCardDateInputViewModel
import com.hedvig.android.feature.greencard.ui.dateinput.GreenCardDateInputViewModelFactory
import com.hedvig.android.feature.greencard.ui.history.GreenCardHistoryDestination
import com.hedvig.android.feature.greencard.ui.history.GreenCardHistoryEvent
import com.hedvig.android.feature.greencard.ui.history.GreenCardHistoryViewModel
import com.hedvig.android.feature.greencard.ui.overview.GreenCardOverviewDestination
import com.hedvig.android.feature.greencard.ui.overview.GreenCardOverviewViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.greenCardEntries(backstack: Backstack, applicationId: String) {
  entry<GreenCardKey> {
    val viewModel: GreenCardHistoryViewModel = metroViewModel()
    val context = LocalContext.current
    GreenCardHistoryDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onStartGenerateGreenCardFlow = { contractId -> backstack.add(GreenCardDateInputKey(contractId)) },
      onNavigateToChooseContract = { backstack.add(GreenCardChooseContractKey) },
      onShareGreenCard = {
        viewModel.emit(GreenCardHistoryEvent.HaveProcessedGreenCardFile)
        context.sharePDF(it, applicationId)
      },
    )
  }

  entry<GreenCardChooseContractKey> {
    val viewModel: ChooseContractForGreenCardViewModel = metroViewModel()
    ChooseContractForGreenCardDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onContinue = { contractId -> backstack.add(GreenCardDateInputKey(contractId)) },
    )
  }

  entry<GreenCardDateInputKey> { key ->
    val viewModel: GreenCardDateInputViewModel =
      assistedMetroViewModel<GreenCardDateInputViewModel, GreenCardDateInputViewModelFactory> {
        create(key.contractId)
      }
    GreenCardDateInputDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<GreenCardCreatedKey> { key ->
    val viewModel: GreenCardOverviewViewModel = metroViewModel()
    val context = LocalContext.current
    GreenCardOverviewDestination(
      greenCardUrl = key.greenCardUrl,
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onDone = backstack::popBackstack,
      onShareGreenCard = { context.sharePDF(it, applicationId) },
    )
  }
}
