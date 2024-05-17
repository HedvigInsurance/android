package com.hedvig.android.feature.home.home.data

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.nullable
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.HomeQuery
import octopus.type.CrossSellType

internal interface GetHomeDataUseCase {
  fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>>
}

internal class GetHomeDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val shouldShowChatButtonUseCase: ShouldShowChatButtonUseCase,
  private val featureManager: FeatureManager,
  private val clock: Clock,
  private val timeZone: TimeZone,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>> {
    return combine(
      apolloClient.query(HomeQuery())
        .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheFirst)
        .safeFlow(::ErrorMessage),
      getMemberRemindersUseCase.invoke(),
      shouldShowChatButtonUseCase.invoke(),
      featureManager.isFeatureEnabled(Feature.HELP_CENTER),
    ) { homeQueryDataResult, memberReminders, showChatIcon, isHelpCenterEnabled ->
      either {
        val homeQueryData: HomeQuery.Data = homeQueryDataResult.bind()
        val contractStatus = homeQueryData.currentMember.toContractStatus()
        val veryImportantMessages = homeQueryData.currentMember.importantMessages.map {
          HomeData.VeryImportantMessage(
            id = it.id,
            message = it.message,
            link = it.link.ifEmpty { null },
          )
        }
        val crossSells = homeQueryData.currentMember.crossSells.map { crossSell ->
          CrossSell(
            id = crossSell.id,
            title = crossSell.title,
            subtitle = crossSell.description,
            storeUrl = crossSell.storeUrl,
            type = when (crossSell.type) {
              CrossSellType.CAR -> CrossSell.CrossSellType.CAR
              CrossSellType.HOME -> CrossSell.CrossSellType.HOME
              CrossSellType.ACCIDENT -> CrossSell.CrossSellType.ACCIDENT
              CrossSellType.PET -> CrossSell.CrossSellType.PET
              CrossSellType.UNKNOWN__ -> CrossSell.CrossSellType.UNKNOWN
            },
          )
        }.toPersistentList()
        val firstVetActions = homeQueryData.currentMember.memberActions
          ?.firstVetAction?.sections?.map { section ->
            FirstVetSection(
              section.buttonTitle,
              section.description,
              section.title,
              section.url,
            )
          } ?: emptyList()
        HomeData(
          contractStatus = contractStatus,
          claimStatusCardsData = homeQueryData.claimStatusCards(),
          veryImportantMessages = veryImportantMessages.toPersistentList(),
          memberReminders = memberReminders,
          showChatIcon = showChatIcon,
          showHelpCenter = isHelpCenterEnabled,
          firstVetSections = firstVetActions,
          crossSells = crossSells,
        )
      }.onLeft { errorMessage ->
        logcat(throwable = errorMessage.throwable) { "GetHomeDataUseCase failed with ${errorMessage.message}" }
      }
    }
  }

  private fun HomeQuery.Data.CurrentMember.toContractStatus(): HomeData.ContractStatus {
    val activeInTheFutureDate = activeInTheFutureDate()
    if (activeInTheFutureDate != null) {
      return HomeData.ContractStatus.ActiveInFuture(activeInTheFutureDate)
    }
    val hasActiveContracts = activeContracts.isNotEmpty()
    if (hasActiveContracts) {
      return HomeData.ContractStatus.Active
    }
    if (isAutomaticallySwitching()) {
      return HomeData.ContractStatus.Switching
    }
    if (areAllNonTerminatedContractsPending()) {
      return HomeData.ContractStatus.Pending
    }
    val allAreTerminated = activeContracts.isEmpty() && pendingContracts.isEmpty() && terminatedContracts.isNotEmpty()
    if (allAreTerminated) {
      return HomeData.ContractStatus.Terminated
    }
    return HomeData.ContractStatus.Unknown.also {
      logcat(LogPriority.ERROR) {
        "HomeQuery.Data.CurrentMember:$this. Resulted in an unknown contract. It should be mapped instead"
      }
    }
  }

  private fun HomeQuery.Data.CurrentMember.isAutomaticallySwitching(): Boolean {
    val isSwitchable = this.pendingContracts.any { pendingContract ->
      pendingContract.externalInsuranceCancellationHandledByHedvig
    }
    return isSwitchable && areAllNonTerminatedContractsPending()
  }

  /**
   * @return [LocalDate] of the earliest upcoming active contract.
   * returns null when there are no active contracts, or the earliest starting contract has already started in the past
   */
  private fun HomeQuery.Data.CurrentMember.activeInTheFutureDate(): LocalDate? {
    return nullable {
      val earliestStartingActiveContract = activeContracts
        .sortedBy { it.masterInceptionDate }
        .firstOrNull()
        .bind()
      val masterInceptionDate = earliestStartingActiveContract.masterInceptionDate
      ensure(masterInceptionDate.atStartOfDayIn(timeZone) > clock.now())
      masterInceptionDate
    }
  }

  private fun HomeQuery.Data.CurrentMember.areAllNonTerminatedContractsPending(): Boolean {
    return activeContracts.isEmpty() && pendingContracts.isNotEmpty()
  }
}

private fun HomeQuery.Data.claimStatusCards(): HomeData.ClaimStatusCardsData? {
  val claimStatusCards: NonEmptyList<HomeQuery.Data.CurrentMember.Claim> =
    this.currentMember.claims.toNonEmptyListOrNull() ?: return null
  return HomeData.ClaimStatusCardsData(claimStatusCards.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery))
}

internal data class ChatMessage(
  val id: String,
  val sender: Sender,
) {
  enum class Sender {
    HEDVIG,
    MEMBER,
  }
}

internal data class HomeData(
  val contractStatus: ContractStatus,
  val claimStatusCardsData: ClaimStatusCardsData?,
  val veryImportantMessages: ImmutableList<VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val showChatIcon: Boolean,
  val showHelpCenter: Boolean,
  val firstVetSections: List<FirstVetSection>,
  val crossSells: ImmutableList<CrossSell>,
) {
  @Immutable
  data class ClaimStatusCardsData(
    val claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  )

  data class VeryImportantMessage(
    val id: String,
    val message: String,
    val link: String?,
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
