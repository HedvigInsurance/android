package com.hedvig.app.util.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.ws.closeConnection

object ReopenSubscriptionException : Exception("ReopenSubscriptionException")

/**
 * The name "reconnect" depends on the apolloClient being configured to re-open the subscription when specifically
 * [ReopenSubscriptionException] is thrown.
 *
 * Example:
 * ```
 * apolloClientBuilder.webSocketReopenWhen { throwable, _ ->
 *   if (throwable is ReopenSubscriptionException) {
 *     return@webSocketReopenWhen true
 *   }
 *   false
 * }
 * ```
 */
fun ApolloClient.reconnectSubscriptions() {
  subscriptionNetworkTransport.closeConnection(ReopenSubscriptionException)
}
