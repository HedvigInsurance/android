package com.hedvig.app.testdata.feature.payment

import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.android.owldroid.graphql.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.graphql.fragment.BankAccountFragment
import com.hedvig.android.owldroid.graphql.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.type.PayoutMethodStatus
import com.hedvig.android.owldroid.graphql.type.StoredCardDetails
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
    currency = currency,
  ).build(),
  private val redeemedCampaigns: List<PaymentQuery.RedeemedCampaign> = emptyList(),
  private val payinType: PayinType = PayinType.TRUSTLY,
  private val payinConnected: Boolean = false,
  private val payoutConnectionStatus: PayoutMethodStatus? = null,
) {
  fun build() = PaymentQuery.Data(
    contracts = contracts.map { contractStatus ->
      PaymentQuery.Contract(
        status = PaymentQuery.Status(
          __typename = contractStatus.typename,
          fragments = PaymentQuery.Status.Fragments(
            ContractStatusFragmentBuilder(contractStatus).build(),
          ),
        ),
      )
    },
    balance = PaymentQuery.Balance(
      failedCharges = failedCharges,
    ),
    chargeEstimation = PaymentQuery.ChargeEstimation(
      charge = PaymentQuery.Charge(
        __typename = "",
        fragments = PaymentQuery.Charge.Fragments(
          MonetaryAmountFragment(
            amount = charge,
            currency = currency,
          ),
        ),
      ),
      discount = PaymentQuery.Discount(
        __typename = "",
        fragments = PaymentQuery.Discount.Fragments(
          MonetaryAmountFragment(
            amount = discount,
            currency = currency,
          ),
        ),
      ),
      subscription = PaymentQuery.Subscription(
        __typename = "",
        fragments = PaymentQuery.Subscription.Fragments(
          MonetaryAmountFragment(
            amount = subscription,
            currency = currency,
          ),
        ),
      ),
    ),
    nextChargeDate = nextChargeDate,
    chargeHistory = chargeHistory,
    insuranceCost = PaymentQuery.InsuranceCost(
      __typename = "",
      freeUntil = freeUntil,
      fragments = PaymentQuery.InsuranceCost.Fragments(
        costFragment = cost,
      ),
    ),
    redeemedCampaigns = redeemedCampaigns,
    bankAccount = if (payinType == PayinType.TRUSTLY && payinConnected) {
      PaymentQuery.BankAccount(
        __typename = "",
        fragments = PaymentQuery.BankAccount.Fragments(
          BankAccountFragment(
            bankName = "Testbanken",
            descriptor = "**** 1234",
          ),
        ),
      )
    } else {
      null
    },
    activePaymentMethodsV2 = if (payinType == PayinType.ADYEN && payinConnected) {
      PaymentQuery.ActivePaymentMethodsV2(
        __typename = StoredCardDetails.type.name,
        fragments = PaymentQuery.ActivePaymentMethodsV2.Fragments(
          ActivePaymentMethodsFragment(
            __typename = StoredCardDetails.type.name,
            asStoredCardDetails = ActivePaymentMethodsFragment.AsStoredCardDetails(
              __typename = StoredCardDetails.type.name,
              brand = "Testkortet",
              lastFourDigits = "1234",
              expiryMonth = "01",
              expiryYear = "2050",
            ),
            asStoredThirdPartyDetails = null,
          ),
        ),
      )
    } else {
      null
    },
    activePayoutMethods = payoutConnectionStatus?.let { PaymentQuery.ActivePayoutMethods(status = it) },
  )
}

enum class PayinType {
  TRUSTLY,
  ADYEN,
}
