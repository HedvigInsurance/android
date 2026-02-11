package com.hedvig.feature.remove.addons.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ConfirmAddonRemovalMutation

internal interface SubmitAddonRemovalUseCase {
  suspend fun invoke(contractId: String, addonIds: List<String>): Either<ErrorMessage, Unit>
}

internal class SubmitAddonRemovalUseCaseImpl(
  private val apolloClient: ApolloClient,
): SubmitAddonRemovalUseCase {
  override suspend fun invoke(
    contractId: String,
    addonIds: List<String>,
  ): Either<ErrorMessage, Unit> {
    return either {
      apolloClient.mutation(ConfirmAddonRemovalMutation(addonIds = addonIds,
        contractId = contractId)).safeExecute().fold(
        ifLeft = { error ->
          logcat(LogPriority.ERROR) { "Tried to do ConfirmAddonRemovalMutation but got error: $error" }
          raise(ErrorMessage())
        },
        ifRight = { result ->
          if (result.addonRemoveConfirm != null) {
            raise(ErrorMessage(result.addonRemoveConfirm.message))
          }
        //todo maybe:  crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully
        },
      )
    }
  }
}
