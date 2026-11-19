package com.hedvig.android.feature.greencard.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.raise.either
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GetGreenCardsUseCase
import com.hedvig.android.feature.greencard.data.GreenCard
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class GreenCardHistoryViewModel(
  getGreenCardsUseCase: GetGreenCardsUseCase,
  getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  downloadPdfUseCase: DownloadPdfUseCase,
  featureManager: FeatureManager,
) : MoleculeViewModel<GreenCardHistoryEvent, GreenCardHistoryUiState>(
    initialState = GreenCardHistoryUiState.Loading,
    presenter = GreenCardHistoryPresenter(
      getGreenCardsUseCase,
      getGreenCardSpecificationsUseCase,
      downloadPdfUseCase,
      featureManager,
    ),
  )

internal class GreenCardHistoryPresenter(
  private val getGreenCardsUseCase: GetGreenCardsUseCase,
  private val getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  private val downloadPdfUseCase: DownloadPdfUseCase,
  private val featureManager: FeatureManager,
) : MoleculePresenter<GreenCardHistoryEvent, GreenCardHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<GreenCardHistoryEvent>.present(
    lastState: GreenCardHistoryUiState,
  ): GreenCardHistoryUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var content by remember {
      mutableStateOf(
        (lastState as? GreenCardHistoryUiState.Success)?.let {
          HistoryContent.Success(it.greenCards, it.eligibleContractIds)
        } ?: HistoryContent.Loading,
      )
    }
    var downloadingUrl by remember { mutableStateOf<String?>(null) }
    var downloadedFile by remember { mutableStateOf<DownloadedFile?>(null) }
    var showDownloadError by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        GreenCardHistoryEvent.RetryLoadData -> dataLoadIteration++
        is GreenCardHistoryEvent.DownloadGreenCard -> downloadingUrl = event.signedUrl
        GreenCardHistoryEvent.HaveProcessedGreenCardFile -> downloadedFile = null
        GreenCardHistoryEvent.DismissDownloadError -> showDownloadError = false
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (content !is HistoryContent.Success) {
        content = HistoryContent.Loading
      }
      // A deep link reaches this screen without passing an entry point, so the flag is checked here too.
      if (!featureManager.isFeatureEnabled(Feature.ENABLE_GREEN_CARD).first()) {
        content = HistoryContent.NotAvailable
        return@LaunchedEffect
      }
      content = either {
        val greenCards = getGreenCardsUseCase.invoke().bind()
        val specifications = getGreenCardSpecificationsUseCase.invoke().bind()
        HistoryContent.Success(greenCards, specifications.contracts.map { it.contractId })
      }.fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Could not load green cards: $it" }
          HistoryContent.Failure
        },
        ifRight = { it },
      )
    }

    LaunchedEffect(downloadingUrl) {
      val url = downloadingUrl ?: return@LaunchedEffect
      downloadPdfUseCase.invoke(url).fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Downloading green card failed: $it" }
          showDownloadError = true
        },
        ifRight = { downloadedFile = it },
      )
      downloadingUrl = null
    }

    return when (val currentContent = content) {
      HistoryContent.Loading -> GreenCardHistoryUiState.Loading

      HistoryContent.Failure -> GreenCardHistoryUiState.Failure

      HistoryContent.NotAvailable -> GreenCardHistoryUiState.NotAvailable

      is HistoryContent.Success -> GreenCardHistoryUiState.Success(
        greenCards = currentContent.greenCards,
        eligibleContractIds = currentContent.eligibleContractIds,
        isDownloadingGreenCard = downloadingUrl != null,
        showDownloadError = showDownloadError,
        downloadedGreenCard = downloadedFile,
      )
    }
  }
}

private sealed interface HistoryContent {
  data object Loading : HistoryContent

  data object Failure : HistoryContent

  data object NotAvailable : HistoryContent

  data class Success(val greenCards: List<GreenCard>, val eligibleContractIds: List<String>) : HistoryContent
}

internal sealed interface GreenCardHistoryEvent {
  data object RetryLoadData : GreenCardHistoryEvent

  data class DownloadGreenCard(val signedUrl: String) : GreenCardHistoryEvent

  data object HaveProcessedGreenCardFile : GreenCardHistoryEvent

  data object DismissDownloadError : GreenCardHistoryEvent
}

internal sealed interface GreenCardHistoryUiState {
  data object Loading : GreenCardHistoryUiState

  data object Failure : GreenCardHistoryUiState

  data object NotAvailable : GreenCardHistoryUiState

  data class Success(
    val greenCards: List<GreenCard>,
    val eligibleContractIds: List<String>,
    val isDownloadingGreenCard: Boolean,
    val showDownloadError: Boolean,
    val downloadedGreenCard: DownloadedFile?,
  ) : GreenCardHistoryUiState
}
