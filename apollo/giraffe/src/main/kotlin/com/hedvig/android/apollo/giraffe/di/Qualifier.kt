package com.hedvig.android.apollo.giraffe.di

import org.koin.core.qualifier.qualifier

/**
 * The [com.apollographql.apollo3.ApolloClient] targeting Giraffe
 */
val giraffeClient = qualifier("giraffeClient")
