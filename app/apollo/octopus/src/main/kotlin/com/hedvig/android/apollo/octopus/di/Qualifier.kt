package com.hedvig.android.apollo.octopus.di

import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.qualifier

/**
 * The [com.apollographql.apollo3.ApolloClient] targeting Octopus
 */
val octopusClient: StringQualifier = qualifier("octopusClient")
