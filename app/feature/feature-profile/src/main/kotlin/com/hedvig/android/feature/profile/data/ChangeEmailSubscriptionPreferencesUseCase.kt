package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import octopus.UpdateSubscriptionPreferenceMutation

/**
 * Requests to be subscribed/unsubscribed to promotional email.
 */
internal interface ChangeEmailSubscriptionPreferencesUseCase {
  suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess>
}

internal class ChangeEmailSubscriptionPreferencesUseCaseImpl(
  private val apolloClient: ApolloClient,
) : ChangeEmailSubscriptionPreferencesUseCase {
  override suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess> {
    return either {
      val data: UpdateSubscriptionPreferenceMutation.Data = apolloClient
        .mutation(UpdateSubscriptionPreferenceMutation(Optional.present(subscribe)))
        .safeExecute()
        .mapLeft {
          SubPrefError(it.toString())
        }
        .bind()
      val userErrorMessage = data.memberUpdateSubscriptionPreference?.message
      if (userErrorMessage != null) {
        raise(SubPrefError(userErrorMessage))
      }
      SubPrefSuccess
    }
  }
}

internal data class SubPrefError(val message: String?)

internal data object SubPrefSuccess
