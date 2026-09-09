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
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentConnection.Active
import com.hedvig.android.feature.payments.data.PaymentConnection.NeedsPayinSetup
import com.hedvig.android.feature.payments.data.PaymentConnection.Pending
import com.hedvig.android.feature.payments.data.PaymentConnection.Unknown
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.data.PrimaryPayinMethod
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class PaymentsPresenter(
  private val getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
) : MoleculePresenter<PaymentsEvent, PaymentsUiState> {
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
      paymentOverviewResult = getUpcomingPaymentUseCase.invoke()
    }

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
                isManualChargeAllowed = paymentOverview.isManualChargeAllowed,
              )
            }
            PaymentsUiState.Content.UpcomingPaymentInfo.NoInfo
          },
          ongoingCharges = paymentOverview.ongoingCharges,
          connectedPaymentInfo = paymentOverview.paymentConnection.toConnectedPaymentInfo(),
          primaryPayinMethod = paymentOverview.primaryPayinMethod,
          memberType = paymentOverview.memberType,
        )
      },
    )
  }
}

private fun PaymentConnection.toConnectedPaymentInfo(): ConnectedPaymentInfo {
  return when (this) {
    Active -> ConnectedPaymentInfo.Active

    Pending -> ConnectedPaymentInfo.Pending

    is NeedsPayinSetup -> ConnectedPaymentInfo.NeedsPayinSetup(
      dueDateToConnect = terminationDateIfNotConnected,
    )

    Unknown -> ConnectedPaymentInfo.Unknown

    PaymentConnection.NeedsPayoutSetup -> ConnectedPaymentInfo.NeedsPayoutSetup
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
    val primaryPayinMethod: PrimaryPayinMethod?,
    val memberType: MemberType,
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
        val isManualChargeAllowed: ManualChargeToPrompt?,
      ) : UpcomingPaymentInfo
    }

    sealed interface ConnectedPaymentInfo {
      object Unknown : ConnectedPaymentInfo

      data class NeedsPayinSetup(
        val dueDateToConnect: LocalDate?,
      ) : ConnectedPaymentInfo

      data object Pending : ConnectedPaymentInfo

      data object Active : ConnectedPaymentInfo

      data object NeedsPayoutSetup : ConnectedPaymentInfo
    }
  }
}
