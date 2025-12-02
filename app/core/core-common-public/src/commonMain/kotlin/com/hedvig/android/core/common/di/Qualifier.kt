package com.hedvig.android.core.common.di

import org.koin.core.qualifier.qualifier

/**
 * The [java.io.File] to be used from datastore to store the preferences file in.
 * It's provided as a file in order to keep the datastore module free of Android dependencies by skipping fetching the
 * file from context, and instead being fed the file in directly.
 */
val datastoreFileQualifier = qualifier("datastoreFileQualifier")

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
