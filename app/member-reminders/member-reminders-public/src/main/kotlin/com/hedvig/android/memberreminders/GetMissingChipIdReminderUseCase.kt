package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import octopus.MissingChipIdReminderQuery

internal interface GetMissingChipIdReminderUseCase {
  fun invoke(): Flow<Either<ErrorMessage, MemberReminder.MissingChipId?>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMissingChipIdReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMissingChipIdReminderUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, MemberReminder.MissingChipId?>> {
    return apolloClient.query(MissingChipIdReminderQuery())
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow(::ErrorMessage)
      .mapLatest { result: Either<ErrorMessage, MissingChipIdReminderQuery.Data> ->
        either {
          result
            .bind()
            .currentMember
            .activeContracts
            .firstOrNull { it.isMissingPetId }
            ?.let { MemberReminder.MissingChipId() }
        }
      }
  }
}
