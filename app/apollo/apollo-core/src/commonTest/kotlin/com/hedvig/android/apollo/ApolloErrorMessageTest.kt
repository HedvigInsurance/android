package com.hedvig.android.apollo

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.apollographql.apollo.exception.ApolloNetworkException
import kotlin.test.Test

class ApolloErrorMessageTest {
  @Test
  fun `a transport failure offers no copy for a screen to show`() {
    val error = ApolloOperationError.OperationException(ApolloNetworkException("Socket timeout has expired"))

    assertThat(ErrorMessage(error).message).isNull()
  }

  @Test
  fun `a rejection carried in the response body offers no copy either`() {
    val error = ApolloOperationError.OperationError.Other("INTERNAL_ERROR for [wildcard] ext: [(classification, X)]")

    assertThat(ErrorMessage(error).message).isNull()
  }

  @Test
  fun `the throwable is kept so the failure is still diagnosable`() {
    val cause = ApolloNetworkException("Socket timeout has expired")

    assertThat(ErrorMessage(ApolloOperationError.OperationException(cause)).throwable).isEqualTo(cause)
  }

  @Test
  fun `the string form still names the underlying error for logs`() {
    val error = ApolloOperationError.OperationError.Other("INTERNAL_ERROR")

    assertThat(ErrorMessage(error).toString()).contains("INTERNAL_ERROR")
  }
}
