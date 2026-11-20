package com.hedvig.android.iosdata.bridge

import arrow.core.Either
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameInstanceAs
import assertk.assertions.prop
import com.apollographql.apollo.exception.ApolloNetworkException
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.core.common.ErrorMessage
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class IosReadTest {
  @Test
  fun `a read returns its value`() = runTest {
    assertThat(iosRead { Either.Right(1).bind() }).isEqualTo(1)
  }

  @Test
  fun `an unauthenticated operation error is UNAUTHENTICATED`() = runTest {
    assertFailure { iosRead { Either.Left(ApolloOperationError.OperationError.Unathenticated).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.UNAUTHENTICATED)
  }

  @Test
  fun `an error message made from an unauthenticated operation error is UNAUTHENTICATED`() = runTest {
    assertFailure { iosRead { Either.Left(ErrorMessage(ApolloOperationError.OperationError.Unathenticated)).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.UNAUTHENTICATED)
  }

  @Test
  fun `a network exception is NETWORK when raised as an operation error or an error message or thrown`() = runTest {
    val network = ApolloNetworkException("offline")
    val reads: List<suspend () -> Unit> = listOf(
      { iosRead { Either.Left(ApolloOperationError.OperationException(network)).bind() } },
      { iosRead { Either.Left(ErrorMessage(throwable = network)).bind() } },
      { iosRead { throw network } },
    )
    for (read in reads) {
      assertFailure { read() }.isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.NETWORK)
    }
  }

  @Test
  fun `any other failure is OTHER and keeps its cause`() = runTest {
    val thrown = IllegalStateException("bug")

    assertFailure { iosRead { throw thrown } }
      .isInstanceOf<ReadFailure>().given {
        assertThat(it.kind).isEqualTo(ReadFailureKind.OTHER)
        assertThat(it.cause).isSameInstanceAs(thrown)
      }
    assertFailure { iosRead { Either.Left(ApolloOperationError.OperationError.Other("bad")).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.OTHER)
  }

  @Test
  fun `a cancellation passes through unchanged`() = runTest {
    val cancellation = CancellationException("cancelled")

    assertFailure { iosRead { throw cancellation } }.isSameInstanceAs(cancellation)
  }
}
