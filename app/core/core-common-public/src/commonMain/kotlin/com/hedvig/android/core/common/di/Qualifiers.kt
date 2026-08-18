package com.hedvig.android.core.common.di

import dev.zacsweers.metro.Qualifier

/** The java.io.File used by Room to store the database. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseFile

/** CoroutineContext defaulting to Dispatchers.IO in production. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** Base ktor HttpClient without auth but with common headers + logging. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseHttpClient
