package com.hedvig.app.feature.profile.ui.payment

import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.android.owldroid.type.PayoutMethodStatus
import java.time.LocalDate

sealed class PaymentModel {
    object Header : PaymentModel()
    data class FailedPayments(
        val failedCharges: Int,
        val nextChargeDate: LocalDate,
    ) : PaymentModel()

    data class NextPayment(
        val inner: PaymentQuery.Data,
    ) : PaymentModel()

    object ConnectPayment : PaymentModel()

    data class CampaignInformation(val inner: PaymentQuery.Data) : PaymentModel()

    object PaymentHistoryHeader : PaymentModel()
    data class Charge(val inner: PaymentQuery.ChargeHistory) : PaymentModel()
    object PaymentHistoryLink : PaymentModel()

    data class TrustlyPayinDetails(
        val bankAccount: PaymentQuery.BankAccount,
        val status: PayinMethodStatus,
    ) : PaymentModel()

    data class AdyenPayinDetails(val inner: PaymentQuery.ActivePaymentMethods) : PaymentModel()

    object PayoutDetailsHeader : PaymentModel()
    data class PayoutConnectionStatus(
        val status: PayoutMethodStatus,
    ) : PaymentModel()

    data class PayoutDetailsParagraph(val status: PayoutMethodStatus) : PaymentModel()

    sealed class Link : PaymentModel() {
        object RedeemDiscountCode : Link()
        object TrustlyChangePayin : Link()
        object AdyenChangePayin : Link()
        object AdyenAddPayout : Link()
        object AdyenChangePayout : Link()
    }
}
