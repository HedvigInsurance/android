package com.hedvig.android.feature.chip.id.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.UpdateChipIdNumberMutation

internal interface UpdateChipIdUseCase {
  suspend fun invoke(petId: String, insuranceId: String): Either<ErrorMessage, Unit>
}

internal class UpdateChipIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : UpdateChipIdUseCase {
  override suspend fun invoke(petId: String, insuranceId: String): Either<ErrorMessage, Unit> {
    return either {
      logcat { "UpdateChipIdNumberMutation start" }
      val result = apolloClient.mutation(
        UpdateChipIdNumberMutation(
          petId = petId,
          contractId = insuranceId,
        ),
      )
        .safeExecute {
          logcat { "UpdateChipIdNumberMutation error: $it" }
          ErrorMessage()
        }
        .bind()

      val userError = result.midtermChangePetId?.userError
      if (userError != null) {
        raise(ErrorMessage(userError.message))
      }
      if (result.midtermChangePetId?.activationDate != null) {
        Unit
      } else {
        raise(ErrorMessage())
      }
    }
  }
}
