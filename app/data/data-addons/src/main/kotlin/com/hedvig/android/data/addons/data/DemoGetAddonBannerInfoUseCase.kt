package com.hedvig.android.data.addons.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoGetAddonBannerInfoUseCase : GetAddonBannerInfoUseCase {
  override fun invoke(source: AddonBannerSource): Flow<Either<ErrorMessage, List<AddonBannerInfo>>> {
    return flowOf(emptyList<AddonBannerInfo>().right())
  }
}
