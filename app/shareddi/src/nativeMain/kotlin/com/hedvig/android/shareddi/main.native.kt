package com.hedvig.android.shareddi

import com.hedvig.feature.claim.chat.di.claimChatModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused") // Used from iOS
fun initApp() = {
  initKoin { "" }
}

@Suppress("unused") // Used from iOS
fun initKoin(getAuthToken: () -> String) {
  startKoin {
    module {
      single<IosAuthTokenInterceptor> {
        IosAuthTokenInterceptor(getAuthToken)
      }
    }
    modules(claimChatModule, platformModule, sharedModule)
  }
}
