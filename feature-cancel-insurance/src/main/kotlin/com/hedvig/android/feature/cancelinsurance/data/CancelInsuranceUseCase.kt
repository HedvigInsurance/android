package com.hedvig.android.feature.cancelinsurance.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.feature.cancelinsurance.InsuranceId
import kotlinx.coroutines.ensureActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.days

// todo real impl
internal class CancelInsuranceUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    insuranceId: InsuranceId,
    selectedDateMillis: Long,
  ): Either<OperationResult.Error, InsuranceId> {
    coroutineContext.ensureActive()
    val submittedDate = Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC)
    return if (submittedDate < Clock.System.now().plus(3.days).toLocalDateTime(TimeZone.UTC)) {
      // Artificial failure when selecting in the next 3 days
      OperationResult.Error.GeneralError("Too early").left()
    } else {
      insuranceId.right()
    }
  }
}
