@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.network.clients

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * What the network clients log beyond GraphQL operation names, error counts, error types and exception classes.
 *
 * [logsHttpBodies] logs every HTTP request and response with its body. [logsGraphqlData] adds the GraphQL response
 * data to the GraphQL log lines, and the response data and operation variables to the attributes of the RUM error a
 * GraphQL error reports. [logsGraphqlErrors]
 * adds the GraphQL error messages, the errors with their extensions and the response extensions, and exception
 * messages; they can quote what was sent, so they have their own switch.
 */
@ObjCName(name = "HedvigSharedNetworkLogPolicy", swiftName = "NetworkLogPolicy", exact = true)
class NetworkLogPolicy(
  val logsHttpBodies: Boolean,
  val logsGraphqlData: Boolean,
  val logsGraphqlErrors: Boolean,
)
