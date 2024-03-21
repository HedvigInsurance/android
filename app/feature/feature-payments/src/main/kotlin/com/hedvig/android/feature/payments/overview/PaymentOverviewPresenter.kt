package com.hedvig.android.feature.payments.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class PaymentOverviewPresenter(
  val getPaymentOverviewDataUseCase: Provider<GetPaymentOverviewDataUseCase>,
) : MoleculePresenter<PaymentsEvent, PaymentOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentsEvent>.present(
    lastState: PaymentOverviewUiState,
  ): PaymentOverviewUiState {
    var paymentUiState: PaymentOverviewUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        PaymentsEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val currentPaymentUiState = paymentUiState
      paymentUiState = if (currentPaymentUiState is PaymentOverviewUiState.Content) {
        currentPaymentUiState.copy(isLoading = true)
      } else {
        PaymentOverviewUiState.Loading
      }
      getPaymentOverviewDataUseCase.provide().invoke().fold(
        ifLeft = {
          paymentUiState = PaymentOverviewUiState.Error
        },
        ifRight = { paymentOverviewData ->
          paymentUiState = PaymentOverviewUiState.Content(
            isLoading = false,
            upcomingPayment = paymentOverviewData.paymentOverview.memberCharge?.let { memberCharge ->
              PaymentOverviewUiState.Content.UpcomingPayment(
                grossAmount = memberCharge.grossAmount,
                dueDate = memberCharge.dueDate,
              )
            },
            upcomingPaymentInfo = run {
              val memberCharge = paymentOverviewData.paymentOverview.memberCharge
              if (memberCharge?.status == MemberCharge.MemberChargeStatus.PENDING) {
                return@run PaymentOverviewUiState.Content.UpcomingPaymentInfo.InProgress
              }
              memberCharge?.failedCharge?.let { failedCharge ->
                return@run PaymentOverviewUiState.Content.UpcomingPaymentInfo.PaymentFailed(
                  failedPaymentStartDate = failedCharge.fromDate,
                  failedPaymentEndDate = failedCharge.toDate,
                )
              }
            },
            connectedPaymentInfo = run {
              val paymentConnection = paymentOverviewData.paymentOverview.paymentConnection
              when (paymentConnection) {
                is PaymentConnection.Active -> PaymentOverviewUiState.Content.ConnectedPaymentInfo.Connected(
                  displayName = paymentConnection.displayName,
                  maskedAccountNumber = paymentConnection.displayValue,
                )

                PaymentConnection.Pending -> PaymentOverviewUiState.Content.ConnectedPaymentInfo.Pending
                else -> PaymentOverviewUiState.Content.ConnectedPaymentInfo.NotConnected(
                  paymentOverviewData.paymentOverview.memberCharge?.dueDate,
                )
              }
            },
            contentForOtherScreens = PaymentOverviewUiState.Content.ContentForOtherScreens(
              paymentOverview = paymentOverviewData.paymentOverview,
              memberCharge = paymentOverviewData.paymentOverview.memberCharge,
              discountList = paymentOverviewData.paymentOverview.discounts,
            ),
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

internal sealed interface PaymentOverviewUiState {
  data object Loading : PaymentOverviewUiState

  data object Error : PaymentOverviewUiState

  data class Content(
    val isLoading: Boolean,
    val upcomingPayment: UpcomingPayment?,
    val upcomingPaymentInfo: UpcomingPaymentInfo?,
    val connectedPaymentInfo: ConnectedPaymentInfo,
    val contentForOtherScreens: ContentForOtherScreens,
  ) : PaymentOverviewUiState {
    data class UpcomingPayment(
      val grossAmount: UiMoney,
      val dueDate: LocalDate,
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

    data class ContentForOtherScreens(
      val paymentOverview: PaymentOverview,
      val memberCharge: MemberCharge?,
      val discountList: List<Discount>,
    )
  }
}
