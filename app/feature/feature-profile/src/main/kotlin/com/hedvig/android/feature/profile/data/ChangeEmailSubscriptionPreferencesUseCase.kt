package com.hedvig.android.feature.profile.data

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.logger.logcat
import octopus.UpdateSubscriptionPreferenceMutation

interface ChangeEmailSubscriptionPreferencesUseCase {
  suspend fun invoke(subscribe: Boolean)
}

class ChangeEmailSubscriptionPreferencesUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
) : ChangeEmailSubscriptionPreferencesUseCase {
  override suspend fun invoke(subscribe: Boolean) {
    val result = apolloClient.mutation(UpdateSubscriptionPreferenceMutation(Optional.present(subscribe)))
      .safeExecute().toEither()
    val msg = result.getOrNull()?.memberUpdateSubscriptionPreference?.message
    logcat { "updateEmailSubscriptionPreference message: $msg" }
    networkCacheManager.clearCache()
  }
}
