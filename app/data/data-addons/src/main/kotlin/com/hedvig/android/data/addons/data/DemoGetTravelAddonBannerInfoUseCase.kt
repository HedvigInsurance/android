package com.hedvig.android.data.addons.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DemoGetTravelAddonBannerInfoUseCase : GetTravelAddonBannerInfoUseCase {
  override fun invoke(source: TravelAddonBannerSource): Flow<Either<ErrorMessage, TravelAddonBannerInfo?>> {
    return flowOf(null.right())
  }
}
