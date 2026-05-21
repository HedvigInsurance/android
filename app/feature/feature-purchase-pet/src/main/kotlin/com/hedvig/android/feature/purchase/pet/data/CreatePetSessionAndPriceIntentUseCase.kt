package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PetMemberContactInfoQuery
import octopus.PetPriceIntentCreateMutation
import octopus.PetShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreatePetSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreatePetSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreatePetSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(PetShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(PetPriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentCreate.id },
        )

      val member = apolloClient
        .query(PetMemberContactInfoQuery())
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
        logcat(LogPriority.ERROR) { "Member is missing SSN — cannot continue pet purchase" }
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
