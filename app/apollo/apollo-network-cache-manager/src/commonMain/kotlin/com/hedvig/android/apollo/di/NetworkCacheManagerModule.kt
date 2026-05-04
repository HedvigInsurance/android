package com.hedvig.android.apollo.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ApolloNetworkCacheManager
import com.hedvig.android.apollo.NetworkCacheManager
import org.koin.dsl.module

val networkCacheManagerModule = module {
  single<NetworkCacheManager> { ApolloNetworkCacheManager(get<ApolloClient>()) }
}
