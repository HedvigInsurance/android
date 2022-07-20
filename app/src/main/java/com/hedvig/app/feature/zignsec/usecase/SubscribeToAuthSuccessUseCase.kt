package com.hedvig.app.feature.zignsec.usecase

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.type.AuthState
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeSubscription
import d
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

/**
 * Subscribes to the [AuthStatusSubscription] and ensures re-subscribing to it when there is a failed auth status or
 * anything else goes wrong with the network. This is necessary to no matter what always make sure that we get informed
 * about the success auth case as long as we're collecting this flow.
 */
class SubscribeToAuthSuccessUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(): Flow<AuthState.SUCCESS> {
    return flow {
      while (currentCoroutineContext().isActive) {
        apolloClient
          .subscription(AuthStatusSubscription())
          .safeSubscription()
          .map(QueryResult<AuthStatusSubscription.Data>::toEither)
          .collect { response ->
            d { "Auth subscription result:$response" }
            when (response) {
              is Either.Left -> return@collect
              is Either.Right -> {
                when (val status = response.value.authStatus?.status) {
                  is AuthState.SUCCESS -> emit(status)
                  // Stop collecting to restart the subscription to ensure we're still listening for the SUCCESS case
                  is AuthState.FAILED -> return@collect
                  // INITIATED/IN_PROGRESS are not necessary to address, they are entirely captured by the WebView-flow
                  else -> {}
                }
              }
            }
          }
        delay(1.seconds)
      }
    }
  }
}
