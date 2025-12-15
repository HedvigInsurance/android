package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<ExtraApolloClientConfiguration> {
    IosExtraApolloClientConfiguration(get<IosAuthTokenInterceptor>())
  }
}

/**
 * Like [platformModule] but allows for dynamic input, for pieces that need to be injected from iOS
 */
internal fun iosPlatformModule(accessTokenFetcher: AccessTokenFetcher) = module {
  single<AccessTokenFetcher> {
    accessTokenFetcher
  }
  single<IosAuthTokenInterceptor> {
    IosAuthTokenInterceptor(get<AccessTokenFetcher>())
  }
  single<HedvigBuildConstants> {
    IosHedvigBuildConstants()
  }
}

private class IosExtraApolloClientConfiguration(
  private val iosAuthTokenInterceptor: IosAuthTokenInterceptor,
) : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
    return builder
      .addInterceptor(iosAuthTokenInterceptor)
      .httpEngine(DefaultHttpEngine())
  }
}

// todo ios provide these from iOS, or provide the 3 options in code and take in prod/stage/dev env as input from iOS
private class IosHedvigBuildConstants : HedvigBuildConstants {
  override val urlGraphqlOctopus: String
    get() = "noop"
  override val urlBaseWeb: String
    get() = "noop"
  override val urlOdyssey: String
    get() = "noop"
  override val urlBotService: String
    get() = "noop"
  override val urlClaimsService: String
    get() = "noop"
  override val deepLinkHosts: List<String>
    get() = listOf("noop")
  override val appVersionName: String
    get() = "noop"
  override val appVersionCode: String
    get() = "noop"
  override val appPackageId: String
    get() = "noop"
  override val isDebug: Boolean
    get() = true
  override val isProduction: Boolean
    get() = false
  override val buildApiVersion: Int
    get() = -1
}
