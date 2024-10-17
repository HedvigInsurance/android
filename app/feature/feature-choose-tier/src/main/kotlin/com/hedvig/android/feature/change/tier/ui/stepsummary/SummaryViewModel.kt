package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ClearNavigation
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.DownLoadFromUrl
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.HandledSharingPdfFile
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.SubmitQuote
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import java.io.File

internal class SummaryViewModel(
  params: SummaryParameters,
  tierRepository: ChangeTierRepository,
  downloadPdfUseCase: DownloadPdfUseCase,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculeViewModel<SummaryEvent, SummaryState>(
    initialState = Loading,
    presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepository,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
      downloadPdfUseCase = downloadPdfUseCase,
    ),
  )

private class SummaryPresenter(
  private val params: SummaryParameters,
  private val tierRepository: ChangeTierRepository,
  private val downloadPdfUseCase: DownloadPdfUseCase,
  private val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculePresenter<SummaryEvent, SummaryState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryState): SummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var loadDataIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }
    var downloadingUrl by remember {
      mutableStateOf<String?>(null)
    }

    CollectEvents { event ->
      when (event) {
        Reload -> loadDataIteration++
        SubmitQuote -> submitIteration++
        ClearNavigation -> {
          if (currentState is MakingChanges) {
            currentState = (currentState as MakingChanges).copy(
              navigateToSuccess = false,
            )
          } else if (currentState is Success) {
            currentState = (currentState as Success).copy(
              navigateToFail = false,
            )
          } else {
            return@CollectEvents
          }
        }

        is DownLoadFromUrl -> {
          if (currentState !is Success) return@CollectEvents
          currentState = (currentState as Success).copy(isLoadingPdf = true)
          downloadingUrl = event.url
        }

        HandledSharingPdfFile -> {
          if (currentState !is Success) return@CollectEvents
          currentState = (currentState as Success).copy(
            savedFileUri = null,
          )
        }
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration > 0) {
        val previousState = currentState
        currentState = MakingChanges()
        tierRepository.submitChangeTierQuote(params.quoteIdToSubmit).fold(
          ifLeft = {
            currentState =
              (previousState as Success).copy(navigateToFail = true)
          },
          ifRight = {
            currentState = MakingChanges(
              navigateToSuccess = true,
            )
          },
        )
      }
    }

    LaunchedEffect(loadDataIteration) {
      currentState = Loading
      getCurrentContractDataUseCase.invoke(params.insuranceId).fold(
        ifLeft = {
          currentState = Failure
        },
        ifRight = { currentContractData ->
          val quote = tierRepository.getQuoteById(params.quoteIdToSubmit)
          quote.fold(
            ifLeft = {
              logcat(ERROR) {
                " Change tier flow SummaryViewModel: quoteIdToSubmit ${params.quoteIdToSubmit} not found in DB!"
              }
              currentState = Failure
            },
            ifRight = { rightQuote ->
              val currentContract = ContractData(
                contractGroup = currentContractData.productVariant.contractGroup,
                activeDisplayPremium = currentContractData.currentDisplayPremium.toString(),
                contractDisplayName = currentContractData.productVariant.displayName,
                contractDisplaySubtitle = currentContractData.currentExposureName,
              )
              currentState = Success(
                quote = rightQuote,
                currentContractData = currentContract,
              )
            },
          )
        },
      )
    }

    LaunchedEffect(downloadingUrl) {
      val downloadingUrlValue = downloadingUrl ?: return@LaunchedEffect
      logcat(LogPriority.INFO) { "Downloading terms and conditions with url:$downloadingUrl" }
      downloadPdfUseCase.invoke(downloadingUrlValue)
        .fold(
          ifLeft = { errorMessage ->
            logcat(LogPriority.ERROR) { "Downloading terms and conditions failed:$errorMessage" }
            // we only putting log on this, but no error state in the UI
            val state = currentState
            if (state !is Success) return@LaunchedEffect
            currentState = state.copy(isLoadingPdf = false)
            downloadingUrl = null
          },
          ifRight = { uri ->
            logcat(
              LogPriority.INFO,
            ) { "Downloading terms and conditions succeeded. Result uri:${uri.absolutePath}" }
            val state = currentState
            if (state !is Success) return@LaunchedEffect
            currentState = state.copy(isLoadingPdf = false, savedFileUri = uri)
            downloadingUrl = null
          },
        )
    }

    return currentState
  }
}

internal sealed interface SummaryState {
  data object Loading : SummaryState

  data class MakingChanges(
    val navigateToSuccess: Boolean = false,
  ) : SummaryState

  data class Success(
    val quote: TierDeductibleQuote,
    val currentContractData: ContractData,
    val savedFileUri: File? = null,
    val isLoadingPdf: Boolean = false,
    val navigateToFail: Boolean = false,
  ) : SummaryState

  data object Failure : SummaryState
}

internal sealed interface SummaryEvent {
  data object SubmitQuote : SummaryEvent

  data object Reload : SummaryEvent

  data object ClearNavigation : SummaryEvent

  data object HandledSharingPdfFile : SummaryEvent

  data class DownLoadFromUrl(val url: String) : SummaryEvent
}
