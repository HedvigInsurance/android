package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.common.builders.ContractStatusFragmentBuilder
import com.hedvig.app.testdata.common.builders.CostBuilder
import java.time.LocalDate

data class PaymentDataBuilder(
    private val contracts: List<ContractStatus> = listOf(ContractStatus.ACTIVE),
    private val failedCharges: Int? = null,
    private val currency: String = "SEK",
    private val discount: String = "0.00",
    private val subscription: String = "139.00",
    private val charge: String = subscription,
    private val nextChargeDate: LocalDate? = LocalDate.now().withDayOfMonth(27),
    private val chargeHistory: List<PaymentQuery.ChargeHistory> = emptyList(),
    private val freeUntil: LocalDate? = null,
    private val cost: CostFragment = CostBuilder().build(),
    private val redeemedCampaigns: List<PaymentQuery.RedeemedCampaign> = emptyList()
) {
    fun build() = PaymentQuery.Data(
        contracts = contracts.map {
            PaymentQuery.Contract(
                status = PaymentQuery.Status(
                    fragments = PaymentQuery.Status.Fragments(
                        ContractStatusFragmentBuilder(it).build()
                    )
                )
            )
        },
        balance = PaymentQuery.Balance(
            failedCharges = failedCharges
        ),
        chargeEstimation = PaymentQuery.ChargeEstimation(
            charge = PaymentQuery.Charge(
                fragments = PaymentQuery.Charge.Fragments(
                    MonetaryAmountFragment(
                        amount = "139.00",
                        currency = currency
                    )
                )
            ),
            discount = PaymentQuery.Discount(
                fragments = PaymentQuery.Discount.Fragments(
                    MonetaryAmountFragment(
                        amount = discount,
                        currency = currency
                    )
                )
            ),
            subscription = PaymentQuery.Subscription(
                fragments = PaymentQuery.Subscription.Fragments(
                    MonetaryAmountFragment(
                        amount = subscription,
                        currency = currency
                    )
                )
            )
        ),
        nextChargeDate = nextChargeDate,
        chargeHistory = chargeHistory,
        insuranceCost = PaymentQuery.InsuranceCost(
            freeUntil = freeUntil,
            fragments = PaymentQuery.InsuranceCost.Fragments(
                costFragment = cost
            )
        ),
        redeemedCampaigns = redeemedCampaigns
    )
}
