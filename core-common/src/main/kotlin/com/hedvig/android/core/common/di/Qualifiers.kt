package com.hedvig.android.core.common.di

import org.koin.core.qualifier.qualifier

/**
 * Provided by the app module, returns the result of BuildConfig.DEBUG.
 */
val isDebugQualifier = qualifier("isDebugQualifier")

/**
 * Provided by the app module, returns whether we are running in release mode.
 * This is useful as [isDebugQualifier] only returns true for dev environment but false for staging environment and we
 * do sometimes want to know the difference.
 */
val isProductionQualifier = qualifier("buildTypeQualifier")

/**
 * The [java.io.File] to be used from datastore to store the preferences file in.
 * It's provided as a file in order to keep the datastore module free of Android dependencies by skipping fetching the
 * file from context, and instead being fed the file in directly.
 */
val datastoreFileQualifier = qualifier("datastoreFileQualifier")

/**
 * A qualifier to pass a lambda to log information from jvm modules
 */
val logInfoQualifier = qualifier("logInfoQualifier")
typealias LogInfoType = (() -> String) -> Unit

/**
 * A qualifier to pass a [kotlin.coroutines.CoroutineContext] which should default to
 * [kotlinx.coroutines.Dispatchers.IO] for production code
 */
val ioDispatcherQualifier = qualifier("ioDispatcher")

// The URL for the octopus super-graph
val octopusGraphQLUrlQualifier = qualifier("octopusGraphQLUrlQualifier")

// The URL for giraffe
val giraffeGraphQLUrlQualifier = qualifier("octopusGraphQLUrlQualifier")

// The URL for the websocket of giraffe
val giraffeGraphQLWebSocketUrlQualifier = qualifier("giraffeGraphQLWebSocketUrlQualifier")

// The ApolloClient targeting Octopus
val octopusClient = qualifier("octopusClient")

// The ApolloClient targeting Giraffe
val giraffeClient = qualifier("giraffeClient")
