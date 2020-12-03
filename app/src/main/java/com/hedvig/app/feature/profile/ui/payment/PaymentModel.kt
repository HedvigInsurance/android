package com.hedvig.app.feature.profile.ui.payment

import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import java.time.LocalDate

sealed class PaymentModel {
    object Header : PaymentModel()
    data class FailedPayments(
        val failedCharges: Int,
        val nextChargeDate: LocalDate
    ) : PaymentModel()

    data class NextPayment(
        val inner: PaymentQuery.Data
    ) : PaymentModel()

    object ConnectPayment : PaymentModel()

    data class CampaignInformation(val inner: PaymentQuery.Data) : PaymentModel()

    object PaymentHistoryHeader : PaymentModel()
    data class Charge(val inner: PaymentQuery.ChargeHistory) : PaymentModel()
    object PaymentHistoryLink : PaymentModel()

    data class TrustlyPayinDetails(
        val bankAccount: PaymentQuery.BankAccount,
        val status: PayinMethodStatus
    ) : PaymentModel()

    data class AdyenPayinDetails(val inner: PaymentQuery.ActivePaymentMethods) : PaymentModel()

    sealed class Link : PaymentModel() {
        object TrustlyChangePayin : Link()
        object AdyenChangePayin : Link()
    }

    object RedeemDiscountCode : PaymentModel()
}
