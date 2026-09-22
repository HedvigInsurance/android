package com.hedvig.android.feature.claim.chat.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.hedvig.android.apollo.safeExecute
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import octopus.ClaimIntentQuery

/**
 * The classifier is only useful if a transport failure still carries its type by the time it
 * reaches it. Asserting that against a hand-built error proves nothing, because the error has to
 * survive the merge step in `safeExecute` first.
 */
@OptIn(ApolloExperimental::class)
class ApolloErrorReachesTheClassifierTest {
  @Test
  fun `a network failure survives safeExecute as a connection error`() = runTest {
    val apolloClient = ApolloClient.Builder()
      .networkTransport(QueueTestNetworkTransport())
      .build()
    apolloClient.enqueueTestNetworkError()

    val error = apolloClient
      .query(ClaimIntentQuery("intent-id"))
      .safeExecute()
      .leftOrNull()

    assertThat(error?.toClaimChatErrorMessage()).isEqualTo(ClaimChatErrorMessage.ConnectionError)
  }
}
