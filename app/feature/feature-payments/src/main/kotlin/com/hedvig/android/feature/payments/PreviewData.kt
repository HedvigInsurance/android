package com.hedvig.android.feature.payments

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

internal val periodsPreviewData = persistentListOf(
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(200.0, CurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(200.0, CurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(400.0, CurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = true,
  ),
  MemberCharge.ChargeBreakdown.Period(
    amount = UiMoney(150.0, CurrencyCode.SEK),
    fromDate = LocalDate.fromEpochDays(200),
    toDate = LocalDate.fromEpochDays(300),
    isPreviouslyFailedCharge = false,
  ),
)

internal val discountsPreviewData = listOf(
  Discount(
    code = "CAR15",
    displayName = "Test 1",
    description = "Desc",
    expiredState = Discount.ExpiredState.AlreadyExpired(LocalDate(2022, 12, 14)),
    amount = UiMoney(20.0, CurrencyCode.SEK),
    isReferral = false,
  ),
  Discount(
    code = "RARING",
    displayName = "Test 2",
    description = "Desc",
    expiredState = Discount.ExpiredState.AlreadyExpired(LocalDate(2022, 12, 14)),
    amount = UiMoney(20.0, CurrencyCode.SEK),
    isReferral = false,
  ),
  Discount(
    code = "APAKATT",
    displayName = "Test 3",
    description = "Desc",
    expiredState = Discount.ExpiredState.ExpiringInTheFuture(LocalDate(2124, 12, 14)),
    amount = UiMoney(20.0, CurrencyCode.SEK),
    isReferral = true,
  ),
  Discount(
    code = "HEJHEJ",
    displayName = "Test 4",
    description = "Desc",
    expiredState = Discount.ExpiredState.AlreadyExpired(LocalDate.fromEpochDays(300)),
    amount = UiMoney(20.0, CurrencyCode.SEK),
    isReferral = false,
  ),
)

private val chargeHistoryPreviewData = listOf(
  MemberCharge(
    grossAmount = UiMoney(130.0, CurrencyCode.SEK),
    netAmount = UiMoney(250.0, CurrencyCode.SEK),
    id = "1",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(100),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(400.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(300.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, CurrencyCode.SEK),
  ),
  MemberCharge(
    grossAmount = UiMoney(500.0, CurrencyCode.SEK),
    netAmount = UiMoney(600.0, CurrencyCode.SEK),
    id = "2",
    status = MemberCharge.MemberChargeStatus.SUCCESS,
    dueDate = LocalDate.fromEpochDays(101),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, CurrencyCode.SEK),
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, CurrencyCode.SEK),
    netAmount = UiMoney(200.0, CurrencyCode.SEK),
    id = "3",
    status = MemberCharge.MemberChargeStatus.SUCCESS,
    dueDate = LocalDate.fromEpochDays(102),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, CurrencyCode.SEK),
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, CurrencyCode.SEK),
    netAmount = UiMoney(200.0, CurrencyCode.SEK),
    id = "4",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(401),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = null,
  ),
  MemberCharge(
    grossAmount = UiMoney(200.0, CurrencyCode.SEK),
    netAmount = UiMoney(200.0, CurrencyCode.SEK),
    id = "5",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(402),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, CurrencyCode.SEK),
  ),
)

internal val paymentOverViewPreviewData = PaymentOverview(
  memberCharge = MemberCharge(
    grossAmount = UiMoney(200.0, CurrencyCode.SEK),
    netAmount = UiMoney(200.0, CurrencyCode.SEK),
    id = "123",
    status = MemberCharge.MemberChargeStatus.FAILED,
    dueDate = LocalDate.fromEpochDays(400),
    failedCharge = MemberCharge.FailedCharge(
      fromDate = LocalDate.fromEpochDays(200),
      toDate = LocalDate.fromEpochDays(201),
    ),
    chargeBreakdowns = persistentListOf(
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Bilforsakring",
        contractDetails = "ABH 234",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
      MemberCharge.ChargeBreakdown(
        contractDisplayName = "Hemforsakring Bostad",
        contractDetails = "Bellmansgatan 19A",
        grossAmount = UiMoney(200.0, CurrencyCode.SEK),
        periods = periodsPreviewData,
      ),
    ),
    discounts = discountsPreviewData,
    carriedAdjustment = UiMoney(200.0, CurrencyCode.SEK),
    settlementAdjustment = UiMoney(200.0, CurrencyCode.SEK),
  ),
  pastCharges = chargeHistoryPreviewData.sortedBy { it.dueDate },
  paymentConnection = PaymentConnection(
    connectionInfo = PaymentConnection.ConnectionInfo(
      displayName = "Nordea",
      displayValue = "31489*****",
    ),
    status = PaymentConnection.PaymentConnectionStatus.NEEDS_SETUP,
  ),
  discounts = discountsPreviewData,
)
