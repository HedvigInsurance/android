package com.hedvig.android.cancelinsurance.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.cancelinsurance.InsuranceId
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

// todo real impl
internal class CancelInsuranceUseCase(
  private val apolloClient: ApolloClient,
) {
  var failTimes = 3

  suspend fun invoke(
    insuranceId: InsuranceId,
    selectedDateMillis: Long,
  ): Either<OperationResult.Error, InsuranceId> {
    coroutineContext.ensureActive()
    return if (failTimes != 0) {
      failTimes--
      OperationResult.Error.NetworkError("manual failure").left()
    } else {
      insuranceId.right()
    }
  }
}
