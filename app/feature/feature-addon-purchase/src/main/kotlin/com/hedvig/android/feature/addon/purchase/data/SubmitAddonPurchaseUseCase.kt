package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.UpsellTravelAddonActivateMutation

internal interface SubmitAddonPurchaseUseCase {
  suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit>
}

internal class SubmitAddonPurchaseUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : SubmitAddonPurchaseUseCase {
  override suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, Unit> {
    return either {
      apolloClient.mutation(UpsellTravelAddonActivateMutation(addonId = addonId, quoteId = quoteId)).safeExecute().fold(
        ifLeft = { error ->
          logcat(LogPriority.ERROR) { "Tried to do UpsellTravelAddonActivateMutation but got error: $error" }
          raise(ErrorMessage())
        },
        ifRight = { result ->
          if (result.upsellTravelAddonActivate.userError != null) {
            raise(ErrorMessage(result.upsellTravelAddonActivate.userError.message))
          }
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(CrossSellInfoType.Addon)
        },
      )
    }
  }
}
