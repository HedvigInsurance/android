package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetInsuranceContractsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>>
}
