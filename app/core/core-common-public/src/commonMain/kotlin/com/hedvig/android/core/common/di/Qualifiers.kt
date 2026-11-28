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

/** Apollo client that never sends an access token, for requests made before login. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicApolloClient

/** Ktor engine for the auth client, carrying the app's network observability hooks. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthHttpClientEngine
