package com.hedvig.android.shared.foreverui.ui.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<ForeverRepository>())
internal class SwitchingForeverRepository(
  override val demoManager: DemoManager,
  override val prodImpl: ForeverRepositoryImpl,
  override val demoImpl: ForeverRepositoryDemo,
) : ForeverRepository, DemoSwitcher<ForeverRepository>() {
  override suspend fun getReferralsData() = pick().getReferralsData()

  override suspend fun updateCode(newCode: String) = pick().updateCode(newCode)
}
