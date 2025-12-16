package com.hedvig.android.shareddi

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.datastore.DeviceIdDataStore
import io.ktor.client.HttpClientConfig
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<AccessTokenFetcher> {
    object : AccessTokenFetcher {
      override suspend fun fetch(): String? {
        return null
      }
    }
  }
  single<ExtraApolloClientConfiguration> {
    NoopExtraApolloClientConfiguration()
  }
  single<HedvigBuildConstants> {
    JvmHedvigBuildConstants
  }
}

private val JvmHedvigBuildConstants = object : HedvigBuildConstants {
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

internal actual fun HttpClientConfig<*>.installDatadogKtorPlugin(hedvigBuildConstants: HedvigBuildConstants) {
  // noop
}
