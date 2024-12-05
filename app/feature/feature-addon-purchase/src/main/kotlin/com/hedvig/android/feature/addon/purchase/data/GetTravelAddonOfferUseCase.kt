package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer

internal interface GetTravelAddonOfferUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, TravelAddonOffer>
}
