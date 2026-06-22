package com.hedvig.android.feature.home.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseDemo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCaseImpl
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingGetHomeDataUseCase(
  private val demoManager: DemoManager,
  private val prodImpl: GetHomeDataUseCaseImpl,
  private val demoImpl: GetHomeDataUseCaseDemo,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean) = flow {
    emitAll(pick().invoke(forceNetworkFetch))
  }

  private suspend fun pick(): GetHomeDataUseCase = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
