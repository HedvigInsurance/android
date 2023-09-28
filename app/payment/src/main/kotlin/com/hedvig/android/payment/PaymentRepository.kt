package com.hedvig.android.payment

import arrow.core.Either
import com.apollographql.apollo3.api.ApolloResponse
import com.hedvig.android.apollo.OperationResult
import giraffe.PaymentQuery
import giraffe.type.PayoutMethodStatus
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
  fun payment(): Flow<ApolloResponse<PaymentQuery.Data>>
  suspend fun refresh(): ApolloResponse<PaymentQuery.Data>
  suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus)
  suspend fun getChargeHistory(): Either<OperationResult.Error, ChargeHistory>
  suspend fun getPaymentData(): Either<OperationResult.Error, PaymentData>
}
