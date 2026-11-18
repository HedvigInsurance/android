package com.hedvig.android.feature.greencard.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.DownloadedFile
import com.hedvig.android.feature.greencard.data.GreenCardUrl
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class GreenCardOverviewViewModel(
  downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculeViewModel<GreenCardOverviewEvent, GreenCardOverviewUiState>(
    initialState = GreenCardOverviewUiState.Success(null),
    presenter = GreenCardOverviewPresenter(downloadPdfUseCase),
  )

internal class GreenCardOverviewPresenter(
  private val downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculePresenter<GreenCardOverviewEvent, GreenCardOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<GreenCardOverviewEvent>.present(
    lastState: GreenCardOverviewUiState,
  ): GreenCardOverviewUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var downloadIteration by remember { mutableIntStateOf(0) }
    var greenCardUrl by remember { mutableStateOf<GreenCardUrl?>(null) }

    CollectEvents { event ->
      when (event) {
        is GreenCardOverviewEvent.DownloadGreenCard -> {
          greenCardUrl = event.greenCardUrl
          downloadIteration++
        }
      }
    }

    LaunchedEffect(downloadIteration) {
      val url = greenCardUrl ?: return@LaunchedEffect
      currentState = GreenCardOverviewUiState.Loading
      currentState = downloadPdfUseCase.invoke(url.uri).fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Downloading green card failed: $it" }
          GreenCardOverviewUiState.Failure
        },
        ifRight = { GreenCardOverviewUiState.Success(it) },
      )
    }
    return currentState
  }
}

internal sealed interface GreenCardOverviewEvent {
  data class DownloadGreenCard(val greenCardUrl: GreenCardUrl) : GreenCardOverviewEvent
}

internal sealed interface GreenCardOverviewUiState {
  data object Loading : GreenCardOverviewUiState

  data object Failure : GreenCardOverviewUiState

  data class Success(val downloadedGreenCard: DownloadedFile?) : GreenCardOverviewUiState
}
