package com.hedvig.android.apollo.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.NetworkCacheManagerImpl
import org.koin.dsl.module

val networkCacheManagerModule = module {
  single<NetworkCacheManager> { NetworkCacheManagerImpl(get<ApolloClient>()) }
}
