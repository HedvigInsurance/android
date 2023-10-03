package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import octopus.CrossSalesQuery

internal interface GetCrossSellsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>>
}
