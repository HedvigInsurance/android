package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.network.clients.di.networkModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(appBuildConfig: AppBuildConfig) = module {
  single<AppBuildConfig> { appBuildConfig }
  includes(platformModule, networkModule, dataStoreModule)
}

internal expect val platformModule: Module
