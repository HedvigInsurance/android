package com.hedvig.android.core.common.android.di

import org.koin.core.qualifier.qualifier

/**
 * Provided by the app module, returns the result of BuildConfig.DEBUG.
 */
val isDebugQualifier = qualifier("isDebugQualifier")

/**
 * The [java.io.File] to be used from datastore to store the preferences file in.
 * It's provided as a file in order to keep the datastore module free of Android dependencies by skipping fetching the
 * file from context, and instead being fed the file in directly.
 */
val datastoreFileQualifier = qualifier("datastoreFileQualifier")
