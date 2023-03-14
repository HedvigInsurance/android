package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import kotlinx.datetime.LocalDate
import octopus.FlowTerminationDateNextMutation
import octopus.FlowTerminationStartMutation
import octopus.type.FlowTerminationDateInput
import octopus.type.FlowTerminationStartInput

internal class TerminateInsuranceRepository(
  private val apolloClient: ApolloClient,
) {
  private lateinit var terminationContext: Any

  suspend fun startTerminationFlow(insuranceId: InsuranceId): Either<ErrorMessage, TerminateInsuranceStep> {
    return either {
      val result = apolloClient
        .mutation(FlowTerminationStartMutation(FlowTerminationStartInput(insuranceId.id)))
        .safeExecute()
        .toEither { ErrorMessage(it) }
        .bind()
      terminationContext = result.flowTerminationStart.context
      result.flowTerminationStart.currentStep.toTerminateInsuranceStep()
    }
  }

  suspend fun setTerminationDate(terminationDate: LocalDate): Either<ErrorMessage, TerminateInsuranceStep> {
    val nextMutation = FlowTerminationDateNextMutation(
      context = terminationContext,
      input = FlowTerminationDateInput(terminationDate),
    )
    return either {
      val result = apolloClient
        .mutation(nextMutation)
        .safeExecute()
        .toEither { ErrorMessage(it) }
        .bind()
      terminationContext = result.flowTerminationDateNext.context
      result.flowTerminationDateNext.currentStep.toTerminateInsuranceStep()
    }
  }
}
