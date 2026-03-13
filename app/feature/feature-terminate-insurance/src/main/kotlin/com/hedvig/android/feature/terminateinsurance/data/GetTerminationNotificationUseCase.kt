package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import octopus.TerminationFlowNotificationQuery
import octopus.type.TerminationFlowNotificationInput

internal class GetTerminationNotificationUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(contractId: String, terminationDate: LocalDate): Flow<Either<ErrorMessage, String?>> {
    return apolloClient
      .query(
        TerminationFlowNotificationQuery(
          TerminationFlowNotificationInput(
            contractId = contractId,
            terminationDate = terminationDate,
          ),
        ),
      )
      .safeFlow(::ErrorMessage)
      .map { response ->
        either {
          response.bind().currentMember.terminationFlowNotification?.message
        }
      }
  }
}
