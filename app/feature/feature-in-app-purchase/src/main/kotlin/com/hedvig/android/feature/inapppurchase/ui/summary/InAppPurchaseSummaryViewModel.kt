package com.hedvig.android.feature.inapppurchase.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.inapppurchase.data.GetInAppPurchaseSigningUseCase
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseOffer
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseSigning
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseSigningStatus
import com.hedvig.android.feature.inapppurchase.data.SignInAppPurchaseUseCase
import com.hedvig.android.feature.inapppurchase.navigation.InAppPurchaseKey
import com.hedvig.android.feature.inapppurchase.navigation.InAppPurchaseSuccessKey
import com.hedvig.android.feature.inapppurchase.navigation.InAppPurchaseSummaryKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class InAppPurchaseSummaryViewModel(
  @Assisted params: InAppPurchaseSummaryKey,
  signInAppPurchaseUseCase: SignInAppPurchaseUseCase,
  getInAppPurchaseSigningUseCase: GetInAppPurchaseSigningUseCase,
  backstack: Backstack,
) : MoleculeViewModel<InAppPurchaseSummaryEvent, InAppPurchaseSummaryUiState>(
    initialState = InAppPurchaseSummaryUiState(params.productDisplayName, params.offer, SigningState.Idle),
    presenter = InAppPurchaseSummaryPresenter(
      params = params,
      signInAppPurchaseUseCase = signInAppPurchaseUseCase,
      getInAppPurchaseSigningUseCase = getInAppPurchaseSigningUseCase,
      backstack = backstack,
    ),
  )

internal class InAppPurchaseSummaryPresenter(
  private val params: InAppPurchaseSummaryKey,
  private val signInAppPurchaseUseCase: SignInAppPurchaseUseCase,
  private val getInAppPurchaseSigningUseCase: GetInAppPurchaseSigningUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<InAppPurchaseSummaryEvent, InAppPurchaseSummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<InAppPurchaseSummaryEvent>.present(
    lastState: InAppPurchaseSummaryUiState,
  ): InAppPurchaseSummaryUiState {
    var signIteration by remember { mutableIntStateOf(0) }
    var signingState: SigningState by remember { mutableStateOf(lastState.signingState) }

    CollectEvents { event ->
      when (event) {
        InAppPurchaseSummaryEvent.Sign -> if (signingState.canStartSigning) signIteration++
        InAppPurchaseSummaryEvent.DismissFailure -> signingState = SigningState.Idle
      }
    }

    LaunchedEffect(signIteration) {
      if (signIteration == 0) return@LaunchedEffect
      signingState = SigningState.Starting
      var signing: InAppPurchaseSigning = signInAppPurchaseUseCase.invoke(params.shopSessionId, params.offer.id).fold(
        ifLeft = {
          signingState = SigningState.Failed(it.message)
          return@LaunchedEffect
        },
        ifRight = { it },
      )
      while (true) {
        when (signing.status) {
          InAppPurchaseSigningStatus.PENDING -> {
            signingState = SigningState.AwaitingBankId(signing.id, signing.autoStartToken, signing.liveQrCodeData)
          }

          InAppPurchaseSigningStatus.CREATING -> {
            signingState = SigningState.Creating
          }

          InAppPurchaseSigningStatus.SIGNED -> {
            backstack.navigateAndPopUpTo<InAppPurchaseKey>(
              InAppPurchaseSuccessKey(params.offer.startDate),
              inclusive = true,
            )
            return@LaunchedEffect
          }

          InAppPurchaseSigningStatus.FAILED -> {
            signingState = SigningState.Failed(signing.failureMessage)
            return@LaunchedEffect
          }
        }
        delay(1.seconds)
        signing = getInAppPurchaseSigningUseCase.invoke(signing.id).fold(
          ifLeft = {
            signingState = SigningState.Failed(it.message)
            return@LaunchedEffect
          },
          ifRight = { it },
        )
      }
    }

    return InAppPurchaseSummaryUiState(params.productDisplayName, params.offer, signingState)
  }
}

internal sealed interface InAppPurchaseSummaryEvent {
  data object Sign : InAppPurchaseSummaryEvent

  data object DismissFailure : InAppPurchaseSummaryEvent
}

internal data class InAppPurchaseSummaryUiState(
  val productDisplayName: String,
  val offer: InAppPurchaseOffer,
  val signingState: SigningState,
)

internal sealed interface SigningState {
  data object Idle : SigningState

  data object Starting : SigningState

  data class AwaitingBankId(
    val signingId: String,
    val autoStartToken: String?,
    val liveQrCodeData: String?,
  ) : SigningState

  data object Creating : SigningState

  /**
   * [message] is the backend's user-facing explanation, null for a network failure.
   */
  data class Failed(val message: String?) : SigningState

  val canStartSigning: Boolean get() = this is Idle || this is Failed
}
