package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.CrossSell
import kotlinx.coroutines.flow.Flow

internal interface GetCrossSellsUseCase {
  fun invoke(): Flow<Either<ErrorMessage, List<CrossSell>>>
}
