package com.hedvig.android.data.forever.di

import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.feature.demomode.DemoManager
import com.hedvig.android.feature.demomode.ProdOrDemoProvider

class ForeverRepositoryProvider(
  demoManager: DemoManager,
  demoImpl: ForeverRepository,
  prodImpl: ForeverRepository,
) : ProdOrDemoProvider<ForeverRepository>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
