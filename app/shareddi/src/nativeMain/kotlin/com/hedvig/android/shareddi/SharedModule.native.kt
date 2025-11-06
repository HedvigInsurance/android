package com.hedvig.android.shareddi

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
  single<ExtraApolloClientConfiguration> {
    IosExtraApolloClientConfiguration(get<IosAuthTokenInterceptor>())
  }
}

private class IosExtraApolloClientConfiguration(
  private val iosAuthTokenInterceptor: IosAuthTokenInterceptor
) : ExtraApolloClientConfiguration {
  override fun configure(builder: ApolloClient.Builder): ApolloClient.Builder {
    return builder
      .addInterceptor(iosAuthTokenInterceptor)
      .httpEngine(DefaultHttpEngine())
  }
}
