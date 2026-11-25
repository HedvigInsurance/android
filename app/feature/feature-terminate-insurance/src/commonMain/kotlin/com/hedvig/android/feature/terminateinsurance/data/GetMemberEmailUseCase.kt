package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.WithdrawalConfirmationEmailQuery

internal interface GetMemberEmailUseCase {
  suspend fun invoke(): Either<ErrorMessage, String>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMemberEmailUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberEmailUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String> = either {
    apolloClient
      .query(WithdrawalConfirmationEmailQuery())
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember
      .email
  }
}
