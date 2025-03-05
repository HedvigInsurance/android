package com.hedvig.android.feature.payments.ui.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection.Active
import com.hedvig.android.feature.payments.data.PaymentConnection.NeedsSetup
import com.hedvig.android.feature.payments.data.PaymentConnection.Pending
import com.hedvig.android.feature.payments.data.PaymentConnection.Unknown
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class PaymentsPresenter(
  private val getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
  private val marketManager: MarketManager,
) : MoleculePresenter<PaymentsEvent, PaymentsUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentsEvent>.present(lastState: PaymentsUiState): PaymentsUiState {
    var paymentsUiState: PaymentsUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        PaymentsEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val currentPaymentUiState = paymentsUiState
      paymentsUiState = when (currentPaymentUiState) {
        is PaymentsUiState.Content -> {
          currentPaymentUiState.copy(isRetrying = true)
        }

        else -> {
          PaymentsUiState.Loading
        }
      }
      getUpcomingPaymentUseCase.provide().invoke().fold(
        ifLeft = {
          paymentsUiState = PaymentsUiState.Error
        },
        ifRight = { paymentOverview ->
          val allowChangingConnectedBankAccount = marketManager.market.value == Market.SE
          paymentsUiState = PaymentsUiState.Content(
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
            connectedPaymentInfo = when (val paymentConnection = paymentOverview.paymentConnection) {
              is Active -> PaymentsUiState.Content.ConnectedPaymentInfo.Connected(
                displayName = paymentConnection.displayName,
                maskedAccountNumber = paymentConnection.displayValue,
                allowChangingConnectedBankAccount = allowChangingConnectedBankAccount,
              )

              Pending -> PaymentsUiState.Content.ConnectedPaymentInfo.Pending

              is NeedsSetup -> {
                PaymentsUiState.Content.ConnectedPaymentInfo.NeedsSetup(
                  dueDateToConnect = paymentConnection.terminationDateIfNotConnected,
                  allowChangingConnectedBankAccount = allowChangingConnectedBankAccount,
                )
              }

              Unknown -> {
                PaymentsUiState.Content.ConnectedPaymentInfo.Unknown
              }
            },
          )
        },
      )
    }
    return paymentsUiState
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
        val allowChangingConnectedBankAccount: Boolean,
      ) : ConnectedPaymentInfo

      data object Pending : ConnectedPaymentInfo

      data class Connected(
        val displayName: String,
        val maskedAccountNumber: String,
        val allowChangingConnectedBankAccount: Boolean,
      ) : ConnectedPaymentInfo
    }
  }
}
