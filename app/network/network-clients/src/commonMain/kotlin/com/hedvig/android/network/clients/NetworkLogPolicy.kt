package com.hedvig.android.network.clients

/**
 * What the network clients log beyond GraphQL operation names, error messages, error extensions and exceptions.
 *
 * [logsHttpBodies] logs every HTTP request and response with its body. [logsGraphqlData] adds the GraphQL response
 * data to the GraphQL log lines and to the attributes of the RUM error a GraphQL error reports.
 */
class NetworkLogPolicy(
  val logsHttpBodies: Boolean,
  val logsGraphqlData: Boolean,
)
