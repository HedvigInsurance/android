package com.hedvig.android.network.clients.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Error as ApolloKotlinError
import com.apollographql.apollo.api.Operation.Data
import com.apollographql.apollo.api.variables
import com.apollographql.apollo.exception.CacheMissException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.android.apollo.ExtensionErrorType
import com.hedvig.android.apollo.extensionErrorType
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.network.clients.NetworkLogPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * Adds [platformInterceptors] in order, then [LoggingInterceptor], so a request is logged after every other
 * interceptor has seen its response.
 */
internal fun ApolloClient.Builder.addHedvigInterceptors(
  platformInterceptors: List<ApolloInterceptor>,
  networkLogPolicy: NetworkLogPolicy,
): ApolloClient.Builder = this
  .apply { platformInterceptors.forEach(::addInterceptor) }
  .addInterceptor(LoggingInterceptor(networkLogPolicy))

internal class LoggingInterceptor(
  private val networkLogPolicy: NetworkLogPolicy,
) : ApolloInterceptor {
  override fun <D : Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    logcat(LogPriority.DEBUG) { "GraphQL request for ${request.operation.name()} START." }
    return chain.proceed(request).onEach { response ->
      logcat(LogPriority.DEBUG) {
        if (networkLogPolicy.logsGraphqlData) {
          "GraphQL request for ${request.operation.name()} EMISSION. Response data: ${response.data}"
        } else {
          "GraphQL request for ${request.operation.name()} EMISSION."
        }
      }
      val data = response.data
      val errors = response.errors.orEmpty().map { it.toGraphqlError() }
      if (errors.isNotEmpty() && !errors.isUnauthenticated()) {
        logError(data, errors, request, response)
      }
      if (response.exception != null && response.exception !is CacheMissException) {
        logcat(LogPriority.WARN) {
          val exception = response.exception!!
          if (networkLogPolicy.logsGraphqlErrors) {
            "GraphQL exception for ${request.operation.name()}: $exception"
          } else {
            "GraphQL exception for ${request.operation.name()}: ${exception::class.simpleName}"
          }
        }
      }
    }.onCompletion {
      logcat(LogPriority.DEBUG) { "GraphQL request for ${request.operation.name()} END." }
    }
  }

  private fun <D : Data> logError(
    data: D?,
    errors: List<LoggableGraphqlError>,
    request: ApolloRequest<D>,
    response: ApolloResponse<D>,
  ) {
    val errorMessage = if (networkLogPolicy.logsGraphqlErrors) {
      val errorMessagesJoinedToString = errors.joinToString(", ", "[", "]") { it.message }
      "GraphQL errors for ${request.operation.name()}: $errorMessagesJoinedToString"
    } else {
      val errorTypes = errors.map { it.extensions.errorType?.value }
      "GraphQL errors for ${request.operation.name()}: ${errors.size} (error types $errorTypes)"
    }
    val errorAttributes = buildMap {
      if (networkLogPolicy.logsGraphqlData) {
        put("data", response.data)
        put("variables", request.variablesForLog())
      }
      if (networkLogPolicy.logsGraphqlErrors) {
        put("errors", errors)
        if (response.extensions.isNotEmpty()) {
          put("extensions", response.extensions.toList().joinToString(", ", "[", "]") { it.toString() })
        }
      }
    }
    logcat(LogPriority.ERROR) {
      buildString {
        append(errorMessage)
        append(" attributes: ")
        append(errorAttributes)
        if (data != null) {
          append(" There was data and errors at the same time!")
          if (networkLogPolicy.logsGraphqlData) {
            append(" Data:$data")
          }
        }
      }
    }
    logError(
      message = errorMessage,
      source = ErrorSource.NETWORK,
      attributes = errorAttributes,
    )
  }
}

/**
 * Reading the variables runs the operation's own serialization. A failure there must not fail the request it describes.
 */
private fun <D : Data> ApolloRequest<D>.variablesForLog(): String {
  return try {
    val customScalarAdapters = executionContext[CustomScalarAdapters] ?: CustomScalarAdapters.Empty
    operation.variables(customScalarAdapters).valueMap.toString()
  } catch (e: Exception) {
    "Could not read variables: ${e::class.simpleName}"
  }
}

internal class LogoutOnUnauthenticatedInterceptor(
  private val authTokenService: AuthTokenService,
  private val demoManager: DemoManager,
) : ApolloInterceptor {
  override fun <D : Data> intercept(request: ApolloRequest<D>, chain: ApolloInterceptorChain): Flow<ApolloResponse<D>> {
    return chain.proceed(request).onEach { response ->
      val errors = response.errors.orEmpty().map { it.toGraphqlError() }
      val isUnauthenticated = errors.isUnauthenticated()
      if (isUnauthenticated && !demoManager.isDemoMode().first()) {
        logcat { "LogoutOnUnauthenticatedInterceptor detected unauthenticated request, force logging out" }
        authTokenService.logoutAndInvalidateTokens()
      }
    }
  }
}

private data class LoggableGraphqlError(
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
    val errorType: ExtensionErrorType?,
    val uncategorizedExtensions: Map<String, Any?>?,
  )
}

private fun List<LoggableGraphqlError>.isUnauthenticated(): Boolean {
  return any { it.extensions.errorType == ExtensionErrorType.Unauthenticated }
}

private fun ApolloKotlinError.toGraphqlError(): LoggableGraphqlError {
  return LoggableGraphqlError(
    message = this.message,
    locations = this.locations?.map { LoggableGraphqlError.Location(it.line, it.column) },
    paths = this.path?.map { LoggableGraphqlError.Path(it.toString()) },
    extensions = LoggableGraphqlError.Extensions(
      errorType = this.extensionErrorType(),
      uncategorizedExtensions = this.extensions?.minus("errorType"),
    ),
  )
}
