package com.hedvig.android.data.addons.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<GetAddonBannerInfoUseCase>())
internal class SwitchingGetAddonBannerInfoUseCase(
  override val demoManager: DemoManager,
  override val demoImpl: DemoGetAddonBannerInfoUseCase,
  override val prodImpl: GetAddonBannerInfoUseCaseImpl,
) : GetAddonBannerInfoUseCase, DemoSwitcher<GetAddonBannerInfoUseCase>() {
  override fun invoke(source: AddonBannerSource) = pickFlow { it.invoke(source) }
}
