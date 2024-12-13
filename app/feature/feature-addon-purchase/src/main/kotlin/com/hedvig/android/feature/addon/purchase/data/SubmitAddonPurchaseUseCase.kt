package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import kotlinx.datetime.LocalDate
import octopus.UpsellTravelAddonActivateMutation.Data.UpsellTravelAddonActivate

internal interface SubmitAddonPurchaseUseCase {
  suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit>
}

internal class SubmitAddonPurchaseUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitAddonPurchaseUseCase {
  override suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit> {
    return either {
      apolloClient.mutation(UpsellTravelAddonActivate(addonId = addonId, quoteId = quoteId)).safeExecute().fold(
        ifLeft = { error ->
          raise(ErrorMessage(error.message))
        },
        ifRight = {
          return Unit
        }
    }
  }
}
