package com.hedvig.android.data.addons.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingGetAddonBannerInfoUseCase(
  private val demoManager: DemoManager,
  private val demoImpl: DemoGetAddonBannerInfoUseCase,
  private val prodImpl: GetAddonBannerInfoUseCaseImpl,
) : GetAddonBannerInfoUseCase {
  override fun invoke(source: AddonBannerSource) = flow {
    emitAll(pick().invoke(source))
  }

  private suspend fun pick(): GetAddonBannerInfoUseCase = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
