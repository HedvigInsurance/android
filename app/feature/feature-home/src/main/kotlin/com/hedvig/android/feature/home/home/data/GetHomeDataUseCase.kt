package com.hedvig.android.feature.home.data

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import giraffe.HomeQuery
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

internal interface GetHomeDataUseCase {
  fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>>
}

internal class GetHomeDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>> {
    return combine(
      apolloClient.query(
        HomeQuery(
          languageService.getGraphQLLocale(),
          languageService.getGraphQLLocale().rawValue,
        ),
      )
        .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheFirst)
        .safeFlow(::ErrorMessage),
      getMemberRemindersUseCase.invoke(),
      flow { emit(getTravelCertificateSpecificationsUseCase.invoke().getOrNull()) },
    ) { homeQueryDataResult, memberReminders, travelCertificateData ->
      either {
        val homeQueryData = homeQueryDataResult.bind()
        val memberName = homeQueryData.member.firstName
        val contractStatus = homeQueryData.toContractStatus()
        val veryImportantMessages = homeQueryData.importantMessages.mapNotNull {
          HomeData.VeryImportantMessage(
            message = it.message ?: return@mapNotNull null,
            link = it.link ?: return@mapNotNull null,
          )
        }
        HomeData(
          memberName = memberName,
          contractStatus = contractStatus,
          claimStatusCardsData = homeQueryData.claimStatusCards(),
          veryImportantMessages = veryImportantMessages.toPersistentList(),
          memberReminders = memberReminders,
          allowAddressChange = contractStatus is HomeData.ContractStatus.Active,
          allowGeneratingTravelCertificate = travelCertificateData != null,
          emergencyData = EmergencyData.from(homeQueryData),
        )
      }.onLeft { errorMessage ->
        logcat(throwable = errorMessage.throwable) { "GetHomeDataUseCase failed with ${errorMessage.message}" }
      }
    }
  }

  private fun HomeQuery.Data.toContractStatus(): HomeData.ContractStatus {
    val isActive = contracts.any { contract ->
      contract.status.asActiveStatus != null ||
        contract.status.asTerminatedTodayStatus != null ||
        contract.status.asTerminatedInFutureStatus != null
    }
    if (isActive) {
      return HomeData.ContractStatus.Active
    }
    if (isSwitching()) {
      return HomeData.ContractStatus.Switching
    }
    if (areAllContractsPending()) {
      return HomeData.ContractStatus.Pending
    }
    activeInTheFutureDate()?.let {
      return HomeData.ContractStatus.ActiveInFuture(it)
    }
    val allAreTerminated = contracts.all { it.status.asTerminatedStatus != null }
    if (allAreTerminated) {
      return HomeData.ContractStatus.Terminated
    }
    return HomeData.ContractStatus.Unknown.also {
      logcat(LogPriority.ERROR) {
        "HomeQuery.Data:$this. Resulted in an unknown contract. It should be mapped instead"
      }
    }
  }

  private fun HomeQuery.Data.isSwitching(): Boolean {
    val isProviderSwitchable = contracts.any { contract ->
      val switchedFromProvider = insuranceProviders.firstOrNull {
        it.id == contract.switchedFromInsuranceProvider
      }
      switchedFromProvider?.switchable ?: false
    }
    return isProviderSwitchable && (areAllContractsPending() || activeInTheFutureDate() != null)
  }

  private fun HomeQuery.Data.activeInTheFutureDate(): LocalDate? {
    return contracts.firstNotNullOfOrNull { contract ->
      contract.status.asActiveInFutureStatus?.futureInception
        ?: contract.status.asActiveInFutureAndTerminatedInFutureStatus?.futureInception
    }?.toKotlinLocalDate()
  }

  private fun HomeQuery.Data.areAllContractsPending(): Boolean {
    return contracts.all { it.status.asPendingStatus != null }
  }
}

private fun HomeQuery.Data.claimStatusCards(): HomeData.ClaimStatusCardsData? {
  val claimStatusCards: NonEmptyList<HomeQuery.ClaimStatusCard> =
    claimStatusCards.toNonEmptyListOrNull() ?: return null
  return HomeData.ClaimStatusCardsData(claimStatusCards.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery))
}

internal data class HomeData(
  val memberName: String?,
  val contractStatus: ContractStatus,
  val claimStatusCardsData: ClaimStatusCardsData?,
  val veryImportantMessages: ImmutableList<VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val allowAddressChange: Boolean,
  val allowGeneratingTravelCertificate: Boolean,
  val emergencyData: EmergencyData?,
) {

  @Immutable
  data class ClaimStatusCardsData(
    val claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  )

  data class VeryImportantMessage(
    val message: String,
    val link: String,
  )

  sealed interface ContractStatus {
    data object Active : ContractStatus
    data object Terminated : ContractStatus
    data object Pending : ContractStatus
    data object Switching : ContractStatus
    data class ActiveInFuture(
      val futureInceptionDate: LocalDate,
    ) : ContractStatus

    data object Unknown : ContractStatus
  }

  companion object
}
