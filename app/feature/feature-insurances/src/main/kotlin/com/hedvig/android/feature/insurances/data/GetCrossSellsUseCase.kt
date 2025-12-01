package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.CrossSell
import octopus.CrossSellsQuery

internal interface GetCrossSellsUseCase {
  suspend fun invoke(): Either<ErrorMessage, CrossSellResult>
}
