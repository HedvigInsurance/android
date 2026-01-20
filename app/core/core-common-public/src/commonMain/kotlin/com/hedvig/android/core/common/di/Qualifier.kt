package com.hedvig.android.core.common.di

import org.koin.core.qualifier.qualifier

/**
 * The [java.io.File] to be used by Room to store the database in.
 */
val databaseFileQualifier = qualifier("databaseFileQualifier")

/**
 * A qualifier to pass a [kotlin.coroutines.CoroutineContext] which should default to
 * [kotlinx.coroutines.Dispatchers.IO] for production code
 */
val ioDispatcherQualifier = qualifier("ioDispatcher")

/**
 * Qualifier for the base ktor HttpClient which does not have authentication configured but does have the common
 * headers and logging which all http clients should have
 */
val baseHttpClientQualifier = qualifier("baseHttpClientQualifier")
