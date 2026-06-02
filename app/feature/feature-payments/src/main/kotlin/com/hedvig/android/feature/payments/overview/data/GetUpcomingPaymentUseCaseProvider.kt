package com.hedvig.android.feature.payments.overview.data

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<Provider<GetUpcomingPaymentUseCase>>())
internal class GetUpcomingPaymentUseCaseProvider(
  override val demoManager: DemoManager,
  apolloClient: ApolloClient,
  clock: Clock,
) : ProdOrDemoProvider<GetUpcomingPaymentUseCase> {
  override val prodImpl: GetUpcomingPaymentUseCase = GetUpcomingPaymentUseCaseImpl(apolloClient, clock)
  override val demoImpl: GetUpcomingPaymentUseCase = GetUpcomingPaymentUseCaseDemo(clock)
}
