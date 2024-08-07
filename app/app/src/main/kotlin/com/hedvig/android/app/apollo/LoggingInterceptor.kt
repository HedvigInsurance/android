package com.hedvig.android.app.apollo

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error as ApolloKotlinError
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Operation.Data
import com.apollographql.apollo.exception.CacheMissException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.android.app.apollo.GraphqlError.Extensions.ErrorType
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

private data class GraphqlError(
  val message: String,
  val locations: List<Location>?,
  val paths: List<Path>?,
  val extensions: Extensions,
) {
  data class Location(
    val line: Int,
    val column: Int,
  )

  data class Path(
    val message: String,
  )

  data class Extensions(
    val errorType: ErrorType?,
    val uncategorizedExtensions: Map<String, Any?>?,
  ) {
    @JvmInline
    value class ErrorType(val value: String) {
      companion object {
        val Unauthenticated = ErrorType("UNAUTHENTICATED")
      }
    }
  }
}

private fun ApolloKotlinError.toGraphqlError(): GraphqlError {
  return GraphqlError(
    message = this.message,
    locations = this.locations?.map { GraphqlError.Location(it.line, it.column) },
    paths = this.path?.map { GraphqlError.Path(it.toString()) },
    extensions = GraphqlError.Extensions(
      errorType = this.extensions?.get("errorType")?.let { GraphqlError.Extensions.ErrorType(it.toString()) },
      uncategorizedExtensions = this.extensions?.minus("errorType"),
    ),
  )
}

private fun List<GraphqlError>.isUnathenticated(): Boolean {
  return any { it.extensions.errorType == ErrorType.Unauthenticated }
}
