package com.hedvig.android.data.forever.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.data.forever.ForeverRepository

class ForeverRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ForeverRepository,
  override val prodImpl: ForeverRepository,
) : ProdOrDemoProvider<ForeverRepository>
