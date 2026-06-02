package com.hedvig.android.feature.home.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlinx.datetime.TimeZone

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<Provider<GetHomeDataUseCase>>())
internal class GetHomeDataUseCaseProvider(
  override val demoManager: DemoManager,
  apolloClient: ApolloClient,
  hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  getMemberRemindersUseCase: GetMemberRemindersUseCase,
  featureManager: FeatureManager,
  clock: Clock,
  timeZone: TimeZone,
  getTravelAddonBannerInfoUseCaseProvider: Provider<GetAddonBannerInfoUseCase>,
) : ProdOrDemoProvider<GetHomeDataUseCase> {
  override val prodImpl: GetHomeDataUseCase = GetHomeDataUseCaseImpl(
    apolloClient = apolloClient,
    hasAnyActiveConversationUseCase = hasAnyActiveConversationUseCase,
    getMemberRemindersUseCase = getMemberRemindersUseCase,
    featureManager = featureManager,
    clock = clock,
    timeZone = timeZone,
    getTravelAddonBannerInfoUseCaseProvider = getTravelAddonBannerInfoUseCaseProvider,
  )
  override val demoImpl: GetHomeDataUseCase = GetHomeDataUseCaseDemo()
}
