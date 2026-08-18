package com.hedvig.android.feature.payments.overview.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetUpcomingPaymentUseCase>())
internal class SwitchingGetUpcomingPaymentUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetUpcomingPaymentUseCaseImpl,
  override val demoImpl: GetUpcomingPaymentUseCaseDemo,
) : GetUpcomingPaymentUseCase, DemoSwitcher<GetUpcomingPaymentUseCase>() {
  override suspend fun invoke() = pick().invoke()
}
