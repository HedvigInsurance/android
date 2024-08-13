package com.hedvig.android.app.apollo

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Operation.Data
import com.apollographql.apollo.exception.CacheMissException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.android.apollo.GraphqlError
import com.hedvig.android.apollo.toGraphqlError
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

internal class LoggingInterceptor : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return chain.proceed(request).onEach { response ->
      val errors = response.errors.orEmpty().map { it.toGraphqlError() }
      if (errors.isNotEmpty() && !errors.isUnathenticated()) {
        logError(errors, request, response)
      }
      if (response.exception != null && response.exception !is CacheMissException) {
        logcat(LogPriority.WARN) {
          "GraphQL exception for ${request.operation.name()}: ${response.exception}"
        }
      }
    }
  }

  private fun <D : Operation.Data> logError(
    errors: List<GraphqlError>,
    request: ApolloRequest<D>,
    response: ApolloResponse<D>,
  ) {
    val errorMessagesJoinedToString = errors.joinToString(", ", "[", "]") { it.message }
    val errorMessage = "GraphQL errors for ${request.operation.name()}: $errorMessagesJoinedToString"
    val errorAttributes = mapOf(
      "data" to response.data,
      "errors" to errors,
    )
    logcat(LogPriority.ERROR) {
      buildString {
        append(errorMessage)
        append(" attributes: ")
        append(errorAttributes)
      }
    }
    logError(
      message = errorMessage,
      source = ErrorSource.NETWORK,
      attributes = errorAttributes,
    )
  }
}

internal class LogoutOnUnauthenticatedInterceptor(
  private val authTokenService: AuthTokenService,
) : ApolloInterceptor {
  override fun <D : Data> intercept(request: ApolloRequest<D>, chain: ApolloInterceptorChain): Flow<ApolloResponse<D>> {
    return chain.proceed(request).onEach { response ->
      val errors = response.errors.orEmpty().map { it.toGraphqlError() }
      val isUnauthenticated = errors.isUnathenticated()
      if (isUnauthenticated) {
        authTokenService.logoutAndInvalidateTokens()
      }
    }
  }
}

private fun List<GraphqlError>.isUnathenticated(): Boolean {
  return any { it.extensions.errorType == GraphqlError.Extensions.ErrorType.Unauthenticated }
}
