package com.hedvig.android.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.logger.logcat
import octopus.UpdateSubscriptionPreferenceMutation

internal interface ChangeEmailSubscriptionPreferencesUseCase {
  suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess>
}

internal class ChangeEmailSubscriptionPreferencesUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : ChangeEmailSubscriptionPreferencesUseCase {
  override suspend fun invoke(subscribe: Boolean): Either<SubPrefError, SubPrefSuccess> {
    val result = apolloClient
      .mutation(UpdateSubscriptionPreferenceMutation(Optional.present(subscribe)))
      .safeExecute()
      .toEither()
      .mapLeft {
        logcat { "" }
        SubPrefError(it.message)
      }
      .map {
        SubPrefSuccess
      }
    networkCacheManager.clearCache()
    return result
  }
}

internal data class SubPrefError(val message: String?)

internal data object SubPrefSuccess
