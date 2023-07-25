package com.hedvig.app.testdata.feature.payment

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.util.months
import giraffe.PayinStatusQuery
import giraffe.PaymentQuery
import giraffe.fragment.IncentiveFragment
import giraffe.type.FreeMonths
import giraffe.type.MonthlyCostDeduction
import giraffe.type.PayinMethodStatus
import giraffe.type.PayoutMethodStatus
import giraffe.type.PercentageDiscountMonths
import java.time.LocalDate

val PAYIN_STATUS_DATA_NEEDS_SETUP = PayinStatusQuery.Data(PayinMethodStatus.NEEDS_SETUP)
val PAYIN_STATUS_DATA_ACTIVE = PayinStatusQuery.Data(PayinMethodStatus.ACTIVE)
val PAYIN_STATUS_DATA_PENDING = PayinStatusQuery.Data(PayinMethodStatus.PENDING)

val PAYMENT_DATA_NOT_CONNECTED = PaymentDataBuilder().build()
val PAYMENT_DATA_FAILED_PAYMENTS = PaymentDataBuilder(failedCharges = 1).build()

val PAYMENT_DATA_TRUSTLY_CONNECTED = PaymentDataBuilder(
  payinType = PayinType.TRUSTLY,
  payinConnected = true,
).build()
val PAYMENT_DATA_ADYEN_CONNECTED = PaymentDataBuilder(
  payinType = PayinType.ADYEN,
  payinConnected = true,
).build()
val PAYMENT_DATA_FREE_MONTHS = PaymentDataBuilder(
  freeUntil = LocalDate.now() + 3.months,
  cost = CostBuilder(
    discountAmount = "139.00",
    netAmount = "0.00",
    grossAmount = "139.00",
  ).build(),
  subscription = "139.00",
  discount = "139.00",
  charge = "0.00",
  redeemedCampaigns = listOf(
    PaymentQuery.RedeemedCampaign(
      __typename = "",
      owner = PaymentQuery.Owner(
        displayName = "Test Owner",
      ),
      code = "",
      fragments = PaymentQuery.RedeemedCampaign.Fragments(
        IncentiveFragment(
          incentive = IncentiveFragment.Incentive(
            __typename = FreeMonths.type.name,
            asFreeMonths = IncentiveFragment.AsFreeMonths(
              __typename = FreeMonths.type.name,
              quantity = 3,
            ),
            asMonthlyCostDeduction = null,
            asNoDiscount = null,
            asPercentageDiscountMonths = null,
          ),
          displayValue = "3 FREE MONTHS",
        ),
      ),
    ),
  ),
).build()
val PAYMENT_DATA_REFERRAL = PaymentDataBuilder(
  discount = "20.00",
  cost = CostBuilder(
    discountAmount = "20.00",
    netAmount = "119.00",
    grossAmount = "139.00",
  ).build(),
  charge = "119.00",
  redeemedCampaigns = listOf(
    PaymentQuery.RedeemedCampaign(
      __typename = "",
      owner = null,
      code = "",
      fragments = PaymentQuery.RedeemedCampaign.Fragments(
        IncentiveFragment(
          incentive = IncentiveFragment.Incentive(
            __typename = MonthlyCostDeduction.type.name,
            asFreeMonths = null,
            asMonthlyCostDeduction = IncentiveFragment.AsMonthlyCostDeduction(
              __typename = MonthlyCostDeduction.type.name,
              amount = IncentiveFragment.Amount(
                amount = "20.00",
              ),
            ),
            asNoDiscount = null,
            asPercentageDiscountMonths = null,
          ),
          displayValue = "20% DISCOUNT PER MONTH",
        ),
      ),
    ),
  ),
).build()
val PAYMENT_DATA_PERCENTAGE_CAMPAIGN = PaymentDataBuilder(
  redeemedCampaigns = listOf(
    PaymentQuery.RedeemedCampaign(
      __typename = "",
      owner = null,
      code = "",
      fragments = PaymentQuery.RedeemedCampaign.Fragments(
        IncentiveFragment(
          incentive = IncentiveFragment.Incentive(
            __typename = PercentageDiscountMonths.type.name,
            asFreeMonths = null,
            asMonthlyCostDeduction = null,
            asPercentageDiscountMonths = IncentiveFragment.AsPercentageDiscountMonths(
              __typename = PercentageDiscountMonths.type.name,
              percentageDiscount = 20.0,
              pdmQuantity = 2,
            ),
            asNoDiscount = null,
          ),
          displayValue = "20% DISCOUNT FOR 2 MONTHS",
        ),
      ),
    ),
  ),
).build()
val PAYMENT_DATA_INACTIVE = PaymentDataBuilder(contracts = listOf(ContractStatus.PENDING)).build()
val PAYMENT_DATA_PAYOUT_NOT_CONNECTED =
  PaymentDataBuilder(payoutConnectionStatus = PayoutMethodStatus.NEEDS_SETUP).build()
val PAYMENT_DATA_PAYOUT_CONNECTED = PaymentDataBuilder(payoutConnectionStatus = PayoutMethodStatus.ACTIVE).build()
val PAYMENT_DATA_PAYOUT_PENDING = PaymentDataBuilder(payoutConnectionStatus = PayoutMethodStatus.PENDING).build()
