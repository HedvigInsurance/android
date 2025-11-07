package com.hedvig.android.shareddi

import com.hedvig.feature.claim.chat.di.claimChatModule
import org.koin.core.context.startKoin

@Suppress("unused") // Used from iOS
fun initKoin(getAuthToken: () -> String) {
  startKoin {
    modules(iosPlatformModule(getAuthToken), platformModule, sharedModule, claimChatModule)
  }
}
