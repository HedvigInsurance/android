package com.hedvig.android.network.clients

/**
 * What the network clients log beyond GraphQL operation names and error messages.
 *
 * [logsHttpBodies] logs every HTTP request and response with its body. [logsGraphqlData] adds GraphQL response data and
 * extensions to the GraphQL log lines and to the attributes of the RUM error a GraphQL error reports.
 */
class NetworkLogPolicy(
  val logsHttpBodies: Boolean,
  val logsGraphqlData: Boolean,
)
