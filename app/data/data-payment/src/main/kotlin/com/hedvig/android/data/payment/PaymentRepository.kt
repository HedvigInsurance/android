package com.hedvig.android.data.payment

import arrow.core.Either
import com.hedvig.android.apollo.OperationResult

interface PaymentRepository {
  suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory>

  suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData>
}
