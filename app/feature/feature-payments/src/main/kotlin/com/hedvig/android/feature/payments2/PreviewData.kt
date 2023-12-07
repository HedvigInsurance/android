package com.hedvig.android.feature.payments2

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments2.data.MemberCharge
import com.hedvig.android.feature.payments2.data.PaymentConnection
import com.hedvig.android.feature.payments2.data.PaymentOverview
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
      )
    )
  ),
  paymentConnection = PaymentConnection(
    connectionInfo = PaymentConnection.ConnectionInfo(
      displayName = "Nordea",
      displayValue = "31489*****",
    ),
    status = PaymentConnection.PaymentConnectionStatus.NEEDS_SETUP,
  ),
)

