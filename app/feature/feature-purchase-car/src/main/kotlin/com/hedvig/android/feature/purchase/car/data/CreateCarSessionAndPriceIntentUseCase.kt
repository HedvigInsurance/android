package com.hedvig.android.feature.purchase.car.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CarMemberContactInfoQuery
import octopus.CarPriceIntentCreateMutation
import octopus.CarShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreateCarSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreateCarSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateCarSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(CarShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(CarPriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentCreate.id },
        )

      val member = apolloClient
        .query(CarMemberContactInfoQuery())
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to fetch member contact info: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.currentMember },
        )
      val ssn = member.ssn
      if (ssn == null) {
        logcat(LogPriority.ERROR) { "Member is missing SSN — cannot continue car purchase" }
        raise(ErrorMessage())
      }

      SessionAndIntent(
        shopSessionId = shopSessionId,
        priceIntentId = priceIntentId,
        ssn = ssn,
        email = member.email,
      )
    }
  }
}
