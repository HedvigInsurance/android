package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentCartEntriesAddMutation
import octopus.ApartmentStartSignMutation

internal interface AddToCartAndStartSignUseCase {
  suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart>
}

internal class AddToCartAndStartSignUseCaseImpl(
  private val apolloClient: ApolloClient,
) : AddToCartAndStartSignUseCase {
  override suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart> {
    return either {
      val cartResult = apolloClient
        .mutation(ApartmentCartEntriesAddMutation(shopSessionId = shopSessionId, offerIds = listOf(offerId)))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to add to cart: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCartEntriesAdd },
        )

      if (cartResult.userError != null) {
        raise(ErrorMessage(cartResult.userError?.message))
      }

      val signResult = apolloClient
        .mutation(ApartmentStartSignMutation(shopSessionId = shopSessionId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to start signing: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionStartSign },
        )

      if (signResult.userError != null) {
        raise(ErrorMessage(signResult.userError?.message))
      }

      val signing = signResult.signing ?: run {
        logcat(LogPriority.ERROR) { "No signing session returned" }
        raise(ErrorMessage())
      }

      val autoStartToken = signing.seBankidProperties?.autoStartToken ?: run {
        logcat(LogPriority.ERROR) { "No BankID autoStartToken in signing response" }
        raise(ErrorMessage())
      }

      SigningStart(
        signingId = signing.id,
        autoStartToken = autoStartToken,
      )
    }
  }
}
