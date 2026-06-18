package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.navigation.SubmitFailureKey
import com.hedvig.android.feature.change.tier.navigation.SubmitSuccessKey
import com.hedvig.android.feature.change.tier.navigation.SummaryParameters
import com.hedvig.android.feature.change.tier.ui.stepcustomize.ContractData
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.Reload
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryEvent.SubmitQuote
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Failure
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Loading
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.MakingChanges
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryState.Success
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.datetime.LocalDate

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SummaryViewModel(
  @Assisted params: SummaryParameters,
  tierRepository: ChangeTierRepository,
  getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SummaryEvent, SummaryState>(
    initialState = Loading,
    presenter = SummaryPresenter(
      params = params,
      tierRepository = tierRepository,
      getCurrentContractDataUseCase = getCurrentContractDataUseCase,
      backstack = backstack,
    ),
  )

private class SummaryPresenter(
  private val params: SummaryParameters,
  private val tierRepository: ChangeTierRepository,
  private val getCurrentContractDataUseCase: GetCurrentContractDataUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SummaryEvent, SummaryState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryState): SummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var loadDataIteration by remember { mutableIntStateOf(0) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        Reload -> {
          loadDataIteration++
        }

        SubmitQuote -> {
          submitIteration++
        }
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration > 0) {
        val previousState = currentState
        currentState = MakingChanges
        tierRepository.submitChangeTierQuote(params.quoteIdToSubmit).fold(
          ifLeft = {
            currentState = previousState
            backstack.add(SubmitFailureKey)
          },
          ifRight = {
            backstack.navigateAndPopUpTo<ChooseTierKey>(
              SubmitSuccessKey(params.activationDate),
              inclusive = true,
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
              val currentQuoteToChange = tierRepository.getQuoteById(tierRepository.getCurrentQuoteId()).getOrNull()
              if (currentQuoteToChange == null) {
                logcat(ERROR) {
                  " Change tier flow SummaryViewModel: currentQuoteId not found in DB!"
                }
                currentState = Failure
              } else {
                val currentContract = ContractData(
                  contractGroup = currentQuoteToChange.productVariant.contractGroup,
                  activeDisplayPremium = currentQuoteToChange.currentTotalCost.monthlyNet,
                  contractDisplayName = currentQuoteToChange.productVariant.displayName,
                  contractDisplaySubtitle = currentContractData.currentExposureName,
                )
                currentState = Success(
                  quote = rightQuote,
                  currentContractData = currentContract,
                  activationDate = params.activationDate,
                )
              }
            },
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
    val activationDate: LocalDate,
  ) : SummaryState {
    val totalNet: UiMoney = quote.newTotalCost.monthlyNet
  }

  data object Failure : SummaryState
}

internal sealed interface SummaryEvent {
  data object SubmitQuote : SummaryEvent

  data object Reload : SummaryEvent
}
