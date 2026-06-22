package com.hedvig.android.feature.payments.overview.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingGetUpcomingPaymentUseCase(
  private val demoManager: DemoManager,
  private val prodImpl: GetUpcomingPaymentUseCaseImpl,
  private val demoImpl: GetUpcomingPaymentUseCaseDemo,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke() = pick().invoke()

  private suspend fun pick(): GetUpcomingPaymentUseCase = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
