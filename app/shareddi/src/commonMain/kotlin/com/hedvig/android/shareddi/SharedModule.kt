package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.di.buildConstantsModule
import com.hedvig.android.network.clients.di.networkModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(appBuildConfig: AppBuildConfig) = module {
  single<AppBuildConfig> { appBuildConfig }
  includes(
    buildConstantsModule,
    networkModule,
    platformModule,
  )
}

internal expect val platformModule: Module
