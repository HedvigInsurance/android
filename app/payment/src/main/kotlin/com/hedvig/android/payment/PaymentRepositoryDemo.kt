package com.hedvig.android.payment

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.api.ApolloResponse
import com.hedvig.android.apollo.OperationResult
import giraffe.PaymentQuery
import giraffe.type.Locale
import giraffe.type.PayoutMethodStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration.Companion.days

class PaymentRepositoryDemo : PaymentRepository {
  override fun payment(): Flow<ApolloResponse<PaymentQuery.Data>> {
    return flowOf(
      ApolloResponse.Builder(
        PaymentQuery(Locale.en_SE),
        UUID.randomUUID(),
        PaymentQuery.Data(),
      ).build(),
    )
  }

  override suspend fun refresh(): ApolloResponse<PaymentQuery.Data> {
    return ApolloResponse.Builder(
      PaymentQuery(Locale.en_SE),
      UUID.randomUUID(),
      PaymentQuery.Data(),
    ).build()
  }

  override suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
  }

  override suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory> = either {
    ChargeHistory(
      List(5) { index ->
        ChargeHistory.Charge(
          amount = Money.of(49 * (index + 1), "SEK"),
          date = Clock.System.now().minus(31.days * index).toLocalDateTime(TimeZone.UTC).date.toJavaLocalDate(),
        )
      },
    )
  }

  override suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData> = either {
    PaymentData(
      nextCharge = Money.of(BigDecimal(100), "SEK"),
      monthlyCost = Money.of(BigDecimal(100), "SEK"),
      totalDiscount = Money.of(BigDecimal(10), "SEK"),
      nextChargeDate = LocalDate.now(),
      redeemedCampagins = listOf(),
      bankName = "Test Bank",
      bankDescriptor = "123456",
      paymentMethod = null,
      bankAccount = null,
      contracts = listOf(),
      payoutMethodStatus = null,
    )
  }
}
