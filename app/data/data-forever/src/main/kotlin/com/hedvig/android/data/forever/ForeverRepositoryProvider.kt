package com.hedvig.android.data.forever

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class ForeverRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ForeverRepository,
  override val prodImpl: ForeverRepository,
) : ProdOrDemoProvider<ForeverRepository>
