package com.hedvig.android.apollo

import com.hedvig.android.core.common.await
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import org.json.JSONObject

sealed interface OperationResult<out T> {
  data class Success<T>(val data: T) : OperationResult<T>

  sealed interface Error : OperationResult<Nothing> {
    val throwable: Throwable?
    val message: String?

    data class NoDataError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): NoDataError {
          return NoDataError(null, message)
        }

        operator fun invoke(throwable: Throwable?): NoDataError {
          return NoDataError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class GeneralError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): GeneralError {
          return GeneralError(null, message)
        }

        operator fun invoke(throwable: Throwable?): GeneralError {
          return GeneralError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class OperationError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): OperationError {
          return OperationError(null, message)
        }

        operator fun invoke(throwable: Throwable?): OperationError {
          return OperationError(throwable, throwable?.localizedMessage)
        }
      }
    }

    data class NetworkError(
      override val throwable: Throwable?,
      override val message: String?,
    ) : Error {
      companion object {
        operator fun invoke(message: String?): NetworkError {
          return NetworkError(null, message)
        }

        operator fun invoke(throwable: Throwable?): NetworkError {
          return NetworkError(throwable, throwable?.localizedMessage)
        }
      }
    }
  }
}

/**
 * Only to be used when making GraphQL calls.
 * Returns [OperationResult.Success] only when the network request is successful and there are no graphQL error messages.
 * Returns [OperationResult.Error] on all other cases.
 */
suspend fun Call.safeGraphqlCall(): OperationResult<JSONObject> = withContext(Dispatchers.IO) {
  try {
    val response = await()
    if (response.isSuccessful.not()) return@withContext OperationResult.Error.NetworkError(response.message)

    val responseBody = response.body ?: return@withContext OperationResult.Error.NoDataError("No data")
    val jsonObject = JSONObject(responseBody.string())

    val errorJsonObject = jsonObject.optJSONArray("errors")?.getJSONObject(0)
    if (errorJsonObject != null) {
      val errorMessage = errorJsonObject.optString("message")
      return@withContext OperationResult.Error.OperationError(errorMessage)
    }

    OperationResult.Success(jsonObject)
  } catch (ioException: IOException) {
    OperationResult.Error.GeneralError(ioException.localizedMessage)
  }
}
