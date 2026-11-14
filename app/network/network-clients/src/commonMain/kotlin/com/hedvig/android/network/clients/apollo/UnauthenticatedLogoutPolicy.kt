package com.hedvig.android.network.clients.apollo

/**
 * Whether a GraphQL response carrying an `UNAUTHENTICATED` error logs the member out, through
 * [LogoutOnUnauthenticatedInterceptor].
 */
internal expect val logsOutOnUnauthenticated: Boolean
