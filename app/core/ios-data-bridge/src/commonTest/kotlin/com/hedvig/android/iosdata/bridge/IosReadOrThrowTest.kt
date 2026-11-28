package com.hedvig.android.iosdata.bridge

import arrow.core.Either
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.prop
import com.apollographql.apollo.exception.ApolloNetworkException
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.MemberFacingErrorMessage
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class IosReadOrThrowTest {
  @Test
  fun `a read returns its value`() = runTest {
    assertThat(iosReadOrThrow { Either.Right(1).bind() }).isEqualTo(1)
  }

  @Test
  fun `an unauthenticated operation error is UNAUTHENTICATED`() = runTest {
    assertFailure { iosReadOrThrow { Either.Left(ApolloOperationError.OperationError.Unathenticated).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.UNAUTHENTICATED)
  }

  @Test
  fun `an error message made from an unauthenticated operation error is UNAUTHENTICATED`() = runTest {
    assertFailure {
      iosReadOrThrow { Either.Left(ErrorMessage(ApolloOperationError.OperationError.Unathenticated)).bind() }
    }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.UNAUTHENTICATED)
  }

  @Test
  fun `a network exception is NETWORK when raised as an operation error or an error message or thrown`() = runTest {
    val network = ApolloNetworkException("offline")
    val reads: List<suspend () -> Unit> = listOf(
      { iosReadOrThrow { Either.Left(ApolloOperationError.OperationException(network)).bind() } },
      { iosReadOrThrow { Either.Left(ErrorMessage(throwable = network)).bind() } },
      { iosReadOrThrow { throw network } },
    )
    for (read in reads) {
      assertFailure { read() }.isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.NETWORK)
    }
  }

  @Test
  fun `any other failure is OTHER and keeps its cause`() = runTest {
    val thrown = IllegalStateException("bug")

    assertFailure { iosReadOrThrow { throw thrown } }
      .isInstanceOf<ReadFailure>().given {
        assertThat(it.kind).isEqualTo(ReadFailureKind.OTHER)
        assertThat(it.cause).isSameInstanceAs(thrown)
      }
    assertFailure { iosReadOrThrow { Either.Left(ApolloOperationError.OperationError.Other("bad")).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::kind).isEqualTo(ReadFailureKind.OTHER)
  }

  @Test
  fun `a member-facing error message reaches Swift with its text`() = runTest {
    assertFailure { iosReadOrThrow { Either.Left(MemberFacingErrorMessage("Your pet is too old")).bind() } }
      .isInstanceOf<ReadFailure>().given {
        assertThat(it.kind).isEqualTo(ReadFailureKind.OTHER)
        assertThat(it.memberFacingMessage).isEqualTo("Your pet is too old")
      }
  }

  @Test
  fun `the message of any other error message stays out of the failure`() = runTest {
    assertFailure { iosReadOrThrow { Either.Left(ErrorMessage("No intent")).bind() } }
      .isInstanceOf<ReadFailure>().prop(ReadFailure::memberFacingMessage).isNull()
  }

  @Test
  fun `a cancellation passes through unchanged`() = runTest {
    val cancellation = CancellationException("cancelled")

    assertFailure { iosReadOrThrow { throw cancellation } }.isSameInstanceAs(cancellation)
  }
}
