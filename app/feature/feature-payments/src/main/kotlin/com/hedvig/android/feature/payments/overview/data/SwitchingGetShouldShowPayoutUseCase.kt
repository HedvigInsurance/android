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
@ContributesBinding(AppScope::class, binding = binding<GetShouldShowPayoutUseCase>())
internal class SwitchingGetShouldShowPayoutUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetShouldShowPayoutUseCaseImpl,
  override val demoImpl: GetShouldShowPayoutUseCaseDemo,
) : GetShouldShowPayoutUseCase, DemoSwitcher<GetShouldShowPayoutUseCase>() {
  override suspend fun invoke() = pick().invoke()
}
