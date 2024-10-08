package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ClearNavigation
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ExpandCard
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.ScrollToDetails
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.SubmitQuote
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SummaryViewModel(
  params: SummaryParameters,
  tierRepository: ChangeTierRepository,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculeViewModel<SummaryEvent, SummaryState>(
    initialState = Loading,
    presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepository,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
    ),
  )

private class SummaryPresenter(
  private val params: SummaryParameters,
  private val tierRepository: ChangeTierRepository,
  private val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
) : MoleculePresenter<SummaryEvent, SummaryState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryState): SummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var loadDataIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        ExpandCard -> TODO()
        Reload -> loadDataIteration++
        ScrollToDetails -> TODO()
        SubmitQuote -> submitIteration++
        ClearNavigation -> {
          if (currentState !is Success) return@CollectEvents
          currentState = (currentState as Success).copy(
            navigateToSuccess = false,
            navigateToFail = false)
        }
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration > 0) {
        val previousState = currentState
        currentState = MakingChanges
        tierRepository.submitChangeTierQuote(params.quoteIdToSubmit).fold(
          ifLeft = {
            currentState =
              (previousState as Success).copy(navigateToSuccess = false, navigateToFail = true)
          },
          ifRight = {
            currentState = (previousState as Success).copy(
              navigateToSuccess = true,
              navigateToFail = false,
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
          val currentContract = ContractData(
            contractGroup = currentContractData.productVariant.contractGroup,
            activeDisplayPremium = currentContractData.currentDisplayPremium.toString(),
            contractDisplayName = currentContractData.productVariant.displayName,
            contractDisplaySubtitle = currentContractData.currentExposureName,
          )
          currentState = Success(
            quote = quote,
            currentContractData = currentContract,
          )
        },
      )
    }

    return currentState
  }
}

internal sealed interface SummaryState {
  data object Loading : SummaryState

  data object MakingChanges : SummaryState

  data class Success(
    val quote: TierDeductibleQuote,
    val currentContractData: ContractData,
    val navigateToSuccess: Boolean = false,
    val navigateToFail: Boolean = false,
  ) : SummaryState

  data object Failure : SummaryState
}

internal sealed interface SummaryEvent {
  data object SubmitQuote : SummaryEvent

  data object ScrollToDetails : SummaryEvent

  data object ExpandCard : SummaryEvent

  data object Reload : SummaryEvent

  data object ClearNavigation : SummaryEvent
}
