package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
internal class GetInsurableLimitsUsageUseCaseDemo : GetInsurableLimitsUsageUseCase {
  override suspend fun invoke(contractId: String): Either<ErrorMessage, InsurableLimitsUsage?> {
    return fakeInsurableLimitsUsage.right()
  }
}

internal val fakeInsurableLimitsUsage = InsurableLimitsUsage(
  periodStartDate = LocalDate(2026, 3, 1),
  periodEndDate = LocalDate(2027, 2, 28),
  total = fakeLimitUsage(used = 34_200.0, limit = 100_000.0),
  medicine = fakeLimitUsage(used = 6_800.0, limit = 10_000.0),
  deductible = fakeLimitUsage(used = 1_500.0, limit = 2_000.0),
)

private fun fakeLimitUsage(used: Double, limit: Double) = InsurableLimitsUsage.LimitUsage(
  used = UiMoney(used, UiCurrencyCode.SEK),
  limit = UiMoney(limit, UiCurrencyCode.SEK),
  remaining = UiMoney((limit - used).coerceAtLeast(0.0), UiCurrencyCode.SEK),
)
