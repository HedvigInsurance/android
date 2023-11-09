package com.hedvig.android.data.payment

import arrow.core.Either
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.core.common.ErrorMessage

interface PaymentRepository {
  suspend fun getChargeHistory(): Either<ErrorMessage, ChargeHistory>

  suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData>
}
