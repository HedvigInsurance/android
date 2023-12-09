package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

internal class PaymentRepositoryDemo : PaymentRepository {
  override suspend fun getChargeHistory(): Either<ErrorMessage, ChargeHistory> = either {
    ChargeHistory(
      List(5) { index ->
        ChargeHistory.Charge(
          amount = UiMoney(49.0 * (index + 1), CurrencyCode.SEK),
          date = Clock.System.now().minus(31.days * index).toLocalDateTime(TimeZone.UTC).date,
          paymentStatus = ChargeHistory.Charge.PaymentStatus.SUCCESSFUL,
        )
      },
    )
  }

  override suspend fun getPaymentData(): Either<ErrorMessage, PaymentData> = either {
    PaymentData(
      id = "id",
      upcomingChargeNet = UiMoney(amount = 99.0, currencyCode = CurrencyCode.SEK),
      upcomingChargeDate = Clock.System.now().plus(27.days).toLocalDateTime(TimeZone.UTC).date,
      monthlyCostNet = UiMoney(amount = 99.0, currencyCode = CurrencyCode.SEK),
      monthlyCostDiscount = UiMoney(amount = 30.0, currencyCode = CurrencyCode.SEK),
      agreements = listOf(
        PaymentData.Agreement(
          ContractGroup.HOUSE,
          "Home Insurance",
          UiMoney(amount = 99.0, currencyCode = CurrencyCode.SEK),
        ),
      ),
      paymentConnectionStatus = PaymentData.PaymentConnectionStatus.ACTIVE,
      paymentDisplayName = "Bank",
      paymentDescriptor = "**** **** **** 1234",
      redeemedCampaigns = emptyList(),
    )
  }

  override suspend fun redeemReferralCode(campaignCode: CampaignCode): Either<RedeemFailure, Unit> {
    return Unit.right()
  }
}
