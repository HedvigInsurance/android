package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentOverview
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

internal interface GetPaymentOverviewDataUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverviewData>
}

internal class GetPaymentOverviewDataUseCaseImpl(
  private val getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
  private val getForeverInformationUseCase: GetForeverInformationUseCase,
  private val getOnlyHasNonPayingContractsUseCase: GetOnlyHasNonPayingContractsUseCase,
) : GetPaymentOverviewDataUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverviewData> {
    return either {
      parZip(
        { getForeverInformationUseCase.invoke().bind() },
        { getUpcomingPaymentUseCase.invoke().bind() },
        { getOnlyHasNonPayingContractsUseCase.invoke().bind() },
      ) { foreverInformation, paymentOverview, onlyHasNonPayingContracts ->
        PaymentOverviewData(
          paymentOverview = paymentOverview,
          foreverInformation = if (onlyHasNonPayingContracts) {
            null
          } else {
            foreverInformation
          },
        )
      }
    }
  }
}

internal data class PaymentOverviewData(
  val paymentOverview: PaymentOverview,
  val foreverInformation: ForeverInformation?,
)

internal class GetPaymentOverviewDataUseCaseDemo(
  private val clock: Clock,
) : GetPaymentOverviewDataUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverviewData> {
    return PaymentOverviewData(
      PaymentOverview(
        MemberCharge(
          UiMoney(100.0, CurrencyCode.SEK),
          UiMoney(100.0, CurrencyCode.SEK),
          "id",
          MemberCharge.MemberChargeStatus.SUCCESS,
          (clock.now() + 10.days).toLocalDateTime(TimeZone.UTC).date,
          null,
          emptyList(),
          emptyList(),
          null,
          null,
        ),
        null,
      ),
      ForeverInformation(
        "HedvigForever",
        UiMoney(20.0, CurrencyCode.SEK),
        UiMoney(10.0, CurrencyCode.SEK),
      ),
    ).right()
  }
}
