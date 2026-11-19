package com.hedvig.android.feature.home.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetHomeDataUseCase>())
internal class SwitchingGetHomeDataUseCase(
  override val demoManager: DemoManager,
  override val prodImpl: GetHomeDataUseCaseImpl,
  override val demoImpl: GetHomeDataUseCaseDemo,
) : GetHomeDataUseCase, DemoSwitcher<GetHomeDataUseCase>() {
  override fun invoke(forceNetworkFetch: Boolean) = pickFlow { it.invoke(forceNetworkFetch) }
}
