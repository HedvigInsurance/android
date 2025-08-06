package com.hedvig.android.apollo

import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.CacheMissException

sealed interface ApolloOperationError {
  val throwable: Throwable?
  val containsUnauthenticatedError: Boolean

  data class CacheMiss(override val throwable: CacheMissException) : ApolloOperationError {
    override val containsUnauthenticatedError: Boolean = false

    override fun toString(): String {
      return "CacheMiss(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  data class OperationException(override val throwable: ApolloException) : ApolloOperationError {
    override val containsUnauthenticatedError: Boolean = false

    override fun toString(): String {
      return "OperationException(throwableMessage=${throwable.message}, throwable=$throwable)"
    }
  }

  sealed interface OperationError : ApolloOperationError {
    object Unathenticated : OperationError {
      override val containsUnauthenticatedError: Boolean = true

      override val throwable: Throwable? = null

      override fun toString(): String {
        return "OperationError.Unathenticated"
      }
    }

    data class Other(
      private val message: String,
      override val containsUnauthenticatedError: Boolean = false,
    ) : OperationError {
      override val throwable: Throwable? = null

      override fun toString(): String {
        return "OperationError.Other(message=$message)"
      }
    }
  }
}
