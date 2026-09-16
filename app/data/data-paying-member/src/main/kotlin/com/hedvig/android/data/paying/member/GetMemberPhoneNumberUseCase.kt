package com.hedvig.android.data.paying.member

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
import octopus.PayingMemberPhoneNumberQuery

/** The number the backend already holds for the member, null when it has never been given. */
interface GetMemberPhoneNumberUseCase {
  suspend fun invoke(): Either<ErrorMessage, String?>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMemberPhoneNumberUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberPhoneNumberUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String?> = either {
    apolloClient
      .query(PayingMemberPhoneNumberQuery())
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember
      .phoneNumber
  }
}
