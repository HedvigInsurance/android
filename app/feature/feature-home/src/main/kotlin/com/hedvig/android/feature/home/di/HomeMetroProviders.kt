package com.hedvig.android.feature.home.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.datetime.TimeZone

@ContributesTo(AppScope::class)
interface HomeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetHomeDataUseCaseProvider(
    demoManager: DemoManager,
    apolloClient: ApolloClient,
    hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
    getMemberRemindersUseCase: GetMemberRemindersUseCase,
    featureManager: FeatureManager,
    clock: Clock,
    timeZone: TimeZone,
    getTravelAddonBannerInfoUseCaseProvider: GetTravelAddonBannerInfoUseCaseProvider,
  ): Provider<GetHomeDataUseCase> = GetHomeDataUseCaseProvider(
    demoManager = demoManager,
    prodImpl = GetHomeDataUseCaseImpl(
      apolloClient = apolloClient,
      hasAnyActiveConversationUseCase = hasAnyActiveConversationUseCase,
      getMemberRemindersUseCase = getMemberRemindersUseCase,
      featureManager = featureManager,
      clock = clock,
      timeZone = timeZone,
      getTravelAddonBannerInfoUseCaseProvider = getTravelAddonBannerInfoUseCaseProvider,
    ),
    demoImpl = GetHomeDataUseCaseDemo(),
  )
}
