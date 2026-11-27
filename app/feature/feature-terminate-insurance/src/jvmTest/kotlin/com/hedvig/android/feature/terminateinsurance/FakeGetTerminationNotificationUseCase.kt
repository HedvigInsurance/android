package com.hedvig.android.feature.terminateinsurance

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.data.GetTerminationNotificationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

internal class FakeGetTerminationNotificationUseCase(
  private val message: Either<ErrorMessage, String?> = null.right(),
) : GetTerminationNotificationUseCase(
    apolloClient = ApolloClient.Builder().serverUrl("https://unused").build(),
  ) {
  val requests = mutableListOf<Pair<String, LocalDate>>()

  override fun invoke(contractId: String, terminationDate: LocalDate): Flow<Either<ErrorMessage, String?>> {
    requests.add(contractId to terminationDate)
    return flowOf(message)
  }
}
