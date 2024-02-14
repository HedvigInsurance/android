package com.hedvig.android.feature.travelcertificate.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class TravelCertificateOverviewViewModel(
  downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
) :
  MoleculeViewModel<TravelCertificateOverviewEvent, TravelCertificateOverviewUiState>(
      initialState = TravelCertificateOverviewUiState.Loading,
      presenter = TravelCertificateOverviewPresenter(downloadTravelCertificateUseCase),
    )

internal class TravelCertificateOverviewPresenter(
  private val downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
) : MoleculePresenter<TravelCertificateOverviewEvent, TravelCertificateOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateOverviewEvent>.present(
    lastState: TravelCertificateOverviewUiState,
  ): TravelCertificateOverviewUiState {
    var currentState by remember {
      mutableStateOf<TravelCertificateOverviewUiState>(TravelCertificateOverviewUiState.Loading)
    }

    var dataLoadIteration by remember { mutableIntStateOf(0) }

    var travelCertificateUrl by remember { mutableStateOf<TravelCertificateUrl?>(null) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateOverviewEvent.RetryLoadData -> {
          dataLoadIteration++
        }

        is TravelCertificateOverviewEvent.OnDownloadCertificate -> {
          travelCertificateUrl = event.travelCertificateUrl
          dataLoadIteration++
        }
      }
    }

    LaunchedEffect(dataLoadIteration) {
      val url = travelCertificateUrl
      if (url != null) {
        currentState = TravelCertificateOverviewUiState.Loading
        logcat(LogPriority.INFO) { "Downloading travel certificate with url:${url.uri}" }
        downloadTravelCertificateUseCase.invoke(url)
          .fold(
            ifLeft = { errorMessage ->
              logcat(LogPriority.ERROR) { "Downloading travel certificate failed:$errorMessage" }
              currentState = TravelCertificateOverviewUiState.Failure
            },
            ifRight = { uri ->
              logcat(
                LogPriority.INFO,
              ) { "Downloading travel certificate succeeded. Result uri:${uri.uri.absolutePath}" }
              currentState = TravelCertificateOverviewUiState.Success(uri)
            },
          )
      } else {
        currentState = TravelCertificateOverviewUiState.Success(null)
      }
    }
    return currentState
  }
}

internal sealed interface TravelCertificateOverviewEvent {
  data object RetryLoadData : TravelCertificateOverviewEvent

  data class OnDownloadCertificate(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateOverviewEvent
}

internal sealed interface TravelCertificateOverviewUiState {
  data object Loading : TravelCertificateOverviewUiState

  data object Failure : TravelCertificateOverviewUiState

  data class Success(
    val travelCertificateUri: TravelCertificateUri?,
  ) : TravelCertificateOverviewUiState
}
