package com.hedvig.android.network.clients.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.network.clients.ExtraApolloClientConfiguration
import com.hedvig.android.network.clients.NoopExtraApolloClientConfiguration
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface NativeNetworkMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideExtraApolloClientConfiguration(): ExtraApolloClientConfiguration = NoopExtraApolloClientConfiguration()
}
