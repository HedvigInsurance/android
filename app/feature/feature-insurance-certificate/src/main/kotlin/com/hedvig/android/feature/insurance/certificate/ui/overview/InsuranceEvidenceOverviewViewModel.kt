package com.hedvig.android.feature.insurance.certificate.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewState.Loading
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import java.io.File

internal class InsuranceEvidenceOverviewViewModel(
  downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculeViewModel<InsuranceEvidenceOverviewEvent, InsuranceEvidenceOverviewState>(
    initialState = Loading,
    presenter = InsuranceEvidenceOverviewPresenter(downloadPdfUseCase),
  )

internal class InsuranceEvidenceOverviewPresenter(
  private val downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculePresenter<InsuranceEvidenceOverviewEvent, InsuranceEvidenceOverviewState> {
  @Composable
  override fun MoleculePresenterScope<InsuranceEvidenceOverviewEvent>.present(
    lastState: InsuranceEvidenceOverviewState,
  ): InsuranceEvidenceOverviewState {
    var currentState by remember {
      mutableStateOf<InsuranceEvidenceOverviewState>(InsuranceEvidenceOverviewState.Loading)
    }

    var dataLoadIteration by remember { mutableIntStateOf(0) }

    var certificateUrl by remember { mutableStateOf<String?>(null) }

    CollectEvents { event ->
      when (event) {
        InsuranceEvidenceOverviewEvent.RetryLoadData -> {
          dataLoadIteration++
        }

        is InsuranceEvidenceOverviewEvent.OnDownloadCertificate -> {
          certificateUrl = event.url
        }
      }
    }

    LaunchedEffect(dataLoadIteration, certificateUrl) {
      val url = certificateUrl
      if (url != null) {
        currentState = InsuranceEvidenceOverviewState.Loading
        logcat(LogPriority.INFO) { "Downloading insurance evidence with url:$url" }
        downloadPdfUseCase.invoke(url)
          .fold(
            ifLeft = { errorMessage ->
              logcat(LogPriority.ERROR) { "Downloading insurance evidence failed:$errorMessage" }
              currentState = InsuranceEvidenceOverviewState.Failure
            },
            ifRight = { uri ->
              logcat(
                LogPriority.INFO,
              ) { "Downloading insurance evidence succeeded. Result uri:${uri.absolutePath}" }
              currentState = InsuranceEvidenceOverviewState.Success(uri)
            },
          )
      } else {
        currentState = InsuranceEvidenceOverviewState.Success(null)
      }
    }
    return currentState
  }
}

internal sealed interface InsuranceEvidenceOverviewState {
  data object Loading : InsuranceEvidenceOverviewState

  data object Failure : InsuranceEvidenceOverviewState

  data class Success(
    val insuranceEvidenceUri: File?,
  ) : InsuranceEvidenceOverviewState
}

internal sealed interface InsuranceEvidenceOverviewEvent {
  data class OnDownloadCertificate(val url: String) : InsuranceEvidenceOverviewEvent

  data object RetryLoadData : InsuranceEvidenceOverviewEvent
}
