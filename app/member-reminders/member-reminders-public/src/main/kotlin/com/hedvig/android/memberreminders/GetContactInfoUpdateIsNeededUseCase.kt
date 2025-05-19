package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import octopus.NeedsContactInfoUpdateReminderQuery

interface GetContactInfoUpdateIsNeededUseCase {
  fun invoke(): Flow<Either<ErrorMessage, MemberReminder.ContactInfoUpdateNeeded?>>
}

internal class GetContactInfoUpdateIsNeededUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetContactInfoUpdateIsNeededUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, MemberReminder.ContactInfoUpdateNeeded?>> {
    return apolloClient.query(
      NeedsContactInfoUpdateReminderQuery(),
    )
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow(::ErrorMessage)
      .mapLatest { result: Either<ErrorMessage, NeedsContactInfoUpdateReminderQuery.Data> ->
        either {
          val isContactInfoUpdateNeeded = result
            .bind()
            .currentMember
            .memberActions
            ?.isContactInfoUpdateNeeded
          if (isContactInfoUpdateNeeded == true) MemberReminder.ContactInfoUpdateNeeded() else null
        }
      }
  }
}
