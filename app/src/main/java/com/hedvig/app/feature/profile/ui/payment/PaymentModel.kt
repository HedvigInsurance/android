package com.hedvig.app.feature.profile.ui.payment

import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.graphql.type.PayinMethodStatus
import com.hedvig.android.owldroid.graphql.type.PayoutMethodStatus
import com.hedvig.hanalytics.PaymentType
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

    data class ConnectPayment(val payinType: PaymentType) : PaymentModel()

    data class CampaignInformation(val inner: PaymentQuery.Data) : PaymentModel()

    object PaymentHistoryHeader : PaymentModel()
    data class Charge(val inner: PaymentQuery.ChargeHistory) : PaymentModel()
    object PaymentHistoryLink : PaymentModel()

    data class TrustlyPayinDetails(
        val bankAccount: PaymentQuery.BankAccount,
        val status: PayinMethodStatus,
    ) : PaymentModel()

    data class AdyenPayinDetails(val inner: PaymentQuery.ActivePaymentMethodsV2) : PaymentModel()

    object PayoutDetailsHeader : PaymentModel()
    data class PayoutConnectionStatus(
        val status: PayoutMethodStatus,
    ) : PaymentModel()

    data class PayoutDetailsParagraph(val status: PayoutMethodStatus) : PaymentModel()

    sealed class Link : PaymentModel() {
        object RedeemDiscountCode : Link()
        data class TrustlyChangePayin(
            override val payinType: PaymentType,
        ) : Link(), PayinLink

        data class AdyenChangePayin(
            override val payinType: PaymentType,
        ) : Link(), PayinLink

        object AdyenAddPayout : Link()

        object AdyenChangePayout : Link()

        interface PayinLink {
            val payinType: PaymentType
        }
    }
}
