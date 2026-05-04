package com.hedvig.android.feature.payments.ui.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentConnection.Active
import com.hedvig.android.feature.payments.data.PaymentConnection.NeedsSetup
import com.hedvig.android.feature.payments.data.PaymentConnection.Pending
import com.hedvig.android.feature.payments.data.PaymentConnection.Unknown
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.overview.data.GetShouldShowPayoutUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

internal class PaymentsPresenter(
  private val getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
  getShouldShowPayoutUseCase: Provider<GetShouldShowPayoutUseCase>,
) : MoleculePresenter<PaymentsEvent, PaymentsUiState> {
  private val shouldShowPayoutPresenter = ShouldShowPayoutPresenter(getShouldShowPayoutUseCase)

  @Composable
  override fun MoleculePresenterScope<PaymentsEvent>.present(lastState: PaymentsUiState): PaymentsUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var paymentOverviewResult: Either<ErrorMessage, PaymentOverview>? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        PaymentsEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      paymentOverviewResult = null
      paymentOverviewResult = getUpcomingPaymentUseCase.provide().invoke()
    }

    val shouldShowPayout = shouldShowPayoutPresenter.present(loadIteration)

    val currentPaymentResult = paymentOverviewResult ?: return PaymentsUiState.Loading

    return currentPaymentResult.fold(
      ifLeft = { PaymentsUiState.Error },
      ifRight = { paymentOverview ->
        PaymentsUiState.Content(
          isRetrying = false,
          upcomingPayment = paymentOverview.memberChargeShortInfo?.let { memberCharge ->
            PaymentsUiState.Content.UpcomingPayment.Content(
              netAmount = memberCharge.netAmount,
              dueDate = memberCharge.dueDate,
              id = memberCharge.id,
            )
          } ?: PaymentsUiState.Content.UpcomingPayment.NoUpcomingPayment,
          upcomingPaymentInfo = run {
            val memberCharge = paymentOverview.memberChargeShortInfo
            if (memberCharge?.status == MemberCharge.MemberChargeStatus.PENDING) {
              return@run PaymentsUiState.Content.UpcomingPaymentInfo.InProgress
            }
            memberCharge?.failedCharge?.let { failedCharge ->
              return@run PaymentsUiState.Content.UpcomingPaymentInfo.PaymentFailed(
                failedPaymentStartDate = failedCharge.fromDate,
                failedPaymentEndDate = failedCharge.toDate,
              )
            }
            PaymentsUiState.Content.UpcomingPaymentInfo.NoInfo
          },
          ongoingCharges = paymentOverview.ongoingCharges,
          connectedPaymentInfo = paymentOverview.paymentConnection.toConnectedPaymentInfo(),
          showPayoutButton = shouldShowPayout,
        )
      },
    )
  }
}

private class ShouldShowPayoutPresenter(
  private val getShouldShowPayoutUseCase: Provider<GetShouldShowPayoutUseCase>,
) {
  @Composable
  fun present(loadIteration: Int): Boolean {
    var shouldShowPayout by remember { mutableStateOf(false) }
    LaunchedEffect(loadIteration) {
      shouldShowPayout = false
      for (attempt in 0..2) {
        delay(attempt.seconds)
        getShouldShowPayoutUseCase.provide().invoke().fold(
          ifLeft = {},
          ifRight = { result ->
            shouldShowPayout = result
            return@LaunchedEffect
          },
        )
      }
    }
    return shouldShowPayout
  }
}

private fun PaymentConnection.toConnectedPaymentInfo(): ConnectedPaymentInfo {
  return when (this) {
    Active -> ConnectedPaymentInfo.Active

    Pending -> ConnectedPaymentInfo.Pending

    is NeedsSetup -> ConnectedPaymentInfo.NeedsSetup(
      dueDateToConnect = terminationDateIfNotConnected,
    )

    Unknown -> ConnectedPaymentInfo.Unknown
  }
}

internal sealed interface PaymentsEvent {
  data object Retry : PaymentsEvent
}

internal sealed interface PaymentsUiState {
  data object Error : PaymentsUiState

  data object Loading : PaymentsUiState

  data class Content(
    val isRetrying: Boolean,
    val upcomingPayment: UpcomingPayment,
    val upcomingPaymentInfo: UpcomingPaymentInfo,
    val ongoingCharges: List<OngoingCharge>,
    val connectedPaymentInfo: ConnectedPaymentInfo,
    val showPayoutButton: Boolean,
  ) : PaymentsUiState {
    sealed interface UpcomingPayment {
      data object NoUpcomingPayment : UpcomingPayment

      data class Content(
        val netAmount: UiMoney,
        val dueDate: LocalDate,
        val id: String?,
      ) : UpcomingPayment
    }

    sealed interface UpcomingPaymentInfo {
      data object NoInfo : UpcomingPaymentInfo

      data object InProgress : UpcomingPaymentInfo

      data class PaymentFailed(
        val failedPaymentStartDate: LocalDate,
        val failedPaymentEndDate: LocalDate,
      ) : UpcomingPaymentInfo
    }

    sealed interface ConnectedPaymentInfo {
      object Unknown : ConnectedPaymentInfo

      data class NeedsSetup(
        val dueDateToConnect: LocalDate?,
      ) : ConnectedPaymentInfo

      data object Pending : ConnectedPaymentInfo

      data object Active : ConnectedPaymentInfo
    }
  }
}
