@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.payments.ios

import com.hedvig.android.data.paying.member.InvoiceDelivery
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberPaymentChargeMethod
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsEvent
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsUiState
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModelFactory
import com.hedvig.android.molecule.public.ScreenModel
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Null and "" as [chargeId] are the same: they select the upcoming charge when the backend sent it without an id,
 * otherwise the first past charge without one, otherwise nothing, which shows Failed.
 */
@HiddenFromObjC
fun createPaymentDetailsScreenModel(
  viewModelFactory: MetroViewModelFactory,
  chargeId: String?,
): PaymentDetailsScreenModel = PaymentDetailsScreenModel(viewModelFactory, chargeId)

/** The details of one charge. The Swift owner's rules are on [ScreenModel]. */
class PaymentDetailsScreenModel internal constructor(
  viewModelFactory: MetroViewModelFactory,
  chargeId: String?,
) : ScreenModel<PaymentDetailsScreenState>() {
  private val viewModel = viewModel(
    PaymentDetailsViewModel::class,
    {
      viewModelFactory.createManuallyAssistedFactory(PaymentDetailsViewModelFactory::class)()
        .create(chargeId.toChargeIdOrNull())
    },
  ) { it.toScreenState() }

  /** Loads the charge again. Safe to call from any thread. */
  fun reload() {
    viewModel.send(PaymentDetailsEvent.Reload)
  }
}

internal fun PaymentDetailsUiState.toScreenState(): PaymentDetailsScreenState = when (this) {
  PaymentDetailsUiState.Loading -> PaymentDetailsScreenState.Loading
  PaymentDetailsUiState.Failure -> PaymentDetailsScreenState.Failed
  is PaymentDetailsUiState.Success -> paymentDetails.toContent()
}

private fun PaymentDetails.toContent(): PaymentDetailsScreenState.Content {
  val charge = memberCharge
  val nextCharge = if (charge.status == MemberCharge.MemberChargeStatus.FAILED) {
    getNextCharge(charge) ?: upComingCharge
  } else {
    null
  }
  return PaymentDetailsScreenState.Content(
    id = charge.id.toChargeIdOrNull(),
    status = charge.status.toPaymentChargeStatus(),
    dueDate = charge.dueDate.toIsoDateString(),
    grossAmount = charge.grossAmount.toPaymentMoney(),
    netAmount = charge.netAmount.toPaymentMoney(),
    chargeBreakdowns = charge.chargeBreakdowns.map { it.toBreakdown() },
    carriedAdjustment = charge.carriedAdjustmentIfAboveZero()?.toPaymentMoney(),
    settlementAdjustment = charge.settlementAdjustmentIfAboveZero()?.toPaymentMoney(),
    referralDiscount = charge.referralDiscount?.let { PaymentReferralDiscount(it.code, it.amount?.toPaymentMoney()) },
    chargeMethod = charge.chargeMethod.toPaymentChargeMethod(),
    payinAccount = when (val info = paymentsInfo) {
      is PaymentDetails.PaymentsInfo.Active -> info.account.toPaymentPayinAccount()
      PaymentDetails.PaymentsInfo.NoPresentableInfo -> null
    },
    nextChargeId = nextCharge?.id.toChargeIdOrNull(),
    nextChargeDueDate = nextCharge?.dueDate?.toIsoDateString(),
  )
}

private fun MemberCharge.ChargeBreakdown.toBreakdown(): PaymentChargeBreakdown = PaymentChargeBreakdown(
  contractDisplayName = contractDisplayName,
  contractDetails = contractDetails,
  grossAmount = grossAmount.toPaymentMoney(),
  netAmount = netAmount.toPaymentMoney(),
  periods = periods.map { period ->
    PaymentChargePeriod(
      amount = period.amount.toPaymentMoney(),
      fromDate = period.fromDate.toIsoDateString(),
      toDate = period.toDate.toIsoDateString(),
      isPreviouslyFailedCharge = period.isPreviouslyFailedCharge,
      periodDescription = when (val description = period.description) {
        MemberCharge.ChargeBreakdown.Period.Description.FullPeriod -> {
          PaymentPeriodDescription.FullPeriod
        }

        is MemberCharge.ChargeBreakdown.Period.Description.BetweenDays -> {
          PaymentPeriodDescription.BetweenDays(description.daysBetween)
        }

        null -> {
          null
        }
      },
    )
  },
  priceBreakdown = priceBreakdown.map { (displayTitle, amount) ->
    PaymentPriceBreakdownItem(displayTitle, amount.toPaymentMoney())
  },
)

private fun MemberPaymentChargeMethod.toPaymentChargeMethod(): PaymentChargeMethod = when (this) {
  MemberPaymentChargeMethod.TRUSTLY -> PaymentChargeMethod.TRUSTLY
  MemberPaymentChargeMethod.SWISH -> PaymentChargeMethod.SWISH
  MemberPaymentChargeMethod.INVOICE -> PaymentChargeMethod.INVOICE
  MemberPaymentChargeMethod.UNKNOWN -> PaymentChargeMethod.UNKNOWN
}

private fun PayinAccount.toPaymentPayinAccount(): PaymentPayinAccount = when (this) {
  is PayinAccount.Trustly -> PaymentPayinAccount.Trustly(clearingNumber, accountNumber, bankName)

  is PayinAccount.SwishPayin -> PaymentPayinAccount.Swish(phoneNumber)

  is PayinAccount.Invoice -> PaymentPayinAccount.Invoice(
    delivery = when (delivery) {
      InvoiceDelivery.Kivra -> PaymentInvoiceDelivery.KIVRA
      InvoiceDelivery.Mail -> PaymentInvoiceDelivery.MAIL
      null -> null
    },
    email = email,
  )
}
