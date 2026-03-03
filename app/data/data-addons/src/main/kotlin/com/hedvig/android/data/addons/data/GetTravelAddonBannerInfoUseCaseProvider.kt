package com.hedvig.android.data.addons.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class GetTravelAddonBannerInfoUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetAddonBannerInfoUseCase,
  override val prodImpl: GetAddonBannerInfoUseCase,
) : ProdOrDemoProvider<GetAddonBannerInfoUseCase>
