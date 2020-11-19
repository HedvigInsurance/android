package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.fragment.BankAccountFragment
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.common.builders.ContractStatusFragmentBuilder
import com.hedvig.app.testdata.common.builders.CostBuilder
import java.time.LocalDate

data class PaymentDataBuilder(
    private val contracts: List<ContractStatus> = listOf(ContractStatus.ACTIVE),
    private val failedCharges: Int? = 0,
    private val currency: String = "SEK",
    private val discount: String = "0.00",
    private val subscription: String = "139.00",
    private val charge: String = subscription,
    private val nextChargeDate: LocalDate? = LocalDate.now().withDayOfMonth(27),
    private val chargeHistory: List<PaymentQuery.ChargeHistory> = emptyList(),
    private val freeUntil: LocalDate? = null,
    private val cost: CostFragment = CostBuilder(
        grossAmount = "139.00",
        netAmount = "139.00",
        discountAmount = "139.00",
        currency = currency
    ).build(),
    private val redeemedCampaigns: List<PaymentQuery.RedeemedCampaign> = emptyList(),
    private val payinType: PayinType = PayinType.TRUSTLY,
    private val payinConnected: Boolean = false,
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
                        amount = charge,
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
        redeemedCampaigns = redeemedCampaigns,
        bankAccount = if (payinType == PayinType.TRUSTLY && payinConnected) {
            PaymentQuery.BankAccount(
                fragments = PaymentQuery.BankAccount.Fragments(
                    BankAccountFragment(
                        bankName = "Testbanken",
                        descriptor = "**** 1234"
                    )
                )
            )
        } else {
            null
        },
        activePaymentMethods = if (payinType == PayinType.ADYEN && payinConnected) {
            PaymentQuery.ActivePaymentMethods(
                fragments = PaymentQuery.ActivePaymentMethods.Fragments(
                    ActivePaymentMethodsFragment(
                        storedPaymentMethodsDetails = ActivePaymentMethodsFragment.StoredPaymentMethodsDetails(
                            brand = "Testkortet",
                            lastFourDigits = "1234",
                            expiryMonth = "01",
                            expiryYear = "2050",
                        )
                    )
                )
            )
        } else {
            null
        }
    )
}

enum class PayinType {
    TRUSTLY,
    ADYEN,
}
