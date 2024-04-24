package com.hedvig.android.feature.payments.payments

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
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class PaymentsPresenter(
  val getUpcomingPaymentUseCase: Provider<GetUpcomingPaymentUseCase>,
) : MoleculePresenter<PaymentsEvent, PaymentsUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentsEvent>.present(lastState: PaymentsUiState): PaymentsUiState {
    var paymentUiState: PaymentsUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        PaymentsEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val currentPaymentUiState = paymentUiState
      paymentUiState = if (currentPaymentUiState is PaymentsUiState.Content) {
        currentPaymentUiState.copy(isLoading = true)
      } else {
        PaymentsUiState.Loading
      }
      getUpcomingPaymentUseCase.provide().invoke().fold(
        ifLeft = {
          paymentUiState = PaymentsUiState.Error
        },
        ifRight = { paymentOverview ->
          paymentUiState = PaymentsUiState.Content(
            isLoading = false,
            upcomingPayment = paymentOverview.memberChargeShortInfo?.let { memberCharge ->
              PaymentsUiState.Content.UpcomingPayment(
                netAmount = memberCharge.netAmount,
                dueDate = memberCharge.dueDate,
                id = memberCharge.id,
              )
            },
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
            },
            connectedPaymentInfo = run {
              when (val paymentConnection = paymentOverview.paymentConnection) {
                is PaymentConnection.Active -> PaymentsUiState.Content.ConnectedPaymentInfo.Connected(
                  displayName = paymentConnection.displayName,
                  maskedAccountNumber = paymentConnection.displayValue,
                )

                PaymentConnection.Pending -> PaymentsUiState.Content.ConnectedPaymentInfo.Pending
                else -> PaymentsUiState.Content.ConnectedPaymentInfo.NotConnected(
                  paymentOverview.memberChargeShortInfo?.dueDate,
                )
              }
            },
          )
        },
      )
    }
    return paymentUiState
  }
}

internal sealed interface PaymentsEvent {
  data object Retry : PaymentsEvent
}

internal sealed interface PaymentsUiState {
  data object Loading : PaymentsUiState

  data object Error : PaymentsUiState

  data class Content(
    val isLoading: Boolean,
    val upcomingPayment: UpcomingPayment?,
    val upcomingPaymentInfo: UpcomingPaymentInfo?,
    val connectedPaymentInfo: ConnectedPaymentInfo,
  ) : PaymentsUiState {
    data class UpcomingPayment(
      val netAmount: UiMoney,
      val dueDate: LocalDate,
      val id: String,
    )

    sealed interface UpcomingPaymentInfo {
      data object InProgress : UpcomingPaymentInfo

      data class PaymentFailed(
        val failedPaymentStartDate: LocalDate,
        val failedPaymentEndDate: LocalDate,
      ) : UpcomingPaymentInfo
    }

    sealed interface ConnectedPaymentInfo {
      data class NotConnected(
        val dueDateToConnect: LocalDate?,
      ) : ConnectedPaymentInfo

      data object Pending : ConnectedPaymentInfo

      data class Connected(
        val displayName: String,
        val maskedAccountNumber: String,
      ) : ConnectedPaymentInfo
    }
  }
}
