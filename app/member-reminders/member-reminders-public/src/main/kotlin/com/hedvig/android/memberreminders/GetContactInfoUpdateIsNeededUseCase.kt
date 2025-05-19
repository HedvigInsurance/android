package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface GetContactInfoUpdateIsNeededUseCase {
  fun invoke(): Flow<Either<ErrorMessage, MemberReminder.ContactInfoUpdateNeeded?>>
}

internal class GetContactInfoUpdateIsNeededUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetContactInfoUpdateIsNeededUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, MemberReminder.ContactInfoUpdateNeeded?>> {
//    return apolloClient.query(
//      NeedsContactInfoUpdateReminderQuery()
//    )
//      .fetchPolicy(FetchPolicy.CacheAndNetwork)
//      .safeFlow(::ErrorMessage)
//      .mapLatest { result: Either<ErrorMessage, NeedsContactInfoUpdateReminderQuery.Data> ->
//        either {
//          val isContactInfoUpdateNeeded = result.
//            .bind()
//            .currentMember
//            .memberActions
//            .isContactInfoUpdateNeeded
//          if (isContactInfoUpdateNeeded) Unit else null
//        }
    return flowOf(
      either {
        null
        // todo: implement instead the commented out part when BE will be ready
      },
    )
  }
}
