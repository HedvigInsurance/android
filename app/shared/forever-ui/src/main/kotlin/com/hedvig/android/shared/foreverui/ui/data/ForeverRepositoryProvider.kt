package com.hedvig.android.shared.foreverui.ui.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class ForeverRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ForeverRepository,
  override val prodImpl: ForeverRepository,
) : ProdOrDemoProvider<ForeverRepository>
