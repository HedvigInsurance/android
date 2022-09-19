package com.hedvig.android.core.common.di

import org.koin.core.qualifier.qualifier

/**
 * Provided by the app module, returns the result of BuildConfig.DEBUG.
 */
val isDebugQualifier = qualifier("isDebugQualifier")

/**
 * Provides a function which returns the gql Locale to be used as parameter in queries that need it.
 */
val getGraphqlLocaleFunctionQualifier = qualifier("getGraphqlLocaleFunctionQualifier")
