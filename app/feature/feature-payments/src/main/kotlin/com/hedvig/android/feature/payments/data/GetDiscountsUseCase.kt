package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import octopus.DiscountsQuery
import octopus.type.RedeemedCampaignType

internal interface GetDiscountsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<Discount>>
}

internal class GetDiscountsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetDiscountsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<Discount>> = either {
    val result = apolloClient.query(DiscountsQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()

    val discounts = result.currentMember.redeemedCampaigns
      .filter { it.type == RedeemedCampaignType.VOUCHER }
      .map {
        Discount(
          code = it.code,
          description = it.description,
          expiredState = Discount.ExpiredState.from(it.expiresAt, clock),
          amount = null,
          isReferral = false,
        )
      }
    discounts
  }
}

private fun Discount.ExpiredState.Companion.from(expirationDate: LocalDate?, clock: Clock): Discount.ExpiredState {
  if (expirationDate == null) {
    return Discount.ExpiredState.NotExpired
  }
  val today = clock.todayIn(TimeZone.currentSystemDefault())
  return if (expirationDate < today) {
    Discount.ExpiredState.AlreadyExpired(expirationDate)
  } else {
    Discount.ExpiredState.ExpiringInTheFuture(expirationDate)
  }
}
