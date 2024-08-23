package com.hedvig.android.apollo

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
