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
import kotlinx.serialization.Serializable
import octopus.TerminationFlowNotificationQuery
import octopus.fragment.TerminationNotificationFragment
import octopus.type.FlowTerminationNotificationType

internal class GetTerminationNotificationUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(contractId: String, terminationDate: LocalDate): Flow<Either<ErrorMessage, TerminationNotification>> {
    return apolloClient
      .query(
        TerminationFlowNotificationQuery(
          contractId = contractId,
          terminationDate = terminationDate,
        ),
      )
      .safeFlow(::ErrorMessage)
      .map { response ->
        either {
          val notification = response.bind().currentMember.terminationFlowNotification?.toTerminationNotification()
          notification ?: raise(ErrorMessage("No termination notification found"))
        }
      }
  }
}

private fun TerminationNotificationFragment.toTerminationNotification(): TerminationNotification {
  return TerminationNotification(
    message = message,
    type = type.toTerminationNotificationType(),
  )
}

private fun FlowTerminationNotificationType.toTerminationNotificationType(): TerminationNotificationType {
  return when (this) {
    FlowTerminationNotificationType.INFO -> TerminationNotificationType.Info
    FlowTerminationNotificationType.WARNING -> TerminationNotificationType.Attention
    FlowTerminationNotificationType.UNKNOWN__ -> TerminationNotificationType.Unknown
  }
}

@Serializable
internal data class TerminationNotification(
  val message: String,
  val type: TerminationNotificationType,
)

@Serializable
internal enum class TerminationNotificationType {
  Info,
  Attention,
  Unknown,
}
