package com.hedvig.android.feature.purchase.common.ui.sign

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.common.data.SigningStatus
import com.hedvig.android.feature.purchase.common.navigation.SigningParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.delay

class SigningViewModel(
  signingParameters: SigningParameters,
  pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculeViewModel<SigningEvent, SigningUiState>(
    initialState = SigningUiState.Polling(
      autoStartToken = signingParameters.autoStartToken,
      startDate = signingParameters.startDate,
      liveQrCodeData = null,
      bankIdOpened = false,
    ),
    presenter = SigningPresenter(signingParameters, pollSigningStatusUseCase),
  )

class SigningPresenter(
  private val signingParameters: SigningParameters,
  private val pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculePresenter<SigningEvent, SigningUiState> {
  @Composable
  override fun MoleculePresenterScope<SigningEvent>.present(lastState: SigningUiState): SigningUiState {
    var bankIdOpened by remember { mutableStateOf((lastState as? SigningUiState.Polling)?.bankIdOpened ?: false) }
    var currentState by remember { mutableStateOf(lastState) }

    var pollIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        SigningEvent.BankIdOpened -> {
          bankIdOpened = true
        }

        SigningEvent.Retry -> {
          currentState = SigningUiState.Polling(
            autoStartToken = signingParameters.autoStartToken,
            startDate = signingParameters.startDate,
            liveQrCodeData = null,
            bankIdOpened = bankIdOpened,
          )
          pollIteration++
        }

        SigningEvent.ClearNavigation -> {}
      }
    }

    LaunchedEffect(pollIteration) {
      if (currentState is SigningUiState.Failed) return@LaunchedEffect
      while (true) {
        pollSigningStatusUseCase.invoke(signingParameters.signingId).fold(
          ifLeft = { error ->
            currentState = SigningUiState.Failed(error.message)
            return@LaunchedEffect
          },
          ifRight = { pollResult ->
            when (pollResult.status) {
              SigningStatus.SIGNED -> {
                currentState = SigningUiState.Success(startDate = signingParameters.startDate)
                return@LaunchedEffect
              }

              SigningStatus.FAILED -> {
                currentState = SigningUiState.Failed("Signeringen misslyckades")
                return@LaunchedEffect
              }

              SigningStatus.PENDING -> {
                currentState = SigningUiState.Polling(
                  autoStartToken = signingParameters.autoStartToken,
                  startDate = signingParameters.startDate,
                  liveQrCodeData = pollResult.liveQrCodeData,
                  bankIdOpened = bankIdOpened,
                )
              }
            }
          },
        )
        delay(2_000)
      }
    }

    return when (val state = currentState) {
      is SigningUiState.Polling -> state.copy(bankIdOpened = bankIdOpened)
      else -> currentState
    }
  }
}

sealed interface SigningUiState {
  data class Polling(
    val autoStartToken: String,
    val startDate: String?,
    val liveQrCodeData: String?,
    val bankIdOpened: Boolean,
  ) : SigningUiState

  data class Success(val startDate: String?) : SigningUiState

  data class Failed(val errorMessage: String?) : SigningUiState
}

sealed interface SigningEvent {
  data object BankIdOpened : SigningEvent

  data object Retry : SigningEvent

  data object ClearNavigation : SigningEvent
}
