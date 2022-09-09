package com.hedvig.app.feature.zignsec.usecase

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.AuthStatusSubscription
import com.hedvig.android.apollo.graphql.type.AuthState
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeSubscription
import com.hedvig.app.util.apollo.toEither
import d
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class SubscribeToAuthResultUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(): Flow<AuthResult> {
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
                when (response.value.authStatus?.status) {
                  is AuthState.SUCCESS -> emit(AuthResult.Success)
                  is AuthState.FAILED -> emit(AuthResult.Failed)
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

sealed interface AuthResult {
  object Success : AuthResult
  object Failed : AuthResult
}
