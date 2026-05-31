package com.hedvig.android.feature.chat.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.chat.data.CbmChatRepository
import com.hedvig.android.feature.chat.data.CbmChatRepositoryDemo
import com.hedvig.android.feature.chat.data.CbmChatRepositoryImpl
import com.hedvig.android.feature.chat.data.GetCbmChatRepositoryProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
internal interface ChatMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetCbmChatRepositoryProvider(
    demoManager: DemoManager,
    prodImpl: CbmChatRepositoryImpl,
    demoImpl: CbmChatRepositoryDemo,
  ): Provider<CbmChatRepository> = GetCbmChatRepositoryProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )
}
