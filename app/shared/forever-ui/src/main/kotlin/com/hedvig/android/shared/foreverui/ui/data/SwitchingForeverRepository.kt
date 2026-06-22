package com.hedvig.android.shared.foreverui.ui.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingForeverRepository(
  private val demoManager: DemoManager,
  private val prodImpl: ForeverRepositoryImpl,
  private val demoImpl: ForeverRepositoryDemo,
) : ForeverRepository {
  override suspend fun getReferralsData() = pick().getReferralsData()

  override suspend fun updateCode(newCode: String) = pick().updateCode(newCode)

  private suspend fun pick(): ForeverRepository = if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}
