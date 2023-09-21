package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import octopus.CrossSalesQuery

internal class GetCrossSellsDemoUseCaseImpl : GetCrossSellsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<CrossSalesQuery.Data.CurrentMember.CrossSell>> {
    return either {
      listOf()
    }
  }
}
