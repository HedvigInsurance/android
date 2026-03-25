package com.hedvig.android.feature.chip.id.data

import arrow.core.Either
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage

internal interface UpdateChipIdUseCase {
  suspend fun invoke(insuranceId: String): Either<ErrorMessage, Unit>
}

internal class UpdateChipIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : UpdateChipIdUseCase {
  override suspend fun invoke(insuranceId: String): Either<ErrorMessage, Unit> {
    return either {
   //   raise(ErrorMessage())
    }
    // TODO: Implement actual mutation call
    // return apolloClient.mutation(UpdateChipIdMutation(insuranceId))
    //   .execute()
    //   .fold(
    //     ifLeft = { Either.left(ErrorMessage()) },
    //     ifRight = { Either.right(Unit) }
    //   )
  }
}
