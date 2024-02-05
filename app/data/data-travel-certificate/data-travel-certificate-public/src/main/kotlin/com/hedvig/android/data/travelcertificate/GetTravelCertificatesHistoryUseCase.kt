package com.hedvig.android.data.travelcertificate

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import octopus.TravelCertificatesQuery

interface GetTravelCertificatesHistoryUseCase {
  suspend fun invoke(): StateFlow<List<TravelCertificate>>
}

internal class GetTravelCertificatesHistoryUseCaseImpl(val apolloClient: ApolloClient) :
  GetTravelCertificatesHistoryUseCase {
  override suspend fun invoke(): StateFlow<List<TravelCertificate>> {
    val result = apolloClient.query(TravelCertificatesQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft {
        logcat(LogPriority.ERROR) { it.message ?: "Could not fetch travel certificates history" }
      }.getOrNull()?.currentMember?.travelCertificates
    val list = result?.map {
      TravelCertificate(it.startDate, it.id, it.signedUrl, it.expiryDate)
    }
    return MutableStateFlow(list ?: listOf()).asStateFlow()
  }
}

data class TravelCertificate(
  val startDate: LocalDate,
  val id: String,
  val signedUrl: String,
  val expiryDate: LocalDate,
)

sealed interface TravelCertificateHistoryError {
  data class Error(
    val errorMessage: ErrorMessage,
  ) : TravelCertificateHistoryError, ErrorMessage by errorMessage
}
