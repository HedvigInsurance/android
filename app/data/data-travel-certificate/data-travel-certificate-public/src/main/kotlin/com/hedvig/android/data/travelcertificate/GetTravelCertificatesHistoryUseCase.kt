package com.hedvig.android.data.travelcertificate

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.TravelCertificatesQuery

interface GetTravelCertificatesHistoryUseCase {
  suspend fun invoke(): List<TravelCertificate>
}

internal class GetTravelCertificatesHistoryUseCaseImpl(val apolloClient: ApolloClient) :
  GetTravelCertificatesHistoryUseCase {
  override suspend fun invoke(): List<TravelCertificate> {
    val result = apolloClient.query(TravelCertificatesQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft {
        logcat(LogPriority.ERROR) { it.message ?: "Could not fetch travel certificates history" }
      }.getOrNull()?.currentMember?.travelCertificates
    val list = result?.map {
      TravelCertificate(it.startDate, it.id, it.signedUrl, it.expiryDate)
    }
    return list ?: listOf()
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

// mock responses

val emptyList = listOf<TravelCertificate>()
val mockWithOneExpiredEarlier = listOf<TravelCertificate>(
  TravelCertificate(
    startDate = LocalDate(2024, 6, 2),
    expiryDate = LocalDate(2024, 7, 9),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
  TravelCertificate(
    startDate = LocalDate(2024, 1, 6),
    expiryDate = LocalDate(2024, 9, 10),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
  TravelCertificate(
    startDate = LocalDate(2023, 12, 9),
    expiryDate = LocalDate(2024, 1, 31),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
)
val mockWithExpiredToday = listOf<TravelCertificate>(
  TravelCertificate(
    startDate = LocalDate(2024, 1, 6),
    expiryDate = LocalDate(2024, 9, 10),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
  TravelCertificate(
    startDate = LocalDate(2023, 11, 25),
    expiryDate = LocalDate(
      java.time.LocalDate.now().year,
      java.time.LocalDate.now().month,
      java.time.LocalDate.now().dayOfMonth,
    ),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
)
val mockWithExpiredYesterday = listOf<TravelCertificate>(
  TravelCertificate(
    startDate = LocalDate(2024, 1, 6),
    expiryDate = LocalDate(2024, 9, 10),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
  TravelCertificate(
    startDate = LocalDate(2023, 11, 25),
    expiryDate = LocalDate(
      java.time.LocalDate.now().year,
      java.time.LocalDate.now().month,
      java.time.LocalDate.now().dayOfMonth - 1,
    ),
    id = "13213",
    signedUrl = "wkehdkwed",
  ),
)
