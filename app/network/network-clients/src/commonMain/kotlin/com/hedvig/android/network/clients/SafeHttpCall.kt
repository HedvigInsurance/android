package com.hedvig.android.network.clients

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.io.IOException

sealed interface NetworkError {
  val throwable: Throwable?
  val message: String

  /**
   * Network IO error, such as no internet connection, host unreachable, timeout, etc.
   * This includes UnknownHostException when DNS resolution fails.
   */
  data class IOError(
    override val message: String,
    override val throwable: IOException,
  ) : NetworkError

  data class UnknownError(
    override val message: String,
    override val throwable: Throwable,
  ) : NetworkError
}

suspend fun HttpClient.safePost(
  urlString: String,
  block: HttpRequestBuilder.() -> Unit = {},
): Either<NetworkError, HttpResponse> {
  return Either.catch {
    post(urlString, block)
  }.mapLeft { it.toNetworkError() }
}

suspend fun <ErrorType> HttpClient.safePost(
  urlString: String,
  mapError: (NetworkError) -> ErrorType,
  block: HttpRequestBuilder.() -> Unit = {},
): Either<ErrorType, HttpResponse> {
  return safePost(urlString, block).mapLeft(mapError)
}

suspend fun HttpClient.safeGet(
  urlString: String,
  block: HttpRequestBuilder.() -> Unit = {},
): Either<NetworkError, HttpResponse> {
  return Either.catch {
    get(urlString, block)
  }.mapLeft { it.toNetworkError() }
}

suspend fun <ErrorType> HttpClient.safeGet(
  urlString: String,
  mapError: (NetworkError) -> ErrorType,
  block: HttpRequestBuilder.() -> Unit = {},
): Either<ErrorType, HttpResponse> {
  return safeGet(urlString, block).mapLeft(mapError)
}

private fun Throwable.toNetworkError(): NetworkError {
  return when (this) {
    is IOException -> NetworkError.IOError(
      message = "Network error: ${message ?: "Unable to reach server"}",
      throwable = this,
    )
    else -> NetworkError.UnknownError(
      message = "Unexpected error: ${message ?: "Unknown error"}",
      throwable = this,
    )
  }
}

fun ErrorMessage(networkError: NetworkError): ErrorMessage = object : ErrorMessage {
  override val message = when (networkError) {
    is NetworkError.IOError -> "Network error: Please check your internet connection."
    is NetworkError.UnknownError -> networkError.message
  }
  override val throwable = networkError.throwable

  override fun toString(): String {
    return "ErrorMessage(message=$message, throwable=$throwable)"
  }
}
