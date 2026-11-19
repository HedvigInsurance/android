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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * The app's Apollo interceptors, the same on every platform and in this order: a member whose session the backend
 * rejects is logged out first, then the request is logged.
 */
internal fun ApolloClient.Builder.addHedvigInterceptors(
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  logMemberData: Boolean,
): ApolloClient.Builder = this
  .addInterceptor(LogoutOnUnauthenticatedInterceptor(authTokenService, demoManager))
  .addInterceptor(LoggingInterceptor(logMemberData))

internal class LoggingInterceptor(
  private val logMemberData: Boolean,
) : ApolloInterceptor {
  override fun <D : Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    logcat(LogPriority.DEBUG) { "GraphQL request for ${request.operation.name()} START." }
    return chain.proceed(request).onEach { response ->
      logcat(LogPriority.DEBUG) {
        val responseData = if (logMemberData) " Response data: ${response.data}" else ""
        "GraphQL request for ${request.operation.name()} EMISSION.$responseData"
      }
      val data = response.data
      val errors = response.errors.orEmpty().map { it.toGraphqlError() }
      if (errors.isNotEmpty() && !errors.isUnauthenticated()) {
        logError(data, errors, request, response)
      }
      if (response.exception != null && response.exception !is CacheMissException) {
        logcat(LogPriority.WARN) {
          "GraphQL exception for ${request.operation.name()}: ${response.exception}"
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
    val errorMessagesJoinedToString = errors.joinToString(", ", "[", "]") { it.message }
    val errorMessage = "GraphQL errors for ${request.operation.name()}: $errorMessagesJoinedToString"
    val errorAttributes = buildMap {
      if (logMemberData) {
        put("data", response.data)
        put("variables", request.variablesForLog())
      }
      put("errors", errors)
      if (response.extensions.isNotEmpty()) {
        put("extensions", response.extensions.toList().joinToString(", ", "[", "]") { it.toString() })
      }
    }
    logcat(LogPriority.ERROR) {
      buildString {
        append(errorMessage)
        append(" attributes: ")
        append(errorAttributes)
        if (data != null) {
          append(" There was data and errors at the same time!")
          if (logMemberData) {
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
