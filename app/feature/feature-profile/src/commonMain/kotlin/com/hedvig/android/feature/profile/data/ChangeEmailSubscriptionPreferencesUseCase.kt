package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.UpdateSubscriptionPreferenceMutation

/**
 * Requests to be subscribed/unsubscribed to promotional email.
 */
internal interface ChangeEmailSubscriptionPreferencesUseCase {
  suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
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
