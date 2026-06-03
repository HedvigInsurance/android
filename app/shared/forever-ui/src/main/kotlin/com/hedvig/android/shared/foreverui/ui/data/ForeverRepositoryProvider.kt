package com.hedvig.android.shared.foreverui.ui.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<Provider<ForeverRepository>>())
internal class ForeverRepositoryProvider(
  override val demoManager: DemoManager,
  override val prodImpl: ForeverRepositoryImpl,
  override val demoImpl: ForeverRepositoryDemo,
) : ProdOrDemoProvider<ForeverRepository>
