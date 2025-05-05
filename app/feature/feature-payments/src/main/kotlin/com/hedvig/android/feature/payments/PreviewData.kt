package com.hedvig.android.feature.payments

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import kotlinx.datetime.LocalDate

internal val periodsPreviewData = listOf(
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(200.0, UiCurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(200.0, UiCurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(400.0, UiCurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = true,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(150.0, UiCurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
)

internal val referralDiscountPreviewData = Discount(
  code = "APAKATT",
  displayName = null,
  description = null,
  expiredState = Discount.ExpiredState.NotExpired,
  amount = UiMoney(-20.0, UiCurrencyCode.SEK),
  isReferral = true,
)

internal val discountsPreviewData = listOf(
  Discount(
    code = "CAR15",
    displayName = null,
    description = "15% discount for 12 months",
    expiredState = Discount.ExpiredState.NotExpired,
    amount = UiMoney(20.0, UiCurrencyCode.SEK),
    isReferral = false,
  ),
  Discount(
    code = "HEJHEJ",
    displayName = "Test 4",
    description = "Desc",
    expiredState = Discount.ExpiredState.AlreadyExpired(LocalDate(2022, 12, 14)),
    amount = UiMoney(20.0, UiCurrencyCode.SEK),
    isReferral = false,
  ),
  Discount(
    code = "HEJHEJ",
    displayName = "Test 4",
    description = "Desc",
    expiredState = Discount.ExpiredState.ExpiringInTheFuture(LocalDate(2025, 12, 14)),
    amount = null,
    isReferral = false,
  ),
)

internal val chargeHistoryPreviewData = listOf(
  MemberCharge(
    grossAmount = UiMoney(130.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(250.0, UiCurrencyCode.SEK),
    id = "1",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(100),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = listOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(400.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(300.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(300.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(280.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
    ),
    carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    referralDiscount = referralDiscountPreviewData,
  ),
  MemberCharge(
    grossAmount = UiMoney(500.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(600.0, UiCurrencyCode.SEK),
    id = "2",
    status = MemberCharge.MemberChargeStatus.SUCCESS,
    dueDate = LocalDate.fromEpochDays(101),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = listOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
    ),
    carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    referralDiscount = referralDiscountPreviewData,
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    id = "3",
    status = MemberCharge.MemberChargeStatus.SUCCESS,
    dueDate = LocalDate.fromEpochDays(102),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = listOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
    ),
    carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    referralDiscount = referralDiscountPreviewData,
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    id = "4",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(401),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = listOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
    ),
    carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    settlementAdjustment = null,
    referralDiscount = referralDiscountPreviewData,
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
    id = "5",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(402),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = listOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
        netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
        periods = periodsPreviewData,
        discounts = discountsPreviewData,
      ),
    ),
    carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
    referralDiscount = referralDiscountPreviewData,
  ),
)

internal val paymentOverViewPreviewData: PaymentOverview
  get() {
    val memberChargeShortInfo = MemberChargeShortInfo(
      netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
      id = "123",
      status = MemberCharge.MemberChargeStatus.FAILED,
      dueDate = LocalDate.fromEpochDays(400),
      failedCharge = MemberCharge.FailedCharge(
        fromDate = LocalDate.fromEpochDays(200),
        toDate = LocalDate.fromEpochDays(201),
      ),
    )
    return PaymentOverview(
      memberChargeShortInfo = memberChargeShortInfo,
      ongoingCharges = listOf(OngoingCharge("id", LocalDate.fromEpochDays(401), UiMoney(200.0, UiCurrencyCode.SEK))),
      paymentConnection = PaymentConnection.Active(
        displayName = "Nordea",
        displayValue = "31489*****",
      ),
    )
  }

internal val paymentDetailsPreviewData = MemberCharge(
  grossAmount = UiMoney(280.0, UiCurrencyCode.SEK),
  netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
  id = "123",
  status = MemberCharge.MemberChargeStatus.PENDING,
  dueDate = LocalDate.fromEpochDays(400),
  failedCharge = MemberCharge.FailedCharge(
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(201),
  ),
  chargeBreakdowns = listOf(
    MemberCharge.ChargeBreakdown(
      contractDisplayName = "Bilforsakring",
      contractDetails = "ABH 234",
      grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
      netAmount = UiMoney(200.0, UiCurrencyCode.SEK),
      periods = periodsPreviewData,
      discounts = discountsPreviewData,
    ),
    MemberCharge.ChargeBreakdown(
      contractDisplayName = "Hemforsakring Bostad",
      contractDetails = "Bellmansgatan 19A",
      grossAmount = UiMoney(200.0, UiCurrencyCode.SEK),
      netAmount = UiMoney(180.0, UiCurrencyCode.SEK),
      periods = periodsPreviewData,
      discounts = discountsPreviewData,
    ),
  ),
  referralDiscount = referralDiscountPreviewData,
  carriedAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
  settlementAdjustment = UiMoney(200.0, UiCurrencyCode.SEK),
)
